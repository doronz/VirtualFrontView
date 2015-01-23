package com.davisECS.virtualfrontview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class LogUtil {

	/*
	 * Writes a log to specified file, with the specified message. 
	 * Must add startTime by calling System.currentTimeMillis() before
	 * function call and storing it in variable to be passed in.
	 */
	static void logTime(long startTime, String filename, String message) {
		Log.d("VirtualFrontView", message);
		String timedMessage = "";
		long currTime = System.currentTimeMillis();
		long elapsedTime = currTime - startTime;
		timedMessage = message + " - " + elapsedTime;
		File logFile = new File("sdcard/" + filename);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(timedMessage);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
