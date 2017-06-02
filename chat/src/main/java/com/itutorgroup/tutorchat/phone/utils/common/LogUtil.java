package com.itutorgroup.tutorchat.phone.utils.common;

import android.util.Log;

import com.itutorgroup.tutorchat.phone.BuildConfig;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class LogUtil {

    private LogUtil() {

    }

    public static final boolean DEBUG = true; //!BuildConfig.BUILD_TYPE.equals("product");
    public static final String TAG = "vipabc";


    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void exception(Throwable e) {
        e("error: " + e.getMessage());
        if (DEBUG) {
            e.printStackTrace();
        }
    }
}
