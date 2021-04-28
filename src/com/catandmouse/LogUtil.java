package com.catandmouse;

import android.util.Log;

public class LogUtil {
    private static final String TAG = "CatAndMouse";
    private static final boolean D = CMConstants.isTestMode;
    
    public static void debug(String message) {
    	if (D) Log.d(TAG, message);
    }

    public static void error(String message) {
    	if (D) Log.e(TAG, message);
    }

    public static void info(String message) {
    	if (D) Log.i(TAG, message);
    }

    public static void verbose(String message) {
    	if (D) Log.v(TAG, message);
    }

    public static void warn(String message) {
    	if (D) Log.w(TAG, message);
    }

}
