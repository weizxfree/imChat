package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import com.itutorgroup.tutorchat.phone.domain.response.tcp.TcpVerifyMessageResponse;
import com.itutorgroup.tutorchat.phone.utils.ProtoStuffSerializerUtil;
import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

/**
 * Created by joyinzhao on 2016/11/8.
 */
public class MessageVerifyDispatcher implements IDispatchListener {
    private static MessageVerifyDispatcher sInstance;

    public static MessageVerifyDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (MessageVerifyDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new MessageVerifyDispatcher();
                }
            }
        }
        return sInstance;
    }

    private MessageVerifyDispatcher() {
    }

    @Override
    public void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener) {
        TcpVerifyMessageResponse response = ProtoStuffSerializerUtil.deserialize(bytes, TcpVerifyMessageResponse.class);
        if (response != null && response.ReceiptID != 0) {
//            MessageReceiveDispatcher.getInstance().onReceiptDone(operation, response.ReceiptID);
        }
    }
}
