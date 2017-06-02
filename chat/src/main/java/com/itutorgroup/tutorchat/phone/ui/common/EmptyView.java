package com.itutorgroup.tutorchat.phone.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2017/1/11.
 */
public class EmptyView extends LinearLayout {

    private TextView mTip;
    private ImageView mIcon;

    public EmptyView(Context context) {
        super(context);
        init(null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_empty_view, null);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(view);
        mTip = (TextView) findViewById(R.id.tv_empty);
        mIcon = (ImageView) findViewById(R.id.imv_empty);
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.EmptyView);
            String tip = ta.getString(R.styleable.EmptyView_empty_str);
            int resId = ta.getResourceId(R.styleable.EmptyView_empty_icon, R.drawable.ic_empty_group);
            ta.recycle();

            setData(tip, resId);
        }
    }

    public void setData(String tip, int resId) {
        mTip.setText(tip);
        mIcon.setImageResource(resId);
    }
}
