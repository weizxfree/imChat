package com.itutorgroup.tutorchat.phone.ui.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.SystemMessageActivity;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/11/24.
 */
public class SystemNoticeView extends FrameLayout implements View.OnClickListener {

    public static final int TYPE_WIFI_DISCONNECT = 0x101;
    public static final int TYPE_SYSTEM_NOTICE = 0x102;

    private int mType;

    @InjectView(id = R.id.rl_container)
    RelativeLayout mRlContainer;

    @InjectView(id = R.id.imv_icon)
    ImageView mImvIcon;

    @InjectView(id = R.id.tv_content)
    TextView mTvContent;

    public SystemNoticeView(Context context, int type) {
        super(context);
        init(context, type);
    }

    private void init(Context context, int type) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_system_notice, null);
        addView(view);
        Injector.injectInto(this, this);
        initView(type);
    }

    private void initView(int type) {
        mType = type;
        setOnClickListener(this);
        if (mType == TYPE_SYSTEM_NOTICE) {
            mImvIcon.setImageResource(R.drawable.ic_system_notice);
            mRlContainer.setBackgroundColor(Color.parseColor("#199b9b9b"));
            mTvContent.setTextColor(Color.parseColor("#78849e"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (mType) {
            case TYPE_WIFI_DISCONNECT:
                onWifiClick();
                break;
            case TYPE_SYSTEM_NOTICE:
                onSystemNoticeClick();
                break;
        }
    }

    private void onSystemNoticeClick() {
        getContext().startActivity(new Intent(getContext(), SystemMessageActivity.class));
    }

    private void onWifiClick() {
        getContext().startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    }

    public void setContent(String content) {
        mTvContent.setText(content);
    }
}
