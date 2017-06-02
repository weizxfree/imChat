package com.itutorgroup.tutorchat.phone.ui.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

import java.util.ArrayList;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class CommonSwitchButton extends TextView {

    private ArrayList<OnSwitchListener> onSwitchListenerList; // 开关监听器

    private boolean mIsChecked;

    public CommonSwitchButton(Context context) {
        super(context);
        init();
    }

    private void init() {
        onSwitchListenerList = new ArrayList<>();
        setBackgroundResource(R.drawable.common_button_switch);
        setLayoutParams(new ViewGroup.LayoutParams(PixelUtil.dp2px(40), PixelUtil.dp2px(25)));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSwitchState();
            }
        });
    }

    private void notifySwitchState(boolean isChecked) {
        if (onSwitchListenerList != null && onSwitchListenerList.size() > 0) {
            for (OnSwitchListener listener : onSwitchListenerList) {
                listener.onSwitched(isChecked);
            }
        }
    }

    public void toggleSwitchState() {
        setSwitchState(!mIsChecked);
    }

    public void initSwitchState(boolean switchState) {
        mIsChecked = switchState;
        setSelected(switchState);
    }

    /**
     * setSwitchState and notify
     *
     * @param switchState
     */
    public void setSwitchState(boolean switchState) {
        mIsChecked = switchState;
        setSelected(mIsChecked);
        notifySwitchState(mIsChecked);
    }

    public boolean getSwitchState() {
        return mIsChecked;
    }

    public void setOnSwitchStateListener(OnSwitchListener listener) {
        onSwitchListenerList.add(listener);
    }

    public interface OnSwitchListener {
        void onSwitched(boolean isSwitchOn);
    }
}