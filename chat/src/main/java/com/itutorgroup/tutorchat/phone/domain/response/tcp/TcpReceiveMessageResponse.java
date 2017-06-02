package com.itutorgroup.tutorchat.phone.domain.response.tcp;

import com.itutorgroup.tutorchat.phone.domain.db.model.TcpMessageModel;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public class TcpReceiveMessageResponse extends CommonResponse {
    @Tag(3)
    public long ReceiptID;
    @Tag(4)
    public ArrayList<TcpMessageModel> MessageList;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (MessageList != null) {
            sb.append(MessageList.size());
            for (TcpMessageModel model : MessageList) {
                sb.append(",").append(model.toString());
            }
        }
        return "TcpReceiveMessageResponse{" +
                "ReceiptID='" + ReceiptID + '\'' +
                ", MessageList=" + sb.toString() +
                '}';
    }
}
