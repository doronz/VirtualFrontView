package com.davisECS.virtualfrontview;

import net.majorkernelpanic.streaming.rtsp.RtspServer;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Streaming the camera output of a host device (server) to a connected peer
 * (client), using the library LibStreaming.
 */
public class MainActivity extends Activity {

	private final static String TAG = "VirtualFrontView";
	private Button mServerButton;
	private Button mClientButton;
	private Spinner mBitrateSpinner;
	private Spinner mResolutionSpinner;
	private EditText mEnterIp;
	private TextView mUserIp;
	private static String mVideoIP;
	private static final String SERVER_IP = "server ip";
	private static final String BITRATE = "bitrate";
	private static final String RESOLUTION = "resolution";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		String ip = IpUtility.getIPAddress(true);
		mVideoIP = "rtsp://" + ip + ":8988";
		Log.i(TAG, "IP: " + mVideoIP);

		mEnterIp = (EditText) findViewById(R.id.ip_text);
		mUserIp = (TextView) findViewById(R.id.user_ip);
		mEnterIp.setText("192.168.49.");
		mUserIp.setText("Your IP: " + ip);
		// Get button references
		mServerButton = (Button) findViewById(R.id.server_button);
		mClientButton = (Button) findViewById(R.id.client_button);
		
		mBitrateSpinner = (Spinner) findViewById(R.id.bitrate_spinner);
		mResolutionSpinner = (Spinner) findViewById(R.id.resolution_spinner);

		// Set what happens when buttons are clicked
		mServerButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Opens server activity
				
				Intent launchServer = new Intent(getApplicationContext(),
						ServerActivity.class);
				launchServer.putExtra(BITRATE, (String) mBitrateSpinner.getSelectedItem());
				launchServer.putExtra(RESOLUTION, (String) mResolutionSpinner.getSelectedItem());
				startActivity(launchServer);
			}
		});
		mClientButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String ip = mEnterIp.getText().toString();
				if (ip.length() < 7)
					Toast.makeText(getApplicationContext(),
							"Please enter a valid IP!", Toast.LENGTH_SHORT)
							.show();
				else {
					Intent launchClient = new Intent(MainActivity.this,
							ClientActivity.class);
					launchClient.putExtra(SERVER_IP, ip);
					startActivity(launchClient);
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Stop RTSP server if it is running
		getApplicationContext().stopService(new Intent(this, RtspServer.class));
	}

}