package com.itutorgroup.tutorchat.phone.utils.voice;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.utils.permission.PermissionsManager;

import java.lang.ref.WeakReference;

public class AudioRecorderButton extends TextView {

    private static final int STATE_NORMAL = 1;// 默认的状态
    private static final int STATE_RECORDING = 2;// 正在录音
    private static final int STATE_WANT_TO_CANCEL = 3;// 希望取消

    private int mCurrentState = STATE_NORMAL; // 当前的状态
    private boolean isRecording = false;// 已经开始录音

    private static final int DISTANCE_Y_CANCEL = 50;

    private DialogManager mDialogManager;
    private AudioManager mAudioManager;

    private float mTime;
    // 是否触发longClick
    private boolean mReady;
    private static final int MAX_TIME = 60; //S
    public static final int WARNING_TIME = 5;
    public static int SHOW_TIME = WARNING_TIME;
    private static final int MSG_AUDIO_PREPARED = 0x110;
    private static final int MSG_VOICE_CHANGED = 0x111;
    private static final int MSG_DIALOG_DIMISS = 0x112;
    private static final int MSG_TIME_WILL_OVER = 0x113;
    private static final int MSG_TIME_OVERD = 0x114;
    private static final int MSG_TIME_LOSE_FOCUS = 0x115;
    private static final int MSG_SHOW_MISS_PERMISSION_DIALOG = 0x116;
    private Context mContext;
    private boolean mAudioFocus;
    private android.media.AudioManager systemAudioManager;
    private boolean isHaveSystemDialog = false;

    /*
     * 获取音量大小的线程
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {

        public void run() {
            while (isRecording) {
                try {
                    if (mTime >= MAX_TIME - WARNING_TIME) {
                        Thread.sleep(1000);
                        mTime += 1.0f;
                        if (mTime - 1 >= MAX_TIME) {
                            mTime = MAX_TIME;
                            mHandler.sendEmptyMessage(MSG_TIME_OVERD);
                        } else {
                            mHandler.sendEmptyMessage(MSG_TIME_WILL_OVER);
                        }
                    } else {
                        Thread.sleep(100);
                        mTime += 0.1f;
                        mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private MyHandler mHandler = new MyHandler((Activity) mContext) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    // 显示對話框在开始录音以后
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    // 开启一个线程
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;

                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;

                case MSG_DIALOG_DIMISS:
                    mDialogManager.dimissDialog();
                    break;

                case MSG_TIME_WILL_OVER:
                    mDialogManager.showWillEndTime(SHOW_TIME);
                    SHOW_TIME--;
                    break;

                case MSG_TIME_OVERD:
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    break;

                case MSG_TIME_LOSE_FOCUS:
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    reset();
                    break;

                case MSG_SHOW_MISS_PERMISSION_DIALOG:
                    PermissionsManager.getInstance().showMissingPermissionDialog(mContext, mContext.getString(R.string.str_record));
                    break;

            }

        }
    };

    /**
     * 以下2个方法是构造方法
     */
    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDialogManager = new DialogManager(mContext);
        String dir = Constant.RECORD_DIR;
        mAudioManager = AudioManager.getInstance(dir);
        systemAudioManager = (android.media.AudioManager) LPApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setOnAudioStateListener(new AudioManager.AudioStateListener() {

            public void wellPrepared() {
                mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
            }
        });


        setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                new Thread(prePareRunnable).start();
                return false;
            }
        });
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mAudioManager != null){
            mAudioManager.setOnAudioStateListener(null);
        }
    }

    Runnable prePareRunnable = new Runnable() {
        @Override
        public void run() {

            boolean lackPermissions = PermissionsManager.getInstance().checkPermissions(PermissionsManager.PERMISSION_GROUP_AUDIO);
            if(lackPermissions){
                mHandler.sendEmptyMessage(MSG_SHOW_MISS_PERMISSION_DIALOG);
            }else{
                if(!isHaveSystemDialog){
                    requestAudioFocus();
                    mReady = true;
                    SHOW_TIME = WARNING_TIME;
                    mAudioManager.prepareAudio();
                }

            }

        }
    };

    android.media.AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new android.media.AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {

                case android.media.AudioManager.AUDIOFOCUS_GAIN:
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    mAudioFocus = true;
                    requestAudioFocus();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS:
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mAudioFocus = false;
                    abandonAudioFocus();
                    break;

                default:
                    break;

            }

        }

    };


    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    /**
     * 录音完成后的回调
     */
    public interface AudioFinishRecorderListener {
        void onFinish(float seconds, String filePath);
    }

    private AudioFinishRecorderListener audioFinishRecorderListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        audioFinishRecorderListener = listener;
    }



    /**
     * 屏幕的触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();// 获得x轴坐标
        int y = (int) event.getY();// 获得y轴坐标
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isHaveSystemDialog = false;
                changeState(STATE_RECORDING);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                    // 如果想要取消，根据x,y的坐标看是否需要取消
                    if (wantToCancle(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mReady) {
                    reset();
                    isHaveSystemDialog = true;
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime < 0.6f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1000);// 延迟显示对话框
                } else if (mCurrentState == STATE_RECORDING) { // 正在录音的时候，结束
                    mDialogManager.dimissDialog();
                    mAudioManager.release();
                    if (audioFinishRecorderListener != null) {
                        audioFinishRecorderListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
                    }

                } else if (mCurrentState == STATE_WANT_TO_CANCEL) { // 想要取消
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                mHandler.sendEmptyMessageDelayed(MSG_TIME_LOSE_FOCUS, 200);// 延迟显示对话框
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 恢复状态及标志位
     */
    private void reset() {
        isRecording = false;
        mTime = 0;
        mReady = false;
        abandonAudioFocus();
        changeState(STATE_NORMAL);
    }

    private boolean wantToCancle(int x, int y) {
        if (x < 0 || x > getWidth()) { // 超过按钮的宽度
            return true;
        }
        // 超过按钮的高度
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }

        return false;
    }

    /**
     * 改变
     */
    private void changeState(int state) {
        if (mCurrentState != state) {
            mCurrentState = state;
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.str_recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_recording);
                    mDialogManager.reset();
                    if (isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recorder_recording);
                    setText(R.string.str_recorder_want_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
        }
    }


    private void requestAudioFocus() {
        if (!mAudioFocus) {
            int result = systemAudioManager.requestAudioFocus(onAudioFocusChangeListener,
                    android.media.AudioManager.STREAM_MUSIC, // Use the music stream.
                    android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (result == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocus = true;
            } else {

            }
        }
    }

    private void abandonAudioFocus() {
        if (mAudioFocus) {
            systemAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
            mAudioFocus = false;
        }
    }





}
