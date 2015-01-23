package com.davisECS.virtualfrontview;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.majorkernelpanic.streaming.SessionBuilder;

public class ClientActivity extends Activity implements OnPreparedListener, OnErrorListener, OnInfoListener,
		SurfaceHolder.Callback, IVideoPlayer {

	MediaPlayer mMediaPlayer;

	SurfaceHolder mSurfaceHolder;
	SurfaceView mSurfaceView;

    //VLC Player
    private LibVLC libvlc;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    private static String mVideoIP = "";
	private static final String TAG = "VirtualFrontView";
	private static final String SERVER_IP = "server ip";
	private static long mTimeStarted;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mVideoIP = getIntent().getStringExtra(SERVER_IP);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (savedInstanceState == null) {

		}
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.client, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
//		// Create a new media player and set the listeners
//		mMediaPlayer = new MediaPlayer();
//		
//		try {
//			mMediaPlayer.setDataSource("rtsp://" + mVideoIP
//					+ ":8988");
//		} catch (IllegalArgumentException | SecurityException
//				| IllegalStateException | IOException e) {
//			e.printStackTrace();
//			Log.e(TAG, "MediaPlayer error!");
//		}
//		// mMediaPlayer.setDisplay(holder);
//		mMediaPlayer.setScreenOnWhilePlaying(true);
//		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//		mMediaPlayer.prepareAsync();
//		mMediaPlayer.setOnPreparedListener(this);

		super.onResume();
        try
        {
            createPlayer(mVideoIP);
        }
        catch(Exception e)
        {

        }
	}

	@Override
	protected void onPause() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		/*if (mMediaPlayer != null) {

			mMediaPlayer.release();
			mMediaPlayer = null;
		}*/
		super.onPause();
        try
        {
            releasePlayer();
        }
        catch(Exception e)
        {

        }
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
        try
        {
            releasePlayer();
        }
        catch(Exception e)
        {

        }
		finish();
	}


	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "Media started!");
		mMediaPlayer.start();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        /*try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setDataSource("rtsp://" + mVideoIP
					+ ":8988");
			mMediaPlayer.prepare();
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnInfoListener(this);
			mTimeStarted = System.currentTimeMillis();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }*/
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        if (libvlc != null)
            libvlc.attachSurface(mSurfaceHolder.getSurface(), this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		finish();
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		long timeEnded = System.currentTimeMillis();
		if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
			if (timeEnded - mTimeStarted >= 2500)
				finish();
		}
		return false;
	}

    private void createPlayer(String serverip) {
        releasePlayer();
        try {
            /*if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }*/

            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            libvlc.setNetworkCaching(350);
            LibVLC.restart(this);
            EventHandler.getInstance().addHandler(mHandler);
            mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            mSurfaceHolder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI("rtsp://" + serverip + ":8988")), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        libvlc.stop();
        libvlc.detachSurface();
        mSurfaceHolder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        /*if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);*/

        // force surface buffer size
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
                               int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<ClientActivity> mOwner;

        public MyHandler(ClientActivity owner) {
            mOwner = new WeakReference<ClientActivity>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            ClientActivity player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerPlaying:
                case EventHandler.MediaPlayerPaused:
                case EventHandler.MediaPlayerStopped:
                default:
                    break;
            }
        }
    }

}
