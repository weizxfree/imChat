package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.response.CheckIsReadResponse;

import java.util.List;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class UpdateMessageReadStatusEvent {
    private static UpdateMessageReadStatusEvent sInstance;

    public List<CheckIsReadResponse.ReadModel> readModels ;

    public List<String> readMessageId ;

    public static UpdateMessageReadStatusEvent getInstance() {
        if (sInstance == null) {
            synchronized (UpdateMessageReadStatusEvent.class) {
                if (sInstance == null) {
                    sInstance = new UpdateMessageReadStatusEvent();
                }
            }
        }
        return sInstance;
    }

    public UpdateMessageReadStatusEvent() {
    }

    public UpdateMessageReadStatusEvent(List<CheckIsReadResponse.ReadModel> readModels){
        this.readModels = readModels;
    }

    public UpdateMessageReadStatusEvent(List<String> readMessageId,boolean single){
        this.readMessageId = readMessageId;
    }

}
