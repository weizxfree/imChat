package com.itutorgroup.tutorchat.phone.utils.tcp;

import com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher.DataDispatcher;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public interface IDispatchListener {
    void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener);
}
