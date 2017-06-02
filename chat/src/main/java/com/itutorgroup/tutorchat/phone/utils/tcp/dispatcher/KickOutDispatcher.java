package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

/**
 * Created by joyinzhao on 2016/12/2.
 */
public class KickOutDispatcher implements IDispatchListener {
    private static KickOutDispatcher sInstance;

    public static KickOutDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (KickOutDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new KickOutDispatcher();
                }
            }
        }
        return sInstance;
    }

    private KickOutDispatcher() {
    }


    @Override
    public void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener) {
        listener.kickOut();
    }
}
