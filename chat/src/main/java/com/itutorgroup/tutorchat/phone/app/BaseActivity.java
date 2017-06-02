/**
 *
 */
package com.itutorgroup.tutorchat.phone.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MyActivityManager;
import com.jude.swipbackhelper.SwipeBackHelper;

import java.lang.ref.WeakReference;
import java.util.Locale;

import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.inject.Injector;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 工程的基类Activity
 *
 * @author Tony Shen
 */
public class BaseActivity extends FragmentActivity {

    public EventBus eventBus;
    protected Context mContext;

    private boolean mIsResume;

    protected Handler mHandler = new MyHandler(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.onCreate(this);
        SwipeBackHelper.getCurrentPage(this).setSwipeEdgePercent(0.2f);
        AppUtils.fixFocusedViewLeak(LPApp.getInstance());
        MyActivityManager.getInstance().add(this);
        mContext = this;
        eventBus = EventBusManager.getInstance();
        eventBus.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResume = true;
        AppManager.getInstance().resumeActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResume = false;
    }

    public boolean isResume() {
        return mIsResume;
    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        Injector.injectInto(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SwipeBackHelper.onDestroy(this);
        eventBus.unregister(this);
        MyActivityManager.getInstance().remove(this);
        LPApp.getInstance().getRefWatcher().watch(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            SwipeBackHelper.onPostCreate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 防止内部Handler类引起内存泄露
     */
    public static class MyHandler extends Handler {
        private final WeakReference<BaseActivity> mActivity;

        public MyHandler(BaseActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().handleMessage(msg);
        }
    }

    public void handleMessage(Message msg) {
    }
}
