package com.itutorgroup.tutorchat.phone.ui.popup;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by tom_zxzhang on 2016/11/1.
 */
public class AudioSensorPopWindow extends PopupWindow {


    private TextView mTextView;
    private Activity mActivity;
    public AudioSensorPopWindow(final Activity activity) {
        mActivity = activity;
        View view = activity.getLayoutInflater().inflate(R.layout.pop_audio_sensor, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setAnimationStyle(R.style.pop_window_anim_style);
        setOutsideTouchable(true);
        //设置SelectPicPopupWindow弹出窗体的背景
//        setBackgroundDrawable(new BitmapDrawable());
        mTextView = (TextView) view.findViewById(R.id.text);


    }


    public void setText(String text){
        mTextView.setText(text);
    }



}
