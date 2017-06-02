package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.receiver.HeadsetReceiver;
import com.itutorgroup.tutorchat.phone.ui.popup.AudioSensorPopWindow;
import com.itutorgroup.tutorchat.phone.utils.voice.MediaManager;

import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by tom_zxzhang on 2016/10/19.
 */
public class AudioSensorManager {


    private static AudioSensorManager sInstance;
    private AudioManager audioManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock = null;//电源锁
    private Sensor mProximiny; // 传感器实例
    private float f_proximiny;
    private SensorManager sensorManager;
    private AudioSensorPopWindow mPopWindow;
    private View mView;
    boolean isNormalState = false;


    public static AudioSensorManager getInstance() {
        if (sInstance == null) {
            synchronized (AudioSensorManager.class) {
                if (sInstance == null) {
                    sInstance = new AudioSensorManager();
                }
            }
        }
        return sInstance;
    }


    private AudioSensorManager() {
        audioManager = (AudioManager) LPApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);
        sensorManager = (SensorManager) LPApp.getInstance().getSystemService(Context.SENSOR_SERVICE);
        mProximiny = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        powerManager = (PowerManager) LPApp.getInstance().getSystemService(Context.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(32, "MyPower");
    }


    public void setFloatView(Context context, View view) {
        mPopWindow = new AudioSensorPopWindow((Activity) context);
        mView = view;
    }

    public void unRegister() {

        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        if (StringUtils.isNotBlank(sensorManager) && StringUtils.isNotBlank(mSeneorEventListener)) {
            sensorManager.unregisterListener(mSeneorEventListener);
        }
        if (StringUtils.isNotBlank(wakeLock) && wakeLock.isHeld()) {
            wakeLock.release();
        }

    }


    public void release() {
        mPopWindow = null;
        mView = null;
    }


    private void registerEarPhoneReceiver() {
        HeadsetReceiver receiver = new HeadsetReceiver();
//        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        //       filter.addAction("android.intent.action.HEADSET_PLUG");
        LPApp.getInstance().registerReceiver(receiver, filter);
    }


    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!MediaManager.isPlaying()) {
                        MediaManager.resume();
                    }
                    MediaManager.setVolume(1,1);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    if (MediaManager.isPlaying())
                        MediaManager.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (MediaManager.isPlaying())
                        MediaManager.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (MediaManager.isPlaying()) {
                        MediaManager.setVolume(1,1);
                    }
                    break;
            }
        }
    };


    public void register() {
        sensorManager.registerListener(mSeneorEventListener, mProximiny, SensorManager.SENSOR_DELAY_GAME);
        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        registerEarPhoneReceiver();
        if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
            showPoupWindow(LPApp.getInstance().getString(R.string.voice_current_in_model_call));
        }

    }


    private SensorEventListener mSeneorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            f_proximiny = event.values[0];
            if (f_proximiny == mProximiny.getMaximumRange()) {
                if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                    if (isNormalState) {
                        changeToSpeaker();
                        isNormalState = false;
                        if (wakeLock.isHeld()) {
                            return;
                        } else {
                            wakeLock.acquire();
                        }
                        showPoupWindow(LPApp.getInstance().getString(R.string.voice_switch_to_normal_model));
                    }
                }
            } else {
                if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                    showPoupWindow(LPApp.getInstance().getString(R.string.voice_current_in_model_call));
                } else {
                    isNormalState = true;
                    changeToReceiver();
                    releaseWakeLock();
                }


            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    private void releaseWakeLock() {
        if (StringUtils.isNotBlank(wakeLock) && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver() {
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    public void showPoupWindow(String tip) {
        mPopWindow.setText(tip);
        mPopWindow.showAsDropDown(mView);
        LPApp.getInstance().mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
            }
        }, 3000);
    }


}
