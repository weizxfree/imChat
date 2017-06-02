package com.itutorgroup.tutorchat.phone.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by joyinzhao on 2016/9/12.
 */
public class MyGridView extends GridView {
    private boolean needScrollBar = false;   //设置是否有ScrollBar，当要在ScollView中显示时，应当设置为false。 否则为 true

    public MyGridView(Context context) {
        super(context);
    }

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!needScrollBar) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}