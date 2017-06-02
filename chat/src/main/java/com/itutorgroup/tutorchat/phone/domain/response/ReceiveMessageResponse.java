package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/24.
 */
public class ReceiveMessageResponse extends CommonResponse{

    @Tag(3)
    public String ReceiptID ;
    @Tag(4)
    public List<MessageModel> MessageList ;

    public ReceiveMessageResponse() {
    }

    @Override
    public String toString() {
        return "ReceiveMessageResponse{" +
                "ReceiptID='" + ReceiptID + '\'' +
                ", MessageList=" + MessageList +
                '}';
    }
}
