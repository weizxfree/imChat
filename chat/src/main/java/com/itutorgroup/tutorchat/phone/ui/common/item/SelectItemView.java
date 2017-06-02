package com.itutorgroup.tutorchat.phone.ui.common.item;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class SelectItemView extends AbsItemView {

    public TextView mTextView;

    public SelectItemView(Context context) {
        super(context);
    }

    public SelectItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View getRightView() {
        mTextView = new TextView(getContext());
        mTextView.setTextColor(getResources().getColor(R.color.text_color_time_send_delay));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(Gravity.CENTER_VERTICAL);
        mTextView.setLayoutParams(params);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setTextColor(getResources().getColor(R.color.gray));
        mTextView.setText(TimeUtils.ConvertUiSendTime(TimeUtils.DEFAULT_DATE_FORMAT.format(new java.util.Date())));
        return mTextView;
    }

    @Override
    protected void loadData() {

    }


    public void clear(){

        mTvTitle.setText("");
        mTextView.setText("");
    }


    public void reset(String time){
        mTvTitle.setTextColor(getResources().getColor(R.color.common_preference_item_text_color));
        mTvTitle.setText(LPApp.getInstance().getString(R.string.delay_send_time));
        mTextView.setText(time);
    }

}
