package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by tom_zxzhang on 2016/11/16.
 */
public class UpdateVersionStatusEvent {

    private static UpdateVersionStatusEvent sInstance;

    public static UpdateVersionStatusEvent getInstance() {
        if (sInstance == null) {
            synchronized (UpdateVersionStatusEvent.class) {
                if (sInstance == null) {
                    sInstance = new UpdateVersionStatusEvent();
                }
            }
        }
        return sInstance;
    }

}
