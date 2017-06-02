package com.itutorgroup.tutorchat.phone.utils.ui;

import android.content.Context;
import android.widget.Toast;

import com.itutorgroup.tutorchat.phone.app.LPApp;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class ToastUtil {
    private ToastUtil() {

    }

    private static Toast mToast;
    private static final Context mContext = LPApp.getInstance();

    public static void show(int resId) {
        show(mContext.getString(resId));
    }

    public static void show(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}
