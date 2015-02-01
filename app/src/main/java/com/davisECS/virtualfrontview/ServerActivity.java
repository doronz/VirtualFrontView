package com.davisECS.virtualfrontview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import net.majorkernelpanic.streaming.MediaStream;
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
	
	private SurfaceView mSurfaceView;
    private Session mSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface);

		// Sets the port of the RTSP server to 8988
		Editor editor = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).edit();
		editor.putString(RtspServer.KEY_PORT, String.valueOf(8988));
		editor.commit();

		// Get bitrate
		int bitrate = Integer.valueOf(getIntent().getStringExtra(BITRATE));
		if (bitrate < 100000)
			bitrate = 100000;

		// Get resolution

		String resolution = getIntent().getStringExtra(RESOLUTION);
		int resX;
		int resY;
        // TODO: Should probably be a switch statement
		if (resolution.equals("320x240")) {
            resX = 320;
            resY = 240;
        } else if (resolution.equals("480x320")) {
			resX = 480;
			resY = 320;
		} else if (resolution.equals("640x480")) {
			resX = 640;
			resY = 480;
		} else if (resolution.equals("800x480")) {
            resX = 800;
            resY = 480;
        } else if (resolution.equals("1280x720")) {
            resX = 1280;
            resY = 720;
		} else {
			resX = 176;
			resY = 144;
		}


		Toast.makeText(this, "Resolution: " + resX + "x" + resY + ", Bitrate: "
				+ bitrate, Toast.LENGTH_LONG).show();

        /*try {
            RunAsRoot();
        } catch (IOException e) {
            Toast.makeText(this, "No root access.", Toast.LENGTH_LONG).show();
            Log.d("VirtualFrontView", "No root.");
        }*/

        // Configures the session
		mSession = SessionBuilder.getInstance().setSurfaceView(mSurfaceView)
				.setPreviewOrientation(0).setContext(this)
				.setVideoQuality(new VideoQuality(resX, resY, 20, bitrate))
				.setAudioEncoder(SessionBuilder.AUDIO_NONE)
				.setVideoEncoder(SessionBuilder.VIDEO_H264).build();

        mSurfaceView.getHolder().addCallback(this);

        // Force use of Media Codec API's Surface-to-buffer stream
        mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIACODEC_API_2);




		// Starts the RTSP server
		getApplicationContext().startService(
				new Intent(getApplicationContext(), RtspServer.class));
	}

	@Override
	protected void onPause() {
		super.onPause();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Stop RTSP server if it is running
		getApplicationContext().stopService(new Intent(this, RtspServer.class));

	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}




	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // Starts the preview of the Camera
        //mSession.startPreview(); // Tends to start the preview before the camera is ready
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        // Stops the streaming session
        mSession.stop();

	}

	@Override
	public void onBitrareUpdate(long bitrate) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreviewStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionConfigured() {

        mSession.start();
	}

	@Override
	public void onSessionStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStopped() {
		// TODO Auto-generated method stub

	}

}
