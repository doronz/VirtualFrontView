package com.davisECS.virtualfrontview;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.majorkernelpanic.streaming.rtsp.RtspServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Streaming the camera output of a host device (server) to a connected peer
 * (client), using the library LibStreaming.
 */
public class MainActivity extends Activity implements ChoosePeerDialogFragment.ChoosePeerDialogListener {

	private final static String TAG = "VirtualFrontView";

	private static Button mServerButton;
	private static Button mClientButton;
    private static Button mConnectButton;
    private static Button mStartDiscoveryButton;
    private static Button mStopDiscoveryButton;

	private Spinner mBitrateSpinner;
	private Spinner mResolutionSpinner;

    private EditText mEnterIp;
    private EditText mEnterDistance;

    private TextView mGroupOwnerIp;
    private TextView mWifiStatusText;
    private TextView mDiscoveryText;
    private TextView mInfoText;

	private static final String SERVER_IP = "server ip";
	private static final String BITRATE = "bitrate";
	private static final String RESOLUTION = "resolution";
    private static final String DISTANCE = "distance";

    // Wifi-Direct P2P stuff
    private WifiP2pInfo mWifiInfo;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private static WifiP2pManager mManager;
    private static Channel mChannel;
    private static BroadcastReceiver mReceiver;
    private PeerListListener mPeerListener;
    private static WifiP2pManager.ChannelListener mChannelListener;
    private static List mPeers = new ArrayList();
    private static boolean mIsPeerChosen = false;
    private static boolean mIsDiscovering = false;

    String mConnectionStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Initialize UI
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get references to XML objects
		mEnterIp = (EditText) findViewById(R.id.edit_IP);
        mEnterDistance = (EditText) findViewById(R.id.edit_distance);

        mGroupOwnerIp       = (TextView) findViewById(R.id.group_owner_ip);
        mWifiStatusText     = (TextView) findViewById(R.id.wifi_status_text);
        mDiscoveryText      = (TextView) findViewById(R.id.discovery_status_text);
        mInfoText           = (TextView) findViewById(R.id.info_text);

        mServerButton           = (Button) findViewById(R.id.server_button);
        mClientButton           = (Button) findViewById(R.id.client_button);
        mConnectButton          = (Button) findViewById(R.id.wifi_connect_button);
        mStartDiscoveryButton   = (Button) findViewById(R.id.wifi_discoverable_button);
        mStopDiscoveryButton    = (Button) findViewById(R.id.stop_discovery_button);

        mBitrateSpinner     = (Spinner) findViewById(R.id.bitrate_spinner);
        mResolutionSpinner  = (Spinner) findViewById(R.id.resolution_spinner);

        // Initialize manager, channel, receiver and peer list listener
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), mChannelListener);

        // Notified when peer list is updated and lets user choose a peer.
        mPeerListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                // Clear old peers
                mPeers.clear();
                mPeers.addAll(peerList.getDeviceList());
                if (mPeers.size() > 0 && !mIsPeerChosen) {
                    FragmentManager fm = getFragmentManager();
                    ChoosePeerDialogFragment choosePeer = ChoosePeerDialogFragment.newInstance(mPeers);
                    choosePeer.show(fm, null);
                }
            }
        };

        // Tell intent filter to listen to appropriate Wi-Fi states
        configureIntentFilter();

        // Wire up the buttons and spinners
        initButtons();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        // Attempt to get root privileges
        if(!Shell.su()) {
            makeToast("NO ROOT!");
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListener);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
	protected void onPause() {
		super.onPause();
        unregisterReceiver(mReceiver);
		// Stop RTSP server if it is running
		getApplicationContext().stopService(new Intent(this, RtspServer.class));
	}

    private void initButtons(){
        // Set what happens when buttons are clicked
        mServerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Opens server activity
                Intent launchServer = new Intent(getApplicationContext(),
                        ServerActivity.class);
                launchServer.putExtra(BITRATE, (String) mBitrateSpinner.getSelectedItem());
                launchServer.putExtra(RESOLUTION, (String) mResolutionSpinner.getSelectedItem());
                launchServer.putExtra(DISTANCE, mEnterDistance.getText().toString());
                startActivity(launchServer);
            }
        });
        mClientButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String ip = mEnterIp.getText().toString();
                if (ip.length() < 7) // Not really validating input too strictly..
                    makeToast(getApplicationContext(),"Please enter a valid IP!" );
                else {
                    Intent launchClient = new Intent(MainActivity.this,
                            ClientActivity.class);
                    launchClient.putExtra(SERVER_IP, ip);
                    launchClient.putExtra(DISTANCE, mEnterDistance.getText().toString());
                    if (mEnterDistance.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter a distance.",
                                Toast.LENGTH_LONG).show();
                    }else {
                        startActivity(launchClient);
                    }
                }
            }
        });
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConnectButton.getText().equals("Connect")) {
                    if (mManager != null) {
                        mManager.requestPeers(mChannel, mPeerListener);
                        setInfo("Requesting peers.");
                    }
                }
                else if (mConnectButton.getText().equals("Disconnect")) {
                        disconnect();
                        setInfo("Disconnecting...");
                }
                else {
                    setInfo("Unable to connect/disconnect.");
                }
            }
        });
        mStartDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsDiscovering) {
                    initPeerDiscovery();
                }
                else{
                    makeToast("Already discovering!");
                }

            }
        });
        mStopDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mIsDiscovering = false;
                        setDiscoveryStatus("Discovery stopped.", false);
                    }

                    @Override
                    public void onFailure(int reason) {
                        setDiscoveryStatus("Unable to stop discovery.", true);
                    }
                });
            }
        });
    }


    private void initPeerDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                makeToast(getApplicationContext(), "Looking for peers!");
                mIsDiscovering = true;
                mConnectButton.setEnabled(true); // Disabled until you become discoverable
            }

            @Override
            public void onFailure(int i) {
                mIsDiscovering = false;
                makeToast(getApplicationContext(), "Error! Can't discover peers.");
                mConnectButton.setEnabled(false); // Disabled until you become discoverable
            }
        });
    }

    private void configureIntentFilter() {
        // Tell intentFilter to listen to the following Wi-Fi states.

        //  Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // Indicates whether discovery has started or stopped
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }

    public void setConnectionStatus(String status) {
        mConnectionStatus = status;
        mWifiStatusText.setText("Connection Status: " + status);
        if (status.equals("Connected")) {
            mWifiStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            mConnectButton.setText("Disconnect");
        }
        else {
            mWifiStatusText.setTextColor(getResources().getColor(android.R.color.black));
            mConnectButton.setText("Connect");
        }
    }

    public void setDiscoveryStatus(String status, boolean enableStopDiscovery){
        mDiscoveryText.setText("Discovery Status: " + status);
        mStopDiscoveryButton.setEnabled(enableStopDiscovery);
    }

    public void setInfo(String info){
        mInfoText.setText("INFO: " + info);
    }

    private void makeToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private void makeToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPeerChosen(final WifiP2pDevice peer) {
        // Connect to the peer
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mIsPeerChosen = true;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                makeToast("Connecting to " + peer.deviceName + "...");
                setInfo("Connecting to " + peer.deviceName + "...");
            }

            @Override
            public void onFailure(int i) {
                StringBuilder sb = new StringBuilder();
                sb.append("Connection to " + peer.deviceName + " failed: ");
                switch (i){
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        sb.append("P2P Unsupported");
                        break;
                    case WifiP2pManager.BUSY:
                        sb.append("Framework is busy");
                        break;
                    case WifiP2pManager.ERROR:
                        sb.append("Internal error");
                        break;
                    default:
                        sb.append("Unknown error");
                        break;
                }
                setInfo(sb.toString());
                mIsPeerChosen = false;
            }
        });
    }

    public void updateGroupIP() {
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (null != info.groupOwnerAddress) {
                    mEnterIp.setText(info.groupOwnerAddress.getHostAddress());
                    mConnectButton.setEnabled(true);
                    if (info.isGroupOwner) {
                        mGroupOwnerIp.setText("You are the group owner!");
                        mClientButton.setEnabled(false);
                        mServerButton.setEnabled(true);
                        mEnterIp.setEnabled(false);
                        mBitrateSpinner.setEnabled(true);
                        mResolutionSpinner.setEnabled(true);
                        mDiscoveryText.setText("");
                        mInfoText.setText("");
                    }
                    else {
                        mConnectButton.setEnabled(false);
                        mGroupOwnerIp.setText("Group owner IP: " + info.groupOwnerAddress.getHostAddress());
                        mClientButton.setEnabled(true);
                        mBitrateSpinner.setEnabled(false);
                        mResolutionSpinner.setEnabled(false);
                        mServerButton.setEnabled(false);
                        mEnterIp.setEnabled(true);
                        mDiscoveryText.setText("");
                        mInfoText.setText("");
                    }
                }
            }
        });
    }

    private void disconnect() {
        if (mManager != null && mChannel != null){
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null && group.isGroupOwner()){
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                makeToast("Attempting to disconnect.");
                                setInfo("Attempting to disconnect.");
                            }

                            @Override
                            public void onFailure(int reason) {
                                makeToast("Unable to disconnect, try again.");
                                setInfo("Unable to disconnect, try again.");
                            }
                        });
                    }
                }
            });
        }
    }

    public void setIsPeerChosen (boolean b){
        mIsPeerChosen = b;
    }
}