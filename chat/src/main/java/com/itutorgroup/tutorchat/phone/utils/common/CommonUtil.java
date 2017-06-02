package com.itutorgroup.tutorchat.phone.utils.common;

import android.text.TextUtils;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by joyinzhao on 2016/12/2.
 */
public class CommonUtil {
    public static <T> void sendResponse(T t, CommonLoadingListener<T> listener) {
        if (listener != null) {
            listener.onResponse(t);
        }
    }

    public static String getIPofUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        } else if (!url.contains(":")) {
            url = "http://" + url;
        }
        try {
            InetAddress address = InetAddress.getByName(new URL(url).getHost());
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public static Action1<Throwable> ACTION_EXCEPTION = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            LogUtil.exception(throwable);
        }
    };

    public static Func1<List, Boolean> FUNC_FILTER_LIST_NOT_EMPTY = new Func1<List, Boolean>() {
        @Override
        public Boolean call(List list) {
            return list != null && list.size() > 0;
        }
    };
}
