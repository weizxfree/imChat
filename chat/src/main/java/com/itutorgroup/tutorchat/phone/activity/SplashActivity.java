package com.itutorgroup.tutorchat.phone.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.itutorgroup.tutorchat.phone.activity.account.LoginActivity;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.jude.swipbackhelper.SwipeBackHelper;

/**
 * splash页面
 *
 * @author tom_zxzhang
 */
public class SplashActivity extends BaseActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
        initAccountState();
    }

    private void initAccountState() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                AppManager.getInstance().autoLoadAppSettings(false);
                return AccountManager.getInstance().loadLoginData();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                Class clazz;
                if (aBoolean) {
                    clazz = MainActivity.class;
                } else {
                    clazz = LoginActivity.class;
                }
                startActivity(new Intent(SplashActivity.this, clazz));
                finish();
            }
        }.execute();
    }
}
