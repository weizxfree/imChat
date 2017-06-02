package com.itutorgroup.tutorchat.phone.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.MainActivity;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.domain.request.LoginRequest;
import com.itutorgroup.tutorchat.phone.domain.response.v2.LoginResponse_v2;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.ClearEditText;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.PasswordEditText;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MyActivityManager;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;
import com.jude.swipbackhelper.SwipeBackHelper;

import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;

public class LoginActivity extends BaseActivity {

    @InjectView(id = R.id.btn_login)
    TextView mBtnLogin;

    @InjectView(id = R.id.edt_account)
    ClearEditText mEdtAccount;

    @InjectView(id = R.id.edt_password)
    PasswordEditText mEdtPassword;

    @InjectView(id = R.id.tv_tip_forget_password)
    TextView mTvForgetTip;

    @InjectView(id = R.id.tv_tip_forget_password_content)
    TextView mTvForgetTipContent;

    public static final String EXTRA_ERROR_INFO = "error_info";
    private int mErrorState;

    public static final int RESULT_LOGIN_SUCCESS = 0x11;

    private boolean mKeepCustomEnv = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyActivityManager.getInstance().finishOtherActivity(this);
        setContentView(R.layout.activity_login);
        checkErrorCode();
        InputMethodUtil.registerVisible(findViewById(R.id.scroll_content), findViewById(R.id.imv_logo));
        mEdtAccount.setOnFocusChangeListener(mOnEdtFocusChangeListener);
        mEdtPassword.setOnFocusChangeListener(mOnEdtFocusChangeListener);
        String lastAccount = AccountManager.getInstance().getLastAccount();
        AccountManager.getInstance().clearAccountData();
        if (!TextUtils.isEmpty(lastAccount)) {
            mEdtAccount.setText(lastAccount);
            mEdtPassword.requestFocus();
        }
        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        Kernel.getInstance().stopTcpService();
        checkAppEnv();
    }

    private void checkErrorCode() {
        mErrorState = getIntent().getIntExtra(EXTRA_ERROR_INFO, 0);
        if (mErrorState != 0) {
            int resId = 0;
            switch (mErrorState) {
                case NetworkError.ERROR_TOKEN_FAILED:
//                    resId = R.string.login_cause_token_failed;
//                    break;
                case NetworkError.ERROR_INVALID_USER_ID:
//                    resId = R.string.login_cause_user_id_invalid;
                    resId = R.string.login_cause_account_failed;
                    break;
                case NetworkError.ERROR_AES_DECRYPT_FAILED:
                    resId = R.string.login_cause_aes_decrypt_failed;
                    break;
                case NetworkError.LOGIN_CAUSE_ENV_CHANGED:
                    mKeepCustomEnv = true;
                    resId = R.string.login_cause_env_changed;
                    break;
            }
            showErrorDialog(resId);
        }
    }

    private void showErrorDialog(int resId) {
        if (resId != 0) {
            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setMessage(resId)
                    .setCancelable(false)
                    .setPositiveButton(R.string.dialog_message_ok, null)
                    .show();
        }
    }

    @OnClick(id = R.id.btn_login)
    private void login() {
        InputMethodUtil.hideInputMethod(LoginActivity.this);
        String account = mEdtAccount.getText().toString();
        String password = mEdtPassword.getText().toString();

        if (TextUtils.isEmpty(account)) {
            showError(getString(R.string.tip_login_empty_account));
        } else if (!verifyMail(account)) {
            showError(getString(R.string.tip_login_error_account_format));
        } else if (TextUtils.isEmpty(password)) {
            showError(getString(R.string.tip_login_empty_password));
        } else {
            loginAsyncTask(account, password);
        }
    }

    private void checkAppEnv() {
        if (mKeepCustomEnv) {
            APIConstant.chooseEnv();
        } else {
            ACache.get(LPApp.getInstance(), ACache.APP_ENV).clear();
        }
    }

    private boolean verifyMail(String account) {
        String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        return account.matches(pattern);
    }

    @OnClick(id = R.id.tv_tip_forget_password)
    void onForgetPasswordClick() {
//        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        InputMethodUtil.hideInputMethod(LoginActivity.this);
        showError(getString(R.string.tips_forget_login_password));
        mTvForgetTip.setTextColor(getResources().getColor(R.color.text_color_tip_forget_password_selected));
    }

    /**
     * 登录接口
     *
     * @param account
     * @param password
     */
    private void loginAsyncTask(String account, String password) {
        LoginRequest request = new LoginRequest(account, password);
        new RequestHandler<LoginResponse_v2>()
                .dialog(LoginActivity.this).tag(this)
                .operation(Operation.USER_LOGIN_V2)
                .bundle(new NBundle().ignoreAllError().build())
                .request(request)
                .exec(LoginResponse_v2.class, new RequestHandler.RequestListener<LoginResponse_v2>() {
                    @Override
                    public void onResponse(LoginResponse_v2 loginResponse, Bundle bundle) {
                        if (loginResponse != null) {
                            AccountManager.getInstance().loginSuccess(loginResponse.Token, loginResponse.User, loginResponse.AESKey, loginResponse.AESValue);
                            AccountManager.getInstance().setLastAccount(loginResponse.User.CompanyEmail);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            setResult(RESULT_LOGIN_SUCCESS);
                            finish();
                        }
                    }

                    @Override
                    public void onError(int errorCode, LoginResponse_v2 response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        String errorMessage = null;
                        boolean isNetWorkActive = AppUtils.isNetWorkActive();
                        if (isNetWorkActive) {
                            errorMessage = NetworkError.sErrorArray.get(errorCode);
                        } else {
                            errorMessage = getString(R.string.tip_home_wifi_disconnected);
                        }
                        if (!TextUtils.isEmpty(errorMessage)) {
                            showError(errorMessage);
                        } else if (e != null) {
                            showError(e.getMessage());
                        } else {
                            showError(getString(R.string.tip_login_failed));
                        }
                    }
                });
    }

    private void showError(String errorMessage) {
        mTvForgetTipContent.setText(errorMessage);
        mTvForgetTipContent.setVisibility(View.VISIBLE);
    }

    @OnClick(id = {R.id.edt_account, R.id.edt_password})
    public void onEdtClick() {
        mTvForgetTipContent.setVisibility(View.GONE);
        mTvForgetTip.setTextColor(getResources().getColor(android.R.color.white));
    }

    private View.OnFocusChangeListener mOnEdtFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mTvForgetTipContent.setVisibility(View.GONE);
                mTvForgetTip.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
    };
}
