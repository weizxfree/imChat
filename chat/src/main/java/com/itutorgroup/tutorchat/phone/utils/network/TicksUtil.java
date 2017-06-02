package com.itutorgroup.tutorchat.phone.utils.network;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/23.
 */
public class TicksUtil {

    private static final String KEY_TICKS = "key_request_ticks:%s:%s";

    private static String getKey(String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        return String.format(KEY_TICKS, currentUserId, key);
    }

    public static void setTicks(String key, final long ticks) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        Observable.just(key)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String key) {
                        key = getKey(key);
                        ACache.get(LPApp.getInstance()).put(key, "" + ticks);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public static long getTicks(String key) {
        if (TextUtils.isEmpty(key)) {
            return 0;
        }
        key = getKey(key);
        String tickStr = ACache.get(LPApp.getInstance()).getAsString(key);
        if (!TextUtils.isEmpty(tickStr)) {
            try {
                long tick = Long.parseLong(tickStr);
                return tick;
            } catch (Exception e) {

            }
        }
        return 0;
    }

    public static void setContactsListTick(long tick) {
        setTicks("getAllContract:" + AccountManager.getInstance().getCurrentUserId(), tick);
    }

    public static long getContactsListTick() {
        return getTicks("getAllContract:" + AccountManager.getInstance().getCurrentUserId());
    }

    public static void setUserSettingTick(long tick) {
        setTicks("GetUserSetting:" + AccountManager.getInstance().getCurrentUserId(), tick);
    }

    public static long getUserSettingTick() {
        return getTicks("GetUserSetting:" + AccountManager.getInstance().getCurrentUserId());
    }
}
