package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class OnlineEvent {
    private static OnlineEvent sInstance;

    public static OnlineEvent getInstance() {
        if (sInstance == null) {
            synchronized (OnlineEvent.class) {
                if (sInstance == null) {
                    sInstance = new OnlineEvent();
                }
            }
        }
        return sInstance;
    }
}
