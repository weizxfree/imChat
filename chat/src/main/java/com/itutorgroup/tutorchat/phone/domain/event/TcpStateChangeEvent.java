package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/12/30.
 */
public class TcpStateChangeEvent {
    private static TcpStateChangeEvent sInstance;

    public static TcpStateChangeEvent getInstance() {
        if (sInstance == null) {
            synchronized (TcpStateChangeEvent.class) {
                if (sInstance == null) {
                    sInstance = new TcpStateChangeEvent();
                }
            }
        }
        return sInstance;
    }

    private TcpStateChangeEvent() {
    }
}
