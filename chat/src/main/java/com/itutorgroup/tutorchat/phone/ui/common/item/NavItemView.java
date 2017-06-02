package com.itutorgroup.tutorchat.phone.ui.common.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class NavItemView extends AbsItemView {

    public ImageView mImvNav;

    public NavItemView(Context context) {
        super(context);
    }

    public NavItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View getRightView() {
        mImvNav = new ImageView(getContext());
        mImvNav.setImageResource(R.drawable.next);
        int size = PixelUtil.dp2px(20);
        mImvNav.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        return mImvNav;
    }

    @Override
    protected void loadData() {

    }

    public void setNavAlpha() {
        mImvNav.setAlpha(80);
    }
}
