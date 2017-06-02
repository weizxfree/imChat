package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

import java.util.List;

/**
 * Created by tom_zxzhang on 2016/8/31.
 */
public class MessageEvent {


    public List<MessageModel> list;


    public MessageEvent(List<MessageModel> list){
        this.list =list;
    }

}
