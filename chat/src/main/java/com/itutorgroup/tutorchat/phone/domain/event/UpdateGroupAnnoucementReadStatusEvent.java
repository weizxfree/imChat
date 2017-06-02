package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class UpdateGroupAnnoucementReadStatusEvent {
    private static UpdateGroupAnnoucementReadStatusEvent sInstance;


    public static UpdateGroupAnnoucementReadStatusEvent getInstance() {
        if (sInstance == null) {
            synchronized (UpdateGroupAnnoucementReadStatusEvent.class) {
                if (sInstance == null) {
                    sInstance = new UpdateGroupAnnoucementReadStatusEvent();
                }
            }
        }
        return sInstance;
    }

}
