package com.itutorgroup.tutorchat.phone.activity.account;

import android.os.Bundle;
import android.widget.Button;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.PasswordEditText;
import com.itutorgroup.tutorchat.phone.utils.ui.EnableStateUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class ResetPasswordActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.edt_password)
    PasswordEditText mEdtPassword;

    @InjectView(id = R.id.edt_repeat_password)
    PasswordEditText mEdtRepeatPassword;

    @InjectView(id = R.id.btn_login)
    Button mBtnLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initView();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_reset_password)).transparent()
                .autoCancel(this);
        EnableStateUtil.proxy(mBtnLogin, mEdtPassword, mEdtRepeatPassword);
    }
}
