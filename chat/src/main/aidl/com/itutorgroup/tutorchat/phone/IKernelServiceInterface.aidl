package com.itutorgroup.tutorchat.phone;

import com.itutorgroup.tutorchat.phone.INotifyCallBack;

interface IKernelServiceInterface {
    void registerCallBack(INotifyCallBack cb);
    void unRegisterCallBack(INotifyCallBack cb);
    void sendMessage(String stringBase64);
    boolean isTcpOnline();
    boolean isTcpConnect();
    void resumeTcpIfDied();
}
