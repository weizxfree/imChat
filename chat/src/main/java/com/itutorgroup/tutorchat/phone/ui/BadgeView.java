package com.itutorgroup.tutorchat.phone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by tom_zxzhang on 2016/10/9.
 */
public class BadgeView extends RelativeLayout{

    public TextView mTextView;

    public BadgeView(Context context) {
        super(context);
        initView(context);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context){
            View.inflate(context, R.layout.header_group_announcement_number, this);
            mTextView = (TextView) findViewById(R.id.tv_count);
    }

    public void setVisvible(boolean flag){
        if(flag){
            mTextView.setVisibility(View.VISIBLE);
        }else{
            mTextView.setVisibility(View.INVISIBLE);
        }
    }

    public void setCount(Long count){
        if(count <=0)
            return;
        mTextView.setText(count+"");
    }


}
