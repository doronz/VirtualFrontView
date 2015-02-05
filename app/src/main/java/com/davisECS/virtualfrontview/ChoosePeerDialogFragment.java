package com.davisECS.virtualfrontview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by D on 2/3/2015.
 */
public class ChoosePeerDialogFragment extends DialogFragment {

    private List<WifiP2pDevice> mPeers;

    public static ChoosePeerDialogFragment newInstance(List peers) {
        ChoosePeerDialogFragment dialog = new ChoosePeerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("PEERS", (ArrayList<WifiP2pDevice>) peers);
        dialog.setArguments(args);
        return dialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int chosenPeer = -1;
        Bundle args = getArguments();
        mPeers = (ArrayList<WifiP2pDevice>) args.getSerializable("PEERS");
        final WifiP2pDeviceArrayAdapter peerAdapter = new WifiP2pDeviceArrayAdapter(getActivity(), mPeers);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a peer:")
                .setSingleChoiceItems(peerAdapter, chosenPeer, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ChoosePeerDialogListener activity = (ChoosePeerDialogListener) getActivity();
                        activity.onPeerChosen(peerAdapter.getItem(i));
                        dialogInterface.dismiss();
                    }

                });

        return builder.create();
    }

    public interface ChoosePeerDialogListener {
        void onPeerChosen(WifiP2pDevice peer);
    }
}
