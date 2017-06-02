package com.itutorgroup.tutorchat.phone.domain.event;

import java.util.List;

/**
 * Created by joyinzhao on 2017/2/6.
 */
public class MessageReadEvent {
    public List<String> mList;

    public MessageReadEvent(List<String> list) {
        this.mList = list;
    }
}
