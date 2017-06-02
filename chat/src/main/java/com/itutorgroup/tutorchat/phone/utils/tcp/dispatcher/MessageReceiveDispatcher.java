package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.PushInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.migration.MigrateHelper;
import com.itutorgroup.tutorchat.phone.domain.request.tcp.VerifyMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.response.tcp.TcpReceiveMessageResponse;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.ProtoStuffSerializerUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public class MessageReceiveDispatcher implements IDispatchListener {
    private static MessageReceiveDispatcher sInstance;

    public static MessageReceiveDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (MessageReceiveDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new MessageReceiveDispatcher();
                }
            }
        }
        return sInstance;
    }

    private MessageReceiveDispatcher() {
    }

    private DataDispatcher.IDataListener mDataListener;

    @Override
    public void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener) {
        mDataListener = listener;
        TcpReceiveMessageResponse response = ProtoStuffSerializerUtil.deserialize(bytes, TcpReceiveMessageResponse.class);
        if (response != null && response.ReceiptID != 0 && response.MessageList != null) {
            VerifyMessageRequest request = new VerifyMessageRequest();
            request.ReceiptID = response.ReceiptID;
            int op = Operation.TCP_VERIFY_MESSAGE;
            if (operation == Operation.TCP_RECEIVE_OFFLINE_MESSAGE) {
                op = Operation.TCP_VERIFY_OFFLINE_MESSAGE;
            }
            listener.sendRequest(op, request);
            LogUtil.d("tcp response = " + response.toString());
            onReceiptDone(op, response);
        }
    }

    public void onReceiptDone(int operation, TcpReceiveMessageResponse response) {
        List<MessageModel> list = MigrateHelper.migration(response.MessageList, MigrateHelper.sMessageModelMigrationListener);
        if (list == null || list.size() == 0) {
            return;
        }
        CommonLoadingListener<List<MessageModel>> listener = null;
        if (operation == Operation.TCP_VERIFY_MESSAGE) {
            listener = mOnMessageSavedListener;
        } else {
            listener = new CommonLoadingListener<List<MessageModel>>() {
                @Override
                public void onResponse(List<MessageModel> list) {
                    if (list == null || list.size() == 0) {
                        return;
                    }
                    boolean isForeground = AppUtils.isRunningForegroundAndScreenOn(LPApp.getInstance());
                    if (!isForeground && UserSettingManager.getInstance().getMySettings().NewsNoticed == 1) {
                        notifyPushReceiverToSendNotification(list);
                    }
                }
            };
        }
        MessageManager.getInstance().saveAndPostMessage(list, listener);
    }

    private CommonLoadingListener<List<MessageModel>> mOnMessageSavedListener = new CommonLoadingListener<List<MessageModel>>() {
        @Override
        public void onResponse(List<MessageModel> list) {
            if (list == null || list.size() == 0) {
                return;
            }
            boolean isForeground = AppUtils.isRunningForegroundAndScreenOn(LPApp.getInstance());
            if (isForeground) {
//                notifyMainProcessToUpdateUI(list);
            } else if (UserSettingManager.getInstance().getMySettings().NewsNoticed == 1) {
                notifyPushReceiverToSendNotification(list);
            }
        }
    };

    private void notifyPushReceiverToSendNotification(List<MessageModel> list) {
        Observable.just(list)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<MessageModel>, Collection<String>>() {
                    @Override
                    public Collection<String> call(List<MessageModel> modelList) {
                        String currentUserId = AccountManager.getInstance().getCurrentUserId();
                        if (TextUtils.isEmpty(currentUserId)) {
                            return null;
                        }
                        HashSet<String> set = new HashSet<>();
                        for (MessageModel model : modelList) {
                            if (!TextUtils.equals(model.PosterID, currentUserId) && model.Type != MessageType.WITH_DRAWAL) {
                                set.add(model.targetId);
                            }
                        }
                        return set;
                    }
                })
                .filter(new Func1<Collection<String>, Boolean>() {
                    @Override
                    public Boolean call(Collection<String> list) {
                        return list != null && list.size() > 0;
                    }
                })
                .map(new Func1<Collection<String>, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call(Collection<String> collection) {
                        PushInfoDao.getInstance().add(collection);
                        ArrayList<String> list = new ArrayList<>();
                        list.addAll(collection);
                        return list;
                    }
                })
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> list) {
                        mDataListener.updateNotification(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void notifyMainProcessToUpdateUI(List<MessageModel> list) {
        ArrayList<String> idList = new ArrayList<>();
        for (MessageModel model : list) {
            idList.add(model.targetId);
        }
        mDataListener.onReceiveMessageModel(idList);
    }
}
