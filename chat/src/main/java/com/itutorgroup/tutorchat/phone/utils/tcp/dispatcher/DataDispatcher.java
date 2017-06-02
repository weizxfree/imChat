package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.utils.AesEncryptionUtil;
import com.itutorgroup.tutorchat.phone.utils.StreamUtils;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.kernel.DataThread;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public class DataDispatcher implements IDispatchListener {
    private static DataDispatcher sInstance;

    public static DataDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (DataDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new DataDispatcher();
                }
            }
        }
        return sInstance;
    }

    private DataDispatcher() {
    }

    @Override
    public void dispatch(final int operation, final byte[] bytes, final IDataListener dataListener) {
        Observable.just(bytes)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        checkWaitForOnline(operation, dataListener);
                        IDispatchListener listener = getDispatchListener(operation);
                        if (listener != null) {
                            byte[] data = StreamUtils.subBytes(bytes, 16, bytes.length - 16);
                            if (data != null && data.length > 0) {
                                data = AesEncryptionUtil.decrypt(data, dataListener.getAESKey(operation), Constant.TCP_AES_IV);
                            }
                            listener.dispatch(operation, data, dataListener);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtil.exception(throwable);
                        String data = "AES key = " + dataListener.getAESKey(operation) + ", data = " + StreamUtils.printBytesToString(bytes);
                        LogUtil.e(DataThread.TAG, data);
//                        AppManager.getInstance().uploadLog("tcp package", data);
                    }
                });
    }

    private void checkWaitForOnline(int operation, IDataListener listener) {
        switch (operation) {
            case Operation.TCP_RECEIVE_OFFLINE_MESSAGE:
            case Operation.TCP_VERIFY_OFFLINE_MESSAGE:
            case Operation.TCP_AUTH:
            case Operation.TCP_OFFLINE_INSTRUCTION:
            case Operation.TCP_OFFLINE_INSTRUCTION_VERIFY:
                listener.waitForOnline(operation);
                break;
            case Operation.TCP_DEVICE_ONLINE:
                listener.online();
                break;
        }
    }

    private IDispatchListener getDispatchListener(int operation) {
        switch (operation) {
            case Operation.TCP_RECEIVE_MESSAGE:
            case Operation.TCP_RECEIVE_OFFLINE_MESSAGE:
                return MessageReceiveDispatcher.getInstance();
            case Operation.TCP_VERIFY_MESSAGE:
            case Operation.TCP_VERIFY_OFFLINE_MESSAGE:
                return MessageVerifyDispatcher.getInstance();
            case Operation.TCP_AUTH:
                return TcpAuthDispatcher.getInstance();
            case Operation.TCP_INSTRUCTION:
            case Operation.TCP_OFFLINE_INSTRUCTION:
                return InstructionDispatcher.getInstance();
            case Operation.TCP_INSTRUCTION_VERIFY:
            case Operation.TCP_OFFLINE_INSTRUCTION_VERIFY:
                return null;
            case Operation.TCP_DEVICE_ONLINE:
                return null;
            case Operation.TCP_KICK_OUT:
                return KickOutDispatcher.getInstance();
        }
        return null;
    }

    public interface IDataListener {
        void sendRequest(int operation, Object request);

        void sendHeartBeat();

        String getAESKey(int operation);

        void setAESKey(String key);

        void waitForOnline(int operation);

        void online();

        void updateConversationList();

        void updateNotification(ArrayList<String> list);

        void onPatches(int type, long timeSpan, String id);

        void onReceiveMessageModel(ArrayList<String> idList);

        void kickOut();
    }
}
