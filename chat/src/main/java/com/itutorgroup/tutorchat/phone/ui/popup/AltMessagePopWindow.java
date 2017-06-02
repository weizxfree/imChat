package com.itutorgroup.tutorchat.phone.ui.popup;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

public class AltMessagePopWindow extends PopupWindow {

    private Activity mActivity;
    private TextView mTvSearchContacts;
    public TextView mTvCreateGroup;

    public AltMessagePopWindow(final Activity activity) {
        mActivity = activity;
        View view = activity.getLayoutInflater().inflate(R.layout.pop_have_alt_message, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setAnimationStyle(R.style.pop_window_anim_style);
        setOutsideTouchable(false);
        //设置SelectPicPopupWindow弹出窗体的背景
//        setBackgroundDrawable(new BitmapDrawable());
        mTvCreateGroup = (TextView) view.findViewById(R.id.text);
        mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setContent(String s){
        mTvCreateGroup.setText(s);
    }











}
