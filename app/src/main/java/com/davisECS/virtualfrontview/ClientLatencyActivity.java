package com.davisECS.virtualfrontview;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.TextView;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientLatencyActivity extends Activity implements Session.Callback,SurfaceHolder.Callback{

    private String server_ip;
    private int server_port = 8988;
    private int mDistance;
    private static final String SERVER_IP = "server ip";
    private static final String TAG = "Latency";
    private static final String DISTANCE = "distance";

    StringBuilder sb = new StringBuilder(); // Used to write info to screen
    private TextView mPacketDetailsText;

    private int mCSeq;
    private Socket mSocket;
    private BufferedReader mBufferedReader;
    private BufferedOutputStream mOutputStream;
    private Session mSession;
    private Handler mMainHandler;
    private Handler mHandler;
    private SurfaceView mSurface;
    private String mSessionID = "-1";

    // States
    private final static int STATE_STARTED = 0x00;
    private final static int STATE_STARTING = 0x01;
    private final static int STATE_STOPPING = 0x02;
    private final static int STATE_STOPPED = 0x03;
    private int mState = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_latency);
        mPacketDetailsText = (TextView) findViewById(R.id.packet_details);
        mDistance = Integer.valueOf(getIntent().getStringExtra(DISTANCE));
        server_ip = getIntent().getStringExtra(SERVER_IP);

        final Semaphore signal = new Semaphore(0);
        new HandlerThread("com.davisECS.virtualfrontview.ClientLatencyActivity"){
            @Override
            protected void onLooperPrepared() {
                mHandler = new Handler();
                signal.release();
            }
        }.start();
        signal.acquireUninterruptibly();
        mSurface = (SurfaceView) findViewById(R.id.hidden_surface);
        mSession = SessionBuilder.getInstance()
                .setCallback(this)
                .setContext(getApplicationContext())
                .setSurfaceView(mSurface)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 20, 500000))
                .build();
        mSurface.getHolder().addCallback(this);
        updateUiLog("Building session...");
        ConnectToServer connect = new ConnectToServer();
        connect.execute();
    }



    private class ConnectToServer extends AsyncTask<Void, String, Void> {

        @Override
        protected void onProgressUpdate(String... values) {
            updateUiLog(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCSeq = 0;
            try {
                mSocket = new Socket(server_ip, server_port);
                mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
            } catch (IOException e) {
                publishProgress("Error setting up network interfaces!");
                return null;
            }
            mMainHandler = new Handler(Looper.getMainLooper());
            mState = STATE_STOPPED;
            mHandler.post(new Runnable () {
                @Override
                public void run() {
                    if (mState != STATE_STOPPED) return;
                    mState = STATE_STARTING;
                    publishProgress("Configuring...");
                    try {
                        mSession.syncConfigure();
                    } catch (Exception e) {
                        publishProgress("Error configuring session. Try again.");
                        mState = STATE_STOPPED;
                        return;
                    }
                    publishProgress("Sending DESCRIBE request to server.");
                    Response response = sendDescribeRequest();
                    publishProgress("Retrieving response...");
                    publishProgress(parseResponse(response));
                  /*
                    publishProgress("Sending ANNOUNCE request to server.");
                    Response response = sendRequestAnnounce();
                    publishProgress("Server response ---");
                    publishProgress("Status: " + response.status);
                    Iterator iter = response.headers.entrySet().iterator();
                    publishProgress("Server response header ---");
                    while (iter.hasNext()){
                        Map.Entry pair = (Map.Entry)iter.next();
                        publishProgress(pair.getKey() + ": " + pair.getValue());
                    }
                    publishProgress("Sending SETUP request to server.");
                    response = sendRequestSetup();
                    publishProgress("Server response ---");
                    publishProgress("Status: " + response.status);
                    iter = response.headers.entrySet().iterator();
                    publishProgress("Server response header ---");
                    while (iter.hasNext()){
                        Map.Entry pair = (Map.Entry)iter.next();
                        publishProgress(pair.getKey() + ": " + pair.getValue());
                    }
                }*/
                }
            });
        return null;
        }
    }


    // Helper method
    void updateUiLog(String text){
        sb.append(text + '\n');
        mPacketDetailsText.setText(sb.toString());
    }

    private String parseResponse(Response response){
        StringBuilder tempSB = new StringBuilder();
        tempSB.append("Server response ---\n");
        tempSB.append("Status: " + response.status + '\n');
        Iterator iter = response.headers.entrySet().iterator();
        tempSB.append("Server response header ---");
        while (iter.hasNext()){
            Map.Entry pair = (Map.Entry)iter.next();
            tempSB.append(pair.getKey() + ": " + pair.getValue()+'\n');
        }
        return tempSB.toString();
    }

    public Response sendDescribeRequest(){
        String request = "DESCRIBE rtsp://"+server_ip+":"+server_port+"/"+" RTSP/1.0\r\n"
                + "CSeq: " + (++mCSeq) + "\r\n\r\n";
        try {
            mOutputStream.write(request.getBytes("UTF-8"));
            mOutputStream.flush();
            Response response = Response.parseResponse(mBufferedReader);
            return response;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }


    public Response sendRequestAnnounce(){
        String body = mSession.getSessionDescription();
        String request = "ANNOUNCE rtsp://"+server_ip+":"+server_port+" RTSP/1.0\r\n" +
                "CSeq: " + (++mCSeq) + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Content-Type: application/sdp \r\n\r\n" +
                body;
        try {
            mOutputStream.write(request.getBytes("UTF-8"));
            mOutputStream.flush();
            Response response = Response.parseResponse(mBufferedReader);
            setSessionID(response);
            return response;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    public Response sendRequestSetup(){
        String params = "UDP;unicast;client_port="+(5000+2)+"-"+(5000+3)+";mode=receive";
        String request = "SETUP rtsp://"+server_ip+":"+server_port+"/"+"/trackID="+1+" RTSP/1.0\r\n" +
                "Transport: RTP/AVP/"+params+"\r\n" +
                addHeaders();

        Log.i(TAG,request.substring(0, request.indexOf("\r\n")));

        try {
            mOutputStream.write(request.getBytes("UTF-8"));
            mOutputStream.flush();
            Response response = Response.parseResponse(mBufferedReader);
            return response;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private String addHeaders() {
        return "CSeq: " + (++mCSeq) + "\r\n" +
                "Content-Length: 0\r\n" +
                "Session: " + mSessionID + "\r\n";
    }

    private void setSessionID(Response response){
        try {
            Matcher m = Response.rexegSession.matcher(response.headers.get("session"));
            m.find();
            mSessionID = m.group(1);
        }catch (Exception e){
            Log.e(TAG, "Invalid response from server while getting session ID.");
        }
    }

    @Override
    public void onBitrateUpdate(long bitrate) {

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        switch(reason){
            case Session.ERROR_INVALID_SURFACE:
                updateUiLog("Error with session: Invalid surface");
                break;
            case Session.ERROR_OTHER:
                updateUiLog("Error with session: Other..");
                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
                updateUiLog("Error with session: Config unsupported.");
                break;
            case Session.ERROR_UNKNOWN_HOST:
                updateUiLog("Error with session: Unknown host.");
                break;
            case Session.ERROR_STORAGE_NOT_READY:
                updateUiLog("Error with session: Storage not ready.");
                break;
        }
    }

    @Override
    public void onPreviewStarted() {
        updateUiLog("Session preview started.");
    }

    @Override
    public void onSessionConfigured() {
        updateUiLog("Session configured.");
        mSession.start();
    }

    @Override
    public void onSessionStarted() {
        updateUiLog("Session started.");
    }

    @Override
    public void onSessionStopped() {
        updateUiLog("Session stopped.");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSession.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSession.stop();
    }

    // Used to parse RTSP info
    static class Response {

        // Parses method & uri
        public static final Pattern regexStatus = Pattern.compile("RTSP/\\d.\\d (\\d+) (\\w+)",Pattern.CASE_INSENSITIVE);
        // Parses a request header
        public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)",Pattern.CASE_INSENSITIVE);
        // Parses a WWW-Authenticate header
        public static final Pattern rexegAuthenticate = Pattern.compile("realm=\"(.+)\",\\s+nonce=\"(\\w+)\"",Pattern.CASE_INSENSITIVE);
        // Parses a Session header
        public static final Pattern rexegSession = Pattern.compile("(\\d+)",Pattern.CASE_INSENSITIVE);
        // Parses a Transport header
        public static final Pattern rexegTransport = Pattern.compile("client_port=(\\d+)-(\\d+).+server_port=(\\d+)-(\\d+)",Pattern.CASE_INSENSITIVE);


        public int status;
        public HashMap<String,String> headers = new HashMap<String,String>();

        /** Parse the method, URI & headers of a RTSP request */
        public static Response parseResponse(BufferedReader input) throws IOException, IllegalStateException, SocketException {
            Response response = new Response();
            String line;
            Matcher matcher;
            // Parsing request method & URI
            if ((line = input.readLine())==null) throw new SocketException("Connection lost");
            matcher = regexStatus.matcher(line);
            matcher.find();
            response.status = Integer.parseInt(matcher.group(1));

            // Parsing headers of the request
            while ( (line = input.readLine()) != null) {
                //Log.e(TAG,"l: "+line.length()+", c: "+line);
                if (line.length()>3) {
                    matcher = rexegHeader.matcher(line);
                    matcher.find();
                    response.headers.put(matcher.group(1).toLowerCase(Locale.US),matcher.group(2));
                } else {
                    break;
                }
            }
            if (line==null) throw new SocketException("Connection lost");

            Log.d(TAG, "Response from server: "+response.status);

            return response;
        }
    }
}
