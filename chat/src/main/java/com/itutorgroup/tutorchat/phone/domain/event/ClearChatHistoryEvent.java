package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class ClearChatHistoryEvent {


    private static ClearChatHistoryEvent sInstance;

    public static ClearChatHistoryEvent getInstance() {
        if (sInstance == null) {
            synchronized (ClearChatHistoryEvent.class) {
                if (sInstance == null) {
                    sInstance = new ClearChatHistoryEvent();
                }
            }
        }
        return sInstance;
    }
}
