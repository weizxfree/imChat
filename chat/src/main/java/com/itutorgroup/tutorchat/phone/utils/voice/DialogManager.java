package com.itutorgroup.tutorchat.phone.utils.voice;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;


/**
 * 用于管理Dialog
 *
 *
 */
public class DialogManager {

    private AlertDialog.Builder builder;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mLable;
    private Context mContext;
    private int status = 0 ; //正常状态
    int time = AudioRecorderButton.WARNING_TIME;
    private AlertDialog dialog;//用于取消AlertDialog.Builder

    /**
     * 构造方法 传入上下文
     */
    public DialogManager(Context context) {
        this.mContext = context;
    }

    // 显示录音的对话框
    public void showRecordingDialog() {

        builder = new AlertDialog.Builder(mContext, R.style.AudioDialog);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_recorder,null);
        mIcon = (ImageView) view.findViewById(R.id.id_recorder_dialog_icon);
        mVoice = (ImageView) view.findViewById(R.id.id_recorder_dialog_voice);
        mLable = (TextView) view.findViewById(R.id.id_recorder_dialog_label);
        builder.setView(view);
        builder.create();
        dialog = builder.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.5f;
        window.setAttributes(lp);

    }

    public void reset(){
        status = 0;
    }


    public void recording() {

        if (dialog != null && dialog.isShowing()) { //显示状态
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);
            status = 0;
            mIcon.setImageResource(R.drawable.voice);
            mLable.setText(LPApp.getInstance().getString(R.string.str_recorder_want_up_cancel));
            mLable.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }

    }

    // 显示想取消的对话框
    public void wantToCancel() {
        if(dialog != null && dialog.isShowing()){ //显示状态
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            status = 1;
            mIcon.setImageResource(R.drawable.voice_cancel);
            mLable.setText(LPApp.getInstance().getString(R.string.str_recorder_want_cancel));
            mLable.setBackgroundResource(R.drawable.bg_record_cancel_text);
        }
    }

    // 显示时间过短的对话框
    public void tooShort() {
        if(dialog != null && dialog.isShowing()){ //显示状态
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLable.setVisibility(View.VISIBLE);
            status = 1;
            mIcon.setImageResource(R.drawable.voice_short);
            mLable.setText(LPApp.getInstance().getString(R.string.record_time_is_too_short));
            mLable.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }
    }


    public void showWillEndTime(int time) {
        if(dialog != null && dialog.isShowing() && status == 0){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLable.setVisibility(View.VISIBLE);
            status = 0;
            mIcon.setImageResource(R.drawable.voice);
            mLable.setText(LPApp.getInstance().getString(R.string.record_time_will_end_show,time+""));
            mLable.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }
    }

    // 显示取消的对话框
    public void dimissDialog() {
        if(dialog != null && dialog.isShowing()){ //显示状态
             dialog.dismiss();
            dialog = null;
        }
    }

    // 显示更新音量级别的对话框
    public void updateVoiceLevel(int level) {

        if(dialog != null && dialog.isShowing()){ //显示状态
			mIcon.setVisibility(View.VISIBLE);
            if(status == 1){
                mVoice.setVisibility(View.GONE);
            }else{
                mVoice.setVisibility(View.VISIBLE);
            }
//			mVoice.setVisibility(View.VISIBLE);
			mLable.setVisibility(View.VISIBLE);
            //设置图片的id
            int resId = mContext.getResources().getIdentifier("voice_"+level, "drawable", mContext.getPackageName());
            mVoice.setImageResource(resId);
        }
    }

}
