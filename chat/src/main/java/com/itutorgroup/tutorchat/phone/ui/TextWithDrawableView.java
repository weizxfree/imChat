package com.itutorgroup.tutorchat.phone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by tom_zxzhang on 2016/10/9.
 */
public class TextWithDrawableView extends RelativeLayout{


    public TextWithDrawableView(Context context) {
        super(context);
        initView(context);
    }

    public TextWithDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context){
            View.inflate(context, R.layout.header_left_text_with_drawable, this);
    }



}
