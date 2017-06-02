package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.Activity;

import com.itutorgroup.tutorchat.phone.app.BaseActivity;

import java.util.ArrayList;

/**
 * Created by joyinzhao on 2016/9/28.
 */
public class MyActivityManager {
    private static MyActivityManager sInstance;

    public static MyActivityManager getInstance() {
        if (sInstance == null) {
            synchronized (MyActivityManager.class) {
                if (sInstance == null) {
                    sInstance = new MyActivityManager();
                }
            }
        }
        return sInstance;
    }

    private ArrayList<BaseActivity> mActivityList = new ArrayList<>();

    public void add(BaseActivity activity) {
        mActivityList.add(activity);
    }

    public void remove(BaseActivity activity) {
        mActivityList.remove(activity);
    }

    public void finishAll() {
        if (mActivityList != null && mActivityList.size() > 0) {
            for (Activity activity : mActivityList) {
                activity.finish();
            }
        }
    }

    public BaseActivity getTopActivity() {
        if (mActivityList != null && mActivityList.size() > 0) {
            return mActivityList.get(mActivityList.size() - 1);
        }
        return null;
    }

    public boolean isTopActivityResumed() {
        BaseActivity activity = MyActivityManager.getInstance().getTopActivity();
        return activity != null && activity.isResume();
    }


    /**
     * finish参数之外的所有Activity
     *
     * @param activities 需要保留的Activity
     */
    public void finishOtherActivity(Activity... activities) {
        if (mActivityList != null && activities != null && mActivityList.size() > 0) {
            for (Activity activity : mActivityList) {
                try {
                    boolean ignore = false;
                    for (Activity ignoreActivity : activities) {
                        if (ignoreActivity == activity) {
                            ignore = true;
                            break;
                        }
                    }
                    if (!ignore) {
                        activity.finish();
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    public void finishActivity(String... activities) {
        if (activities != null) {
            for (String name : activities) {
                if (mActivityList != null && mActivityList.size() > 0) {
                    for (Activity a : mActivityList) {
                        if (a.getComponentName().getClassName().contains(name)) {
                            a.finish();
                        }
                    }
                }
            }
        }
    }
}
