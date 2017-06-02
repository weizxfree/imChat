package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import com.itutorgroup.tutorchat.phone.domain.response.tcp.TCPAuthResponse;
import com.itutorgroup.tutorchat.phone.utils.ProtoStuffSerializerUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class TcpAuthDispatcher implements IDispatchListener {
    private static TcpAuthDispatcher sInstance;

    public static TcpAuthDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (TcpAuthDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new TcpAuthDispatcher();
                }
            }
        }
        return sInstance;
    }

    private TcpAuthDispatcher() {
    }

    @Override
    public void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener) {
        TCPAuthResponse response = ProtoStuffSerializerUtil.deserialize(bytes, TCPAuthResponse.class);
        if (response != null) {
            listener.setAESKey(response.AesKey);
            listener.sendHeartBeat();
        }
    }
}
