package com.itutorgroup.tutorchat.phone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.itutorgroup.tutorchat.phone.utils.manager.AudioSensorManager;

/**
 * Created by tom_zxzhang on 2016/12/1.
 */
public class HeadsetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            //插入和拔出耳机会触发此广播
            case AudioManager.ACTION_HEADSET_PLUG:
                int state = intent.getIntExtra("state", 0);
                if (state == 1){
                    AudioSensorManager.getInstance().changeToHeadset();
                } else if (state == 0){
                    AudioSensorManager.getInstance().changeToSpeaker();
                }
                break;
            default:
                break;
        }
    }
}