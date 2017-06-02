package com.itutorgroup.tutorchat.phone.ui.common.item;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.itutorgroup.tutorchat.phone.ui.common.CommonSwitchButton;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class SwitchItemView extends AbsItemView {

    public CommonSwitchButton mSwitchButton;

    public SwitchItemView(Context context) {
        super(context);
    }

    public SwitchItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View getRightView() {
        mSwitchButton = new CommonSwitchButton(getContext());
        return mSwitchButton;
    }

    @Override
    protected void loadData() {

    }

    @Override
    public void onClick(View v) {
        mSwitchButton.toggleSwitchState();
    }


}
