package com.itutorgroup.tutorchat.phone.activity.chat;

import android.os.Bundle;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class SystemMessageSettingsActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message_settings);
        initView();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_system_message)).autoCancel(this);
    }
}
