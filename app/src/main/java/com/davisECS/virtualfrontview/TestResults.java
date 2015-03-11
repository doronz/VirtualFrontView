package com.davisECS.virtualfrontview;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for logging test results.
 */
public class TestResults {
    private static final String TAG = "VirtualFrontView";

    private static int mPacketsSent;
    private static int mPacketsOffset = 0; // The number of packets sent before we started counting

    public static boolean isCounting() {
        return mCounting;
    }

    private static boolean mCounting = false;


    public static void RunTest(String... params){
        TestRunner runner = new TestRunner();
        Log.d(TAG, "EXECUTING A TEST");
        runner.execute(params);
    }



    private static class TestRunner extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {
            if (params[0].equalsIgnoreCase("start")) { // If we want to start counting
                if (params[1].equalsIgnoreCase("sender")) {// If we are the sender
                    startCountingPackets(true);
                }
                else {
                    startCountingPackets(false); // Count packets on receiver side
                }
            }
            else if (params[0].equalsIgnoreCase("stop")) {
                if (params[1].equalsIgnoreCase("sender")) {
                    recordPackets(true, params[2]);
                } else {
                    recordPackets(false, params[2]);
                }
            }
            return null;
        }
    }

    /**
     * Run this command when you are about to send the first packet. It gets the current packet count
     * and sets it as the offset value so that we can know how many packets were sent from this point
     * forward.
     * @param sender set to true if you want to get the transmitted packets, false if you want received
     */
    private static void startCountingPackets(boolean sender){
        // We do not want to reset the offset more than once while we are counting.
        if (!mCounting) {
            try {
                String output;
                String s = "-1";
                for (int i = 0; i < 3; i++) {
                    output = Shell.sudo("busybox ifconfig p2p-p2p0-" + i); // Try other interfaces
                    String[] lines = output.split("\n");
                    try {
                        if (sender) {
                            s = lines[5]; // TX info
                        } else {
                            s = lines[4]; // RX info
                        }
                        break; // If no error then we got a result.
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                Pattern p = Pattern.compile("([0-9]+)");
                Matcher m = p.matcher(s);
                if (m.find()) {
                    mPacketsOffset = Integer.parseInt(m.group());
                }
                mCounting = true;
            } catch (Exception e) {

            }
        }
    }

    /**
     * Run this command after the last packet was sent to the client. Make sure it's run once the
     * client stops accepting packets. You don't want to over-count the number of packets the client
     * was supposed to receive.
     * @param sender set to true if you want to get the transmitted packets, false if you want received
     */
    private static void recordPackets(boolean sender, String distance){
        if (isExternalStorageWritable()) {
            try {
                String output;
                String s = "-1";
                for (int i = 0; i < 3; i++) {
                    output = Shell.sudo("busybox ifconfig p2p-p2p0-" + i); // Try other interfaces
                    String[] lines = output.split("\n");
                    try {
                        if (sender) {
                            s = lines[5]; // TX info
                        } else {
                            s = lines[4]; // RX info
                        }
                        break; // If no error then we got a result.
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                mCounting = false;
                Pattern p = Pattern.compile("([0-9]+)");
                Matcher m = p.matcher(s);
                int sentPacketsNumber = 0;
                if (m.find()) {
                    sentPacketsNumber = Integer.parseInt(m.group());
                }
                mPacketsSent = sentPacketsNumber - mPacketsOffset;
                String[] fileOutput = new String[2];
                fileOutput[0] = String.valueOf(new Timestamp(System.currentTimeMillis()));
                fileOutput[1] = String.valueOf(mPacketsSent);
                recordResults(fileOutput, sender, distance);
            } catch (Shell.ShellException e) {
                e.printStackTrace();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Writes the results in a csv file with timestamp and packets sent count.
     * @param result
     * @param sender set to true if you want to get the transmitted packets, false if you want received
     */
    private static void recordResults(String[] result, boolean sender, String distance) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/VirtualFrontView");
        dir.mkdirs();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH");
        try {
            File file;
            if (sender)
                file = new File(dir, "Sent (" + sdf.format(date) + ").csv");
            else
                file = new File(dir, "Received (" + sdf.format(date) + ").csv");
            FileWriter writer = new FileWriter(file, true);
            writer.append(result[0] + "," + result[1] + "," + distance + '\n');
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
