package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/9/23.
 */
public class NetworkEvent {
    public boolean mIsConnect;

    public NetworkEvent(boolean isConnect) {
        mIsConnect = isConnect;
    }
}
