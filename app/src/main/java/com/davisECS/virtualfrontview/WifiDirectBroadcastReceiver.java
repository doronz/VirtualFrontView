package com.davisECS.virtualfrontview;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * This class will be used to listen for changes to the System's Wi-Fi P2P state.
 */
public class WifiDirectBroadcastReceiver extends android.content.BroadcastReceiver {

    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    private PeerListListener mPeerListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity, PeerListListener peerListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.mPeerListener = peerListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mActivity.setConnectionStatus("Wifi-Direct is Enabled");
            } else {
                mActivity.setConnectionStatus("Wifi-Direct is Disabled");
                mActivity.setIsPeerChosen(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed.
            mActivity.setDiscoveryStatus("Peers updated.", true);
            //TODO : Need to fix stop discovery button.



        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Connection state changed!
            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            // Check if we connected or disconnected.
            if (networkState.isConnected()) {
                mActivity.setConnectionStatus("Connected");
                mActivity.updateGroupIP();
            }
            else {
                mActivity.setConnectionStatus("Disconnected");
                mManager.cancelConnect(mChannel, null);
                mActivity.setIsPeerChosen(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // This device's wifi state changed

        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)){
            // Peer discovery stopped or started
            int discovery = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            if (discovery == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
                mActivity.setDiscoveryStatus("Peer discovery started.", true);
            else if (discovery == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
                mActivity.setDiscoveryStatus("Peer discovery stopped.", false);

        }
    }
}
