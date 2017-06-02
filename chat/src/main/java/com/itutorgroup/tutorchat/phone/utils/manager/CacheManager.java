package com.itutorgroup.tutorchat.phone.utils.manager;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2017/1/13.
 */
public class CacheManager {
    private static CacheManager sInst;

    public static CacheManager getInst() {
        if (sInst == null) {
            synchronized (CacheManager.class) {
                if (sInst == null) {
                    sInst = new CacheManager();
                }
            }
        }
        return sInst;
    }

    private CacheManager() {
    }

    public void save(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        Observable.just(new String[]{key, value})
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] str) {
                        ACache.get(LPApp.getInstance()).put(str[0], str[1], 24 * 60 * 60);
                    }
                }, CommonUtil.ACTION_EXCEPTION);

    }

    public String getString(String key) {
        return ACache.get(LPApp.getInstance()).getAsString(key);
    }
}
