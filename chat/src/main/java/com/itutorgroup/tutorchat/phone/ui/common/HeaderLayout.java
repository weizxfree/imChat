package com.itutorgroup.tutorchat.phone.ui.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class HeaderLayout extends LinearLayout {

    public View mContentView;
    public LinearLayout mLayoutLeftContainer;
    public LinearLayout mLayoutRightContainer;
    public LinearLayout mLayoutMiddleContainer;
    public TextView mTvTitle;

    public HeaderLayout(Context context) {
        super(context);
        init(context);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.header_layout, null);
        addView(mContentView);
        initViews();
    }

    private void initViews() {
        mLayoutLeftContainer = (LinearLayout) findViewById(R.id.header_layout_left_container);
        mLayoutRightContainer = (LinearLayout) findViewById(R.id.header_layout_right_container);
        mLayoutMiddleContainer = (LinearLayout) findViewById(R.id.header_layout_middle_container);
        mTvTitle = (TextView) findViewById(R.id.header_htv_subtitle);
    }

    public HeaderLayout transparent() {
        mContentView.setBackgroundColor(Color.TRANSPARENT);
        return this;
    }

    public HeaderLayout title(String title) {
        mTvTitle.setText(title);
        return this;
    }

    public HeaderLayout leftText(String text, OnClickListener listener) {
        addText(text, listener, mLayoutLeftContainer);
        return this;
    }

    public TextView addLeftText(String text, OnClickListener listener) {
        return addText(text, listener, mLayoutLeftContainer);
    }

    public HeaderLayout leftImage(int resId, OnClickListener listener) {
        addImage(resId, listener, mLayoutLeftContainer);
        return this;
    }

    public ImageView addLeftImage(int resId, OnClickListener listener) {
        return addImage(resId, listener, mLayoutLeftContainer);
    }

    public HeaderLayout titleImage(int resId, OnClickListener listener) {
        addImage(resId, listener, mLayoutMiddleContainer);
        return this;
    }

    public ImageView addTitleImage(int resId, OnClickListener listener) {
        return addImage(resId, listener, mLayoutMiddleContainer);
    }

    public HeaderLayout rightText(String text, OnClickListener listener) {
        addText(text, listener, mLayoutRightContainer);
        return this;
    }

    public TextView addRightText(String text, OnClickListener listener) {
        return addText(text, listener, mLayoutRightContainer);
    }

    public HeaderLayout rightImage(int resId, OnClickListener listener) {
        addImage(resId, listener, mLayoutRightContainer);
        return this;
    }

    public ImageView addRightImage(int resId, OnClickListener listener) {
        return addImage(resId, listener, mLayoutRightContainer);
    }

    public HeaderLayout autoCancel(Activity activity) {
        TextView tv = (TextView) inflate(getContext(), R.layout.header_menu_text, null);
        tv.setText(R.string.back);
        Drawable drawable = getResources().getDrawable(R.drawable.back);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tv.setCompoundDrawables(drawable, null, null, null);
        tv.setCompoundDrawablePadding(-PixelUtil.dp2px(10));
        OnClickListener listener = new InputMethodUtil.CancelListener(activity);
        tv.setOnClickListener(listener);
        mLayoutLeftContainer.addView(tv);
        return this;
    }

    public TextView addCancelMenu(OnClickListener listener) {
        TextView tv = (TextView) inflate(getContext(), R.layout.header_menu_text, null);
        tv.setText(R.string.back);
        Drawable drawable = getResources().getDrawable(R.drawable.back);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tv.setCompoundDrawablePadding(-PixelUtil.dp2px(10));
        tv.setCompoundDrawables(drawable, null, null, null);
        tv.setOnClickListener(listener);
        mLayoutLeftContainer.addView(tv);
        return tv;
    }

    private TextView addText(String text, OnClickListener listener, ViewGroup viewGroup) {
        TextView tv = (TextView) inflate(getContext(), R.layout.header_menu_text, null);
        if (listener != null) {
            tv.setOnClickListener(listener);
        }
        tv.setText(text);
        viewGroup.addView(tv);
        return tv;
    }

    private ImageView addImage(int resId, OnClickListener listener, ViewGroup viewGroup) {
        View view = inflate(getContext(), R.layout.header_menu_image, null);
        if (listener != null) {
            view.setOnClickListener(listener);
        }
        ImageView imv = (ImageView) view.findViewById(R.id.imv_header_item);
        imv.setImageResource(resId);
        viewGroup.addView(view);
        return imv;
    }
}