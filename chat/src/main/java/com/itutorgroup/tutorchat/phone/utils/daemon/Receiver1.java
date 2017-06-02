package com.itutorgroup.tutorchat.phone.utils.daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.itutorgroup.tutorchat.phone.utils.permission.platform.AutoStartUtil;

/**
 * DO NOT do anything in this Receiver!<br/>
 *
 * Created by Mars on 12/24/15.
 */
public class Receiver1 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AutoStartUtil.init(context);
    }
}
