package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class ContactsEvent {
    private static ContactsEvent sInstance;

    public static ContactsEvent getInstance() {
        if (sInstance == null) {
            synchronized (ContactsEvent.class) {
                if (sInstance == null) {
                    sInstance = new ContactsEvent();
                }
            }
        }
        return sInstance;
    }
}
