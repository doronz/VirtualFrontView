package com.davisECS.virtualfrontview;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

public class ServerActivity extends Activity implements Session.Callback,
		SurfaceHolder.Callback {

	private final static String TAG = "VirtualFrontView";
	private static final String BITRATE = "bitrate";
	private static final String RESOLUTION = "resolution";
	
	private SurfaceView mSurfaceView;

	// For client video playback
	MediaPlayer mediaPlayer;
	SurfaceHolder surfaceHolder;

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
		int resX = 176;
		int resY = 144;
		if (resolution.equals("320x240")) {
            resX = 320;
            resY = 240;
        } else if (resolution.equals("352x288")) {
			resX = 352;
			resY = 288;
		} else if (resolution.equals("528x432")) {
			resX = 528;
			resY = 432;
		} else if (resolution.equals("704x576")) {
			resX = 704;
			resY = 576;
		} else {
			resX = 176;
			resY = 144;
		}

		Toast.makeText(this, "Resolution: " + resX + "x" + resY + ", Bitrate: "
				+ bitrate, Toast.LENGTH_LONG).show();

		// Configures the SessionBuilder
		SessionBuilder.getInstance().setSurfaceView(mSurfaceView)
				.setPreviewOrientation(0).setContext(this)
				.setVideoQuality(new VideoQuality(resX, resY, 20, bitrate))
				.setAudioEncoder(SessionBuilder.AUDIO_NONE)
				.setVideoEncoder(SessionBuilder.VIDEO_H264);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
