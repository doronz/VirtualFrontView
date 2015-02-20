package com.davisECS.virtualfrontview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;

public class ServerActivity extends Activity implements Session.Callback,
		SurfaceHolder.Callback {

	private final static String TAG = "VirtualFrontView";
	private static final String BITRATE = "bitrate";
	private static final String RESOLUTION = "resolution";
    private static final String DISTANCE = "distance";

    private String mDistance;

    private static final int FPS = 30; // Highest recommended fps
	
	private SurfaceView mSurfaceView;
    private Chronometer mChrono;

    private int mBitrate; // Video stream bitrate

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private int mResX; // Video stream resolution width
    private int mResY; // Video stream resolution length

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);

        // Screen stays on while in this activity
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mChrono = (Chronometer) findViewById(R.id.chrono);
        mChrono.setFormat("SS");

		// Sets the port of the RTSP server to 8988
		Editor editor = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).edit();
		editor.putString(RtspServer.KEY_PORT, String.valueOf(8988));
		editor.commit();

        // Handle user selected bitrate, resolution
        updateBitrate();
        updateResolution();

        // Get distance for tests
        mDistance = getIntent().getStringExtra(DISTANCE);

        // Configures the session
		SessionBuilder.getInstance()
                .setCallback(this)
                .setSurfaceView(mSurfaceView)
				.setPreviewOrientation(0)
                .setContext(this)
				.setVideoQuality(new VideoQuality(mResX, mResY, FPS, mBitrate))
				.setAudioEncoder(SessionBuilder.AUDIO_NONE)
				.setVideoEncoder(SessionBuilder.VIDEO_H264);

        // Starts the RTSP server
        this.startService(new Intent(this,RtspServer.class));
        TestResults.RunTest("start", "sender");
        mChrono.start();

        // Force use of Media Codec API's Surface-to-buffer stream
        //mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIACODEC_API_2); // Doesn't seem to work
        // Force use of Media Codec API Buffer-to-buffer stream
        //mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIACODEC_API);

	}

    private void updateResolution(){
        String resolution = getIntent().getStringExtra(RESOLUTION);
        // This version of Java doesn't support switch(String)
        if (resolution.equals("320x240")) {
            mResX = 320;
            mResY = 240;
        } else if (resolution.equals("480x320")) {
            mResX = 480;
            mResY = 320;
        } else if (resolution.equals("640x480")) {
            mResX = 640;
            mResY = 480;
        } else if (resolution.equals("800x480")) {
            mResX = 800;
            mResY = 480;
        } else if (resolution.equals("1280x720")) {
            mResX = 1280;
            mResY = 720;
        } else {
            mResX = 176;
            mResY = 144;
        }

        Toast.makeText(this, "Resolution: " + mResX + "x" + mResY + ", Bitrate: "
                + mBitrate, Toast.LENGTH_LONG).show();
    }

    // Retrieves user selected bitrate from main activity
    private void updateBitrate(){
        // Get bitrate
        mBitrate = Integer.valueOf(getIntent().getStringExtra(BITRATE));
        if (mBitrate < 100000)
            mBitrate = 100000;
    }


	@Override
	protected void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Stop RTSP server if it is running
		getApplicationContext().stopService(new Intent(this, RtspServer.class));
        TestResults.RunTest("stop", "sender", mDistance);
        mChrono.stop();
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}




	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {


	}

	@Override
	public void onBitrareUpdate(long bitrate) {

	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {

	}

	@Override
	public void onPreviewStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionConfigured() {

	}

	@Override
	public void onSessionStarted() {

	}

	@Override
	public void onSessionStopped() {

	}

}
