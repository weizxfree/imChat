package com.itutorgroup.tutorchat.phone.utils.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.ChatMsgAdapter;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.beans.MessageModelByPage;
import com.itutorgroup.tutorchat.phone.domain.beans.ReceiverMessageIDsWithType;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateGroupAnnoucementReadStatusEvent;
import com.itutorgroup.tutorchat.phone.domain.inter.ChatType;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageStatus;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.request.CheckIsReadRequest;
import com.itutorgroup.tutorchat.phone.domain.request.SendServiceMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.request.SetReadRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CheckIsReadResponse;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.MD5Util;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MessageManager {
    private static MessageManager sInstance;
    public static final String SINGLE_MESSAGE_ACTIVITY_NAME = Constant.SINGLE_MESSAGE_ACTIVITY_NAME;
    public static final String GROUP_MESSAGE_ACTIVITY_NAME = Constant.GROUP_MESSAGE_ACTIVITY_NAME;
    public static final int SET_READ_ISRECIPET_TYPE_1 = 1;
    public static final int SET_READ_ISRECIPET_TYPE_2 = 2;

    public static MessageManager getInstance() {
        if (sInstance == null) {
            synchronized (MessageManager.class) {
                if (sInstance == null) {
                    sInstance = new MessageManager();
                }
            }
        }
        return sInstance;
    }


    /*非公告信息默认在活动页面就是已读，而公告信息需要点击才算是已读*/
    public void setMessageReadRequest(final List<MessageModel> modelList, final String activityName) {
        if (!MyActivityManager.getInstance().isTopActivityResumed()) {
            return;
        }
        if (Preconditions.isBlank(modelList))
            return;

        new AsyncTask<Void, Void, ReceiverMessageIDsWithType>() {
            @Override
            protected ReceiverMessageIDsWithType doInBackground(Void... params) {
                List<String> groupAnnouncemntIDs = new ArrayList<String>();
                ReceiverMessageIDsWithType receiverMessageIDsWithType = new ReceiverMessageIDsWithType();
                receiverMessageIDsWithType.IsRecipetEqual1IDs = new ArrayList<String>();
                receiverMessageIDsWithType.IsRecipetEqual2IDs = new ArrayList<String>();
                if (activityName.equals(MessageManager.GROUP_MESSAGE_ACTIVITY_NAME)) {
                    for (MessageModel messageModel : modelList) {
                        if (messageModel.Type != MessageType.GROUPANNOUNCEMENT) {
                            if (messageModel.IsReceipt == SET_READ_ISRECIPET_TYPE_1) {
                                receiverMessageIDsWithType.IsRecipetEqual1IDs.add(messageModel.ReceiverMessageID);
                            } else if (messageModel.IsReceipt == SET_READ_ISRECIPET_TYPE_2) {
                                receiverMessageIDsWithType.IsRecipetEqual2IDs.add(messageModel.ReceiverMessageID);
                            }
                        } else {
                            groupAnnouncemntIDs.add(messageModel.MessageID);
                        }
                    }
                } else {
                    for (MessageModel messageModel : modelList) {
                        if (messageModel.Type != MessageType.VOICE) {
                            if (messageModel.IsReceipt == SET_READ_ISRECIPET_TYPE_1) {
                                receiverMessageIDsWithType.IsRecipetEqual1IDs.add(messageModel.ReceiverMessageID);
                            } else if (messageModel.IsReceipt == SET_READ_ISRECIPET_TYPE_2) {
                                receiverMessageIDsWithType.IsRecipetEqual2IDs.add(messageModel.ReceiverMessageID);
                            }
                        }
                    }
                }

                if (Preconditions.isNotBlank(groupAnnouncemntIDs) && groupAnnouncemntIDs.size() > 0) {
                    EventBusManager.getInstance().post(UpdateGroupAnnoucementReadStatusEvent.getInstance());
                }

                return receiverMessageIDsWithType;
            }

            @Override
            protected void onPostExecute(ReceiverMessageIDsWithType receiverMessageIDsWithTypes) {
                super.onPostExecute(receiverMessageIDsWithTypes);
                if (Preconditions.isNotBlank(receiverMessageIDsWithTypes.IsRecipetEqual1IDs)) {
                    SetReadRequestWithType(receiverMessageIDsWithTypes.IsRecipetEqual1IDs, SET_READ_ISRECIPET_TYPE_1);
                } else if (Preconditions.isNotBlank(receiverMessageIDsWithTypes.IsRecipetEqual2IDs)) {
                    SetReadRequestWithType(receiverMessageIDsWithTypes.IsRecipetEqual2IDs, SET_READ_ISRECIPET_TYPE_2);
                }
            }
        }.execute();

    }

    private void SetReadRequestWithType(List<String> receiverMessageIDs, int type) {
        if (Preconditions.isBlank(receiverMessageIDs))
            return;
        SetReadRequest readRequest = new SetReadRequest();
        readRequest.init();
        readRequest.receiverMessageIDs = receiverMessageIDs;
        readRequest.IsRecipet = type;
        new RequestHandler<CommonResponse>()
                .operation(Operation.SET_READ)
                .bundle(new NBundle().addIgnoreToastErrorCode(NetworkError.ERROR_INVALID_UNREAD_MESSAGE_ID).build())
                .request(readRequest)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        if (response != null) {


                        }
                    }
                });
    }


    public void setGroupAnnouncementIsReadFromReceiverID(final MessageModel messageModel) {

        if (StringUtils.isBlank(messageModel))
            return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MessageDao.getInstance().updateGroupAnnouncementReadMessage(messageModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<String> receiverMessageIDs = new ArrayList<>();
                List<String> messageIdList = new ArrayList<>();
                receiverMessageIDs.add(messageModel.ReceiverMessageID);
                messageIdList.add(messageModel.MessageID);
                SetReadRequest readRequest = new SetReadRequest();
                readRequest.init();
                readRequest.receiverMessageIDs = receiverMessageIDs;
                new RequestHandler<CommonResponse>()
                        .operation(Operation.SET_READ)
                        .bundle(new NBundle().addIgnoreToastErrorCode(NetworkError.ERROR_INVALID_UNREAD_MESSAGE_ID).build())
                        .request(readRequest)
                        .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                            @Override
                            public void onResponse(CommonResponse response, Bundle bundle) {
                            }

                            @Override
                            public void onError(int errorCode, CommonResponse response, Exception e, Bundle bundle) {
                            }

                            @Override
                            public void onNullResponse(Bundle bundle) {
                                super.onNullResponse(bundle);
                            }
                        });

                return null;
            }

        }.execute();


    }


    /**
     * 单聊每次打开页面，默认单聊所有信息均已读过
     */
    public static void setSingleMessageReaded(final String received, final CommonLoadingListener<Void> listener) {

        if (StringUtils.isEmpty(received))
            return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<String> Type1List = null;
                List<String> Type2List = null;
                try {
                    Type1List = MessageDao.getInstance().queryReceiveMessageIDS(AccountManager.getInstance().getCurrentUserId(), received, SET_READ_ISRECIPET_TYPE_1);
                    Type2List = MessageDao.getInstance().queryReceiveMessageIDS(AccountManager.getInstance().getCurrentUserId(), received, SET_READ_ISRECIPET_TYPE_2);
                    if (listener != null) {
                        listener.onResponse(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Preconditions.isNotBlank(Type1List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type1List, SET_READ_ISRECIPET_TYPE_1);
                } else if (Preconditions.isNotBlank(Type2List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type2List, SET_READ_ISRECIPET_TYPE_2);
                }
                return null;
            }

        }.execute();
    }

    public static void setSingleMessageReaded(final String received) {

        if (StringUtils.isEmpty(received))
            return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<String> Type1List = null;
                List<String> Type2List = null;
                try {
                    Type1List = MessageDao.getInstance().queryReceiveMessageIDS(AccountManager.getInstance().getCurrentUserId(), received, SET_READ_ISRECIPET_TYPE_1);
                    Type2List = MessageDao.getInstance().queryReceiveMessageIDS(AccountManager.getInstance().getCurrentUserId(), received, SET_READ_ISRECIPET_TYPE_2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Preconditions.isNotBlank(Type1List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type1List, SET_READ_ISRECIPET_TYPE_1);
                } else if (Preconditions.isNotBlank(Type2List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type2List, SET_READ_ISRECIPET_TYPE_2);
                }
                return null;
            }

        }.execute();
    }


    /*设置群聊全部已读*/
    public static void setGroupMessageReaded(final String groupId) {

        if (StringUtils.isEmpty(groupId))
            return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<String> Type1List = null;
                List<String> Type2List = null;
                try {
                    Type1List = MessageDao.getInstance().queryUnReadReceiverMessageIDSFromGroup(AccountManager.getInstance().getCurrentUserId(), groupId, SET_READ_ISRECIPET_TYPE_1);
                    Type2List = MessageDao.getInstance().queryUnReadReceiverMessageIDSFromGroup(AccountManager.getInstance().getCurrentUserId(), groupId, SET_READ_ISRECIPET_TYPE_2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Preconditions.isNotBlank(Type1List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type1List, SET_READ_ISRECIPET_TYPE_1);
                } else if (Preconditions.isNotBlank(Type2List)) {
                    MessageManager.getInstance().SetReadRequestWithType(Type2List, SET_READ_ISRECIPET_TYPE_2);
                }
                return null;
            }

        }.execute();
    }


    /**
     * 检查消息是否已读
     *
     * @param
     */
    public static synchronized void CheckIsReadRequest(final String receiverd, final boolean isSingleChat) {

        if (StringUtils.isEmpty(receiverd))
            return;

        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {
                List<String> list = null;
                try {
                    if (isSingleChat) {
                        list = MessageDao.getInstance().queryUnReadMessageIDS(AccountManager.getInstance().getCurrentUserId(), receiverd);
                    } else {
                        list = MessageDao.getInstance().queryUnReadMessageIDSFromGroup(AccountManager.getInstance().getCurrentUserId(), receiverd, Constant.MESSAGE_ID, false);
                    }
                    if (Preconditions.isBlank(list))
                        return null;
                    CheckIsReadRequest readRequest = new CheckIsReadRequest();
                    readRequest.init();
                    readRequest.MessageIDs = list;
                    new RequestHandler<CheckIsReadResponse>()
                            .operation(Operation.CHECK_IS_READ)
                            .bundle(new NBundle().ignoreResponseLog().build())
                            .request(readRequest)
                            .exec(CheckIsReadResponse.class, new RequestHandler.RequestListener<CheckIsReadResponse>() {
                                @Override
                                public void onResponse(CheckIsReadResponse response, Bundle bundle) {
                                    if (response != null) {
                                        saveReadMessage(isSingleChat, response.ReadInfoList);
                                    }
                                }
                            });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }

        }.execute();

    }


    /**
     * 从adapter目前绑定的list来查询，日后可以优化到只更新当前屏的数据
     *
     * @param
     */
    public synchronized void checkMessagesReadState(final List<MessageModel> messageModelList, final boolean isSingleChat, final CommonLoadingListener<List<CheckIsReadResponse.ReadModel>> listener) {

        if (Preconditions.isBlank(messageModelList))
            return;

        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {
                /*CopyOnWriteArrayList 适合读多写少的情况，用以解决同步问题*/
                CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<String>();
                if (isSingleChat) {
                    for (MessageModel messageModel : messageModelList) {
                        if (StringUtils.isNotEmpty(messageModel.MessageID) && messageModel.IsRead == 0) {
                            list.add(messageModel.MessageID);
                        }
                    }
                } else {
                    for (MessageModel messageModel : messageModelList) {
                        if (StringUtils.isNotEmpty(messageModel.MessageID) && messageModel.GroupUnReadNumbers > 0) {
                            list.add(messageModel.MessageID);
                        }
                    }
                }
                if (Preconditions.isBlank(list))
                    return null;
                CheckIsReadRequest readRequest = new CheckIsReadRequest();
                readRequest.init();
                readRequest.MessageIDs = list;
                new RequestHandler<CheckIsReadResponse>()
                        .operation(Operation.CHECK_IS_READ)
                        .bundle(new NBundle().ignoreResponseLog().build())
                        .request(readRequest)
                        .exec(CheckIsReadResponse.class, new RequestHandler.RequestListener<CheckIsReadResponse>() {
                            @Override
                            public void onResponse(CheckIsReadResponse response, Bundle bundle) {
                                if (response != null) {
                                    saveReadMessage(isSingleChat, response.ReadInfoList);
                                    if (listener != null) {
                                        listener.onResponse(response.ReadInfoList);
                                    }
                                }
                            }
                        });

                return list;
            }

        }.execute();

    }


    public void getMySingleMessage(final String UserID, final List<MessageModel> list, final CommonLoadingListener<List<MessageModel>> listener) {

        if (Preconditions.isBlank(list) || StringUtils.isEmpty(UserID))
            return;
        final List<MessageModel> messageModelList = new ArrayList<>();
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                for (MessageModel messageModel : list) {
                    if (TextUtils.equals(ConversationManager.getInstance().getMessageTargetId(messageModel), UserID)
                            && TextUtils.equals(messageModel.currentUserId, AccountManager.getInstance().getCurrentUserId())) {
                        messageModelList.add(messageModel);
                    }
                }
                return messageModelList;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (listener != null) {
                    listener.onResponse(list);
                }
            }
        }.execute();
    }


    public void getMyGroupMessage(final String GroupId, final List<MessageModel> list, final CommonLoadingListener<List<MessageModel>> listener) {

        if (Preconditions.isBlank(list) || StringUtils.isEmpty(GroupId))
            return;
        final List<MessageModel> messageModelList = new ArrayList<>();
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {

                for (MessageModel messageModel : list) {
                    if (TextUtils.equals(messageModel.currentUserId, AccountManager.getInstance().getCurrentUserId())
                            && TextUtils.equals(messageModel.GroupId, GroupId)) {
                        messageModelList.add(messageModel);
                    }
                }
                return messageModelList;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (listener != null) {
                    listener.onResponse(list);
                }
            }
        }.execute();
    }

    public synchronized void saveAndPostMessage(final List<MessageModel> modelList, final CommonLoadingListener<List<MessageModel>> listener) {
        if (Preconditions.isBlank(modelList))
            return;
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                // 需要落库的数据
                List<MessageModel> list = new ArrayList();
                // 撤回的消息id列表
                List<String> recallList = new ArrayList<>();
                for (MessageModel messageModel : modelList) {
                    messageModel.ReceiverMessageID = messageModel.MessageID;
                    messageModel.currentUserId = AccountManager.getInstance().getCurrentUserId();
                    messageModel.targetId = ConversationManager.getInstance().getMessageTargetId(messageModel);
                    messageModel.targetType = TextUtils.isEmpty(messageModel.GroupId) ? ChatType.SINGLE : ChatType.GROUP;
                    if (messageModel.IsSelf == 1) { // 从PC端同步过来的消息
                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                        messageModel.IsHavePermissionAccessReadStatus = messageModel.IsReceipt;
                    }
                    if (messageModel.Type == MessageType.WITH_DRAWAL) {
                        // 撤回消息的Content表示将被撤回的MessageId
                        recallList.add(messageModel.Content);
                        messageModel.MessageID = messageModel.Content;
                    } else if (messageModel.Type == MessageType.SYSTEM_NOTICE) {
                        SystemNoticeManager.getInstance().receiveNoticeMessage(messageModel);
                    } else {
                        if (messageModel.Type == MessageType.TEXT) {
                            messageModel.Content = FaceConversionUtil.getInstace().getExpressionZhFromServer(messageModel.Content);
                        }
                        list.add(messageModel);
                    }
                    if (list.size() != 0) {
                        MessageDao.getInstance().add(list);
                    }
                    if (recallList.size() != 0) {
                        MessageDao.getInstance().updateMessageType(recallList);
                    }
                    if (modelList != null && modelList.size() > 0) {
                        EventBusManager.getInstance().post(new MessageEvent(modelList));
                    }
                }
                return modelList;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (listener != null) {
                    listener.onResponse(list);
                }
            }
        }.execute();
    }

    public synchronized void SaveAndPostMessage(final List<MessageModel> modelList) {
//        saveAndPostMessage(modelList, null);
    }

    /**
     * @param modelList
     * @return PosterIdList
     */
    private void notifyMessageByEventBus(List<MessageModel> modelList) {
        List<String> cancelMessageList = new ArrayList<>();
        for (MessageModel messageModel : modelList) {
            if (messageModel.IsSelf == 1) { // 从PC端同步过来的消息
                messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                messageModel.IsHavePermissionAccessReadStatus = messageModel.IsReceipt;
            }
            if (messageModel.Type == MessageType.WITH_DRAWAL) {
                cancelMessageList.add(messageModel.Content);
            }
            messageModel.ReceiverMessageID = messageModel.MessageID;
            messageModel.currentUserId = AccountManager.getInstance().getCurrentUserId();
            messageModel.targetId = ConversationManager.getInstance().getMessageTargetId(messageModel);
        }
        MessageDao.getInstance().updateMessageType(cancelMessageList);
        EventBusManager.getInstance().post(new MessageEvent(modelList));
    }

    public synchronized void getGroupReadMessage(final List<MessageModel> modelList, final List<CheckIsReadResponse.ReadModel> readModels, final CommonLoadingListener<List<MessageModel>> listener) {

        if (Preconditions.isBlank(modelList) || Preconditions.isBlank(readModels))
            return;
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                try {
                    for (MessageModel messageModel : modelList) {
                        for (CheckIsReadResponse.ReadModel readModel : readModels) {
                            if (StringUtils.isNotEmpty(messageModel.MessageID) && readModel.MessageID.equals(messageModel.MessageID)) {
                                messageModel.GroupUnReadNumbers = readModel.UserNum - readModel.ReadCount;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return modelList;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (listener != null) {
                    listener.onResponse(list);
                }
            }
        }.execute();
    }

    public synchronized void getSingleReadMessage(final List<MessageModel> modelList, final List<String> messageIds, final CommonLoadingListener<List<MessageModel>> listener) {
        if (Preconditions.isBlank(modelList) || Preconditions.isBlank(messageIds))
            return;

        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                try {
                    for (MessageModel messageModel : modelList) {
                        for (String messageId : messageIds) {
                            if (StringUtils.isNotEmpty(messageModel.MessageID) && messageId.equals(messageModel.MessageID)) {
                                messageModel.IsRead = 1;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return modelList;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (listener != null) {
                    listener.onResponse(list);
                }
            }
        }.execute();
    }


    public void addMessage(final MessageModel messageModel) {

        MessageDao.getInstance().add(messageModel);

    }


    public void removeMessageById(final String messageId) {
        Observable.just(messageId)
                .observeOn(Schedulers.io())
                .map(new Func1<String, Void>() {
                    @Override
                    public Void call(String id) {
                        MessageDao.getInstance().RemoveMessageById(messageId);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBusManager.getInstance().post(ConversationEvent.getInstance());
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }


    public void updateLocalMessageByResponse(final String oldMessageId, final String newMessageId, final int messageSendStatus, final int isHavePermissionAccessReadStatus, final long insertTime ,final CommonLoadingListener listener) {
        Observable.just(oldMessageId)
                .observeOn(Schedulers.io())
                .map(new Func1<String, Void>() {
                    @Override
                    public Void call(String id) {
                        MessageDao.getInstance().UpdateMessageId(oldMessageId, newMessageId, messageSendStatus, isHavePermissionAccessReadStatus, insertTime);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (listener != null) {
                            listener.onResponse(null);
                        }
                    }

                }, CommonUtil.ACTION_EXCEPTION);
    }


    public static void saveReadMessage(final boolean isSingleChat, final List<CheckIsReadResponse.ReadModel> ReadInfoList) {
        if (Preconditions.isBlank(ReadInfoList))
            return;
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                if (isSingleChat) {
                    List<String> hasReadMessageID = new ArrayList<String>();
                    for (CheckIsReadResponse.ReadModel model : ReadInfoList) {
                        if (model.ReadCount == 1) {
                            hasReadMessageID.add(model.MessageID);
                        }
                    }
                    if (Preconditions.isBlank(hasReadMessageID))
                        return null;
                    try {
                        MessageDao.getInstance().updateReadMessage(hasReadMessageID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        MessageDao.getInstance().updateGroupReadMessage(ReadInfoList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

        }.execute();
    }

    /**
     * 查询历史的公告栏未读数量
     *
     * @param groupId
     * @param listener
     */
    public void queryGroupAnnouncementUnReadCount(final String groupId, final CommonLoadingListener<Long> listener) {

        Observable.just(groupId).subscribeOn(Schedulers.io())
                .map(new Func1<String, Long>() {
                    @Override
                    public Long call(String id) {
                        try {
                            return MessageDao.getInstance().queryAnnoucementUnReadCount(AccountManager.getInstance().getCurrentUserId(), groupId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Long.valueOf(-1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long count) {
                        if (listener != null) {
                            listener.onResponse(count);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);

    }


    public void queryMessagesByMessageId(final String messageId, final String targetId, final CommonLoadingListener<MessageModelByPage> listener) {

        Observable.just("").subscribeOn(Schedulers.io())
                .map(new Func1<String, MessageModelByPage>() {
                    @Override
                    public MessageModelByPage call(String id) {
                        try {
                            return MessageDao.getInstance().getMessagesByMessageId(targetId, messageId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MessageModelByPage>() {
                    @Override
                    public void call(MessageModelByPage messageModelByPage) {
                        if (listener != null) {
                            listener.onResponse(messageModelByPage);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }


    /*查询alt 列表*/
    public void queryHasAltMessagesByMessageId(final String targetId, final CommonLoadingListener<List<MessageModel>> listener) {
        Observable.just("").subscribeOn(Schedulers.io())
                .map(new Func1<String, List<MessageModel>>() {
                    @Override
                    public List<MessageModel> call(String id) {
                        return MessageDao.getInstance().getHasAltMeMessageWithTargetId(targetId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MessageModel>>() {
                    @Override
                    public void call(List<MessageModel> list) {
                        if (listener != null) {
                            listener.onResponse(list);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }


    /**
     * 根据 targetId 获取未读消息列表
     *
     * @param targetId
     */

    public List<MessageModel> getUnReadMessagesByTargetID(final String targetId, final CommonLoadingListener<List<MessageModel>> listener) {
        Observable.just("").subscribeOn(Schedulers.io())
                .map(new Func1<String, List<MessageModel>>() {
                    @Override
                    public List<MessageModel> call(String id) {
                        return MessageDao.getInstance().getAllUnreadMessagesByTargetID(targetId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MessageModel>>() {
                    @Override
                    public void call(List<MessageModel> list) {
                        if (listener != null) {
                            listener.onResponse(list);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
        return null;
    }


    public void notifyReceivedMessage(ArrayList<String> idList) {
        if (idList != null && idList.size() > 0) {
            Observable.just(idList)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ArrayList<String>, List<MessageModel>>() {
                        @Override
                        public List<MessageModel> call(ArrayList<String> list) {
                            EventBusManager.getInstance().post(ConversationEvent.getInstance());
                            return MessageDao.getInstance().queryMessagesWithId(list);
                        }
                    })
                    .filter(new Func1<List<MessageModel>, Boolean>() {
                        @Override
                        public Boolean call(List<MessageModel> list) {
                            return list != null && list.size() > 0;
                        }
                    })
                    .subscribe(new Action1<List<MessageModel>>() {
                        @Override
                        public void call(List<MessageModel> models) {
                            notifyMessageByEventBus(models);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public String getMessageConversationText(MessageModel message, CommonLoadingListener<String> listener) {
        String content = "";
        switch (message.Type) {
            case MessageType.FILE:
                content = LPApp.getInstance().getString(R.string.msg_type_file);
                break;
            case MessageType.PIC:
                content = LPApp.getInstance().getString(R.string.msg_type_pic);
                break;
            case MessageType.VOICE:
                content = LPApp.getInstance().getString(R.string.msg_type_voice);
                break;
            case MessageType.SYSTEM_MESSAGE:
                getSystemMessage(message, listener);
                break;
            case MessageType.WITH_DRAWAL:
                getWithDrawalMessageContent(message, listener);
                content = message.Content;
                break;
            default:
                content = message.Content;
                break;
        }
        if (listener != null && message.Type != MessageType.SYSTEM_MESSAGE && message.Type != MessageType.WITH_DRAWAL) {
            listener.onResponse(content);
        }
        return content;
    }


    public void getMessageNotificationText(MessageModel model, final CommonLoadingListener<String> listener) {
        int resId = 0;
        switch (model.Type) {
            case MessageType.FILE:
                resId = R.string.msg_notification_receive_file;
                break;
            case MessageType.PIC:
                resId = R.string.msg_notification_receive_pic;
                break;
            case MessageType.VOICE:
                resId = R.string.msg_notification_receive_voice;
                break;
            case MessageType.GROUPANNOUNCEMENT:
                String content = LPApp.getInstance().getString(R.string.msg_notification_receive_group_announcement);
                CommonUtil.sendResponse(content, listener);
                return;
            case MessageType.SYSTEM_MESSAGE:
                getSystemMessage(model, listener);
                return;
            case MessageType.WITH_DRAWAL:
                getWithDrawalMessageContent(model, listener);
                return;
            case MessageType.TEXT:
                String currentUserId = AccountManager.getInstance().getCurrentUserId();
                if (TextUtils.isEmpty(currentUserId)) {
                    return;
                }
                if (model.AltReceivers != null && model.AltReceivers.size() > 0 && model.AltReceivers.contains(currentUserId)) {
                    resId = R.string.msg_notification_receive_alt_me;
                } else {
                    CommonUtil.sendResponse(model.Content, listener);
                    return;
                }
                break;
        }
        String posterId = model.PosterID;
        if (!TextUtils.isEmpty(posterId)) {
            final int finalResId = resId;
            UserInfoManager.getInstance().getUserInfo(posterId, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo userInfo) {
                    String content = LPApp.getInstance().getString(finalResId, userInfo.Name);
                    CommonUtil.sendResponse(content, listener);
                }
            });
        }
    }

    public void getWithDrawalMessageContent(MessageModel message, final CommonLoadingListener<String> listener) {
        if (listener == null) {
            return;
        }
        String posterId = message.PosterID;
        if (TextUtils.equals(posterId, AccountManager.getInstance().getCurrentUserId())) {
            String content = LPApp.getInstance().getString(R.string.withdrawal_message_by_self);
            listener.onResponse(content);
            return;
        } else {
            UserInfoManager.getInstance().getUserInfo(posterId, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo userInfo) {
                    String currentUserId = AccountManager.getInstance().getCurrentUserId();
                    String poster = "";
                    if (!TextUtils.isEmpty(currentUserId) && !currentUserId.equals(userInfo.UserID)) {
                        poster = userInfo.Name;
                    } else {
                        poster = "";
                    }
                    String content = LPApp.getInstance().getString(R.string.withdrawal_message_by_user, poster);
                    listener.onResponse(content);
                }
            });
        }
    }


    public void getSystemMessage(MessageModel item, final CommonLoadingListener<String> listener) {
        try {
            JSONObject myJsonObject = new JSONObject(item.Content);
            final int operation = myJsonObject.getInt("OP");
            String userId = myJsonObject.getString("Master");
            final List<String> userIDs = JSON.parseArray(myJsonObject.getString("values"), String.class);
            if (userId.equals(AccountManager.getInstance().getCurrentUserId())) {
                getUserNames(operation, userIDs, LPApp.getInstance().getString(R.string.msg_system_master_isself), listener);
            } else {
                UserInfoManager.getInstance().getUserInfo(item.PosterID, new CommonLoadingListener<UserInfo>() {
                    @Override
                    public void onResponse(final UserInfo userInfoResponse) {
                        getUserNames(operation, userIDs, userInfoResponse.Name, listener);
                    }
                });
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onResponse(item.Content);
            }
        }
    }

    private void getUserNames(final int operation, final List<String> userIDs, final String masterName, final CommonLoadingListener<String> listener) {
        final StringBuilder receiverName = new StringBuilder();
        List<String> newUserIDs = new ArrayList<>();
        if (operation == 4) { //改群名
            receiverName.append(userIDs.get(1));
            if (listener != null) {
                listener.onResponse(getResult(operation, masterName, receiverName.toString()));
            }
        }
        if (Preconditions.isBlank(userIDs))
            return;
        for (String userID : userIDs) {
            if (StringUtils.isNotEmpty(userID)) {
                newUserIDs.add(userID);
            }
        }
        UserInfoManager.getInstance().forceGetUserList(newUserIDs, new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> userInfoList) {
                if (Preconditions.isBlank(userInfoList))
                    return;
                for (UserInfo userInfo : userInfoList) {
                    receiverName.append(userInfo.Name + "，");
                }
                receiverName.deleteCharAt(receiverName.length() - 1);
                if (listener != null) {
                    listener.onResponse(getResult(operation, masterName, receiverName.toString()));
                }
            }
        });
    }


    public String getResult(int operation, String masterName, String receiverName) {
        switch (operation) {
            case 1:
                return LPApp.getInstance().getString(R.string.msg_system_join_group, masterName, receiverName);
            case 2:
                return LPApp.getInstance().getString(R.string.msg_system_remove_group, masterName, receiverName);
            case 3:
                return LPApp.getInstance().getString(R.string.msg_system_transfer_group, masterName, receiverName);
            case 4:
                return LPApp.getInstance().getString(R.string.msg_system_rename_group, masterName, receiverName);
            case 5:
                return LPApp.getInstance().getString(R.string.msg_system_setadmin_group, masterName, receiverName);
            case 6:
                return LPApp.getInstance().getString(R.string.msg_system_canceladmin_group, masterName, receiverName);
        }
        return null;

    }

    public void loadEmojiText(final Context context, final TextView tv, final String text) {
        loadEmojiText(context, tv, text, 50);
    }

    public void loadEmojiText(final Context context, final TextView tv, final String text, final int size) {
        if (tv != null) {
            Observable.just(tv)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<TextView, SpannableString>() {
                        @Override
                        public SpannableString call(TextView tv) {
                            tv.setTag(R.id.tag_text, text);
                            if (TextUtils.isEmpty(text)) {
                                return null;
                            } else {
                                return FaceConversionUtil.getInstace().getExpressionString(context, text, size);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<SpannableString>() {
                        @Override
                        public void call(SpannableString ss) {
                            if (tv.getTag(R.id.tag_text) != null && tv.getTag(R.id.tag_text).equals(text)) {
                                tv.setText(ss == null ? "" : ss);
                                if (tv instanceof EditText) {
                                    ((EditText) tv).setSelection(tv.getText().length());
                                }
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public void loadEmojiText(final Context context, final TextView tv, final String text, final int size, final CommonLoadingListener<Void> listener) {
        if (tv != null) {
            Observable.just(tv)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<TextView, SpannableString>() {
                        @Override
                        public SpannableString call(TextView tv) {
                            tv.setTag(R.id.tag_text, text);
                            if (TextUtils.isEmpty(text)) {
                                return null;
                            } else {
                                return FaceConversionUtil.getInstace().getExpressionString(context, text, size);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<SpannableString>() {
                        @Override
                        public void call(SpannableString ss) {
                            if (tv.getTag(R.id.tag_text) != null && tv.getTag(R.id.tag_text).equals(text)) {
                                tv.setText(ss == null ? "" : ss);
                                if (tv instanceof EditText) {
                                    ((EditText) tv).setSelection(tv.getText().length());
                                }
                                if(listener != null){
                                    listener.onResponse(null);
                                }
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public void loadEmojiText(final Context context, final TextView tv, final String text, final int size, final ChatMsgAdapter.NoUnderlineSpan span) {
        if (tv != null) {
            Observable.just(tv)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<TextView, SpannableString>() {
                        @Override
                        public SpannableString call(TextView tv) {
                            tv.setTag(R.id.tag_text, text);
                            if (TextUtils.isEmpty(text)) {
                                return null;
                            } else {
                                return FaceConversionUtil.getInstace().getExpressionString(context, text, size);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<SpannableString>() {
                        @Override
                        public void call(SpannableString ss) {
                            synchronized (this) {
                                if (tv.getTag(R.id.tag_text) != null && tv.getTag(R.id.tag_text).equals(text)) {
                                    tv.setText(ss == null ? "" : ss);
                                    if (tv instanceof EditText) {
                                        ((EditText) tv).setSelection(tv.getText().length());
                                    }
                                    if (tv.getText() instanceof Spannable) {
                                        Spannable s = (Spannable) tv.getText();
                                        s.setSpan(span, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                }
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }


    public void clearNotificationByTargetId(String targetId) {
        PushInfoManager.getInstance().clearTargetNotification(targetId);
        ConversationManager.getInstance().modifyConversationRead(targetId);
    }


    public void createSendMessage(final String content, final int type, final String targetId, final CommonLoadingListener<MessageModel> listener, int targetType, final double... voiceTime) {
        createSendMessage(content, type, targetId, null, false, listener, targetType, voiceTime);
    }

    public void createSendMessage(final String content, final int type, final String targetId, final ArrayList<String> altList, final boolean isGroup, final CommonLoadingListener<MessageModel> listener, final int targetType, final double... voiceTime) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(targetId))
            return;
        Observable.just("").subscribeOn(Schedulers.io())
                .map(new Func1<String, MessageModel>() {
                    @Override
                    public MessageModel call(String id) {
                        try {
                            MessageModel messageModel = new MessageModel();
                            messageModel.Type = type;
                            if (MessageType.VOICE == messageModel.Type) {
                                messageModel.VoiceTime = (float) voiceTime[0];
                            }
                            if (isGroup) {
                                messageModel.GroupId = targetId;
                                if (Preconditions.isNotBlank(altList)) {
                                    messageModel.AltReceivers = altList;
                                }
                            } else {
                                messageModel.ReceiverID = targetId;
                            }
                            messageModel.IsRead = 0;
                            messageModel.PosterID = AccountManager.getInstance().getCurrentUserId();
                            messageModel.CreateTime = System.currentTimeMillis();
                            messageModel.IsSelf = 1;
                            messageModel.currentUserId = AccountManager.getInstance().getCurrentUserId();
                            messageModel.Content = type == MessageType.TEXT ? FaceConversionUtil.getInstace().getExpressionZhFromServer(content) : content;
                            messageModel.LocalId = MD5Util.getMD5String(System.currentTimeMillis() + "");
                            messageModel.MessageID = AccountManager.getInstance().getCurrentUserId() + messageModel.LocalId;
                            messageModel.targetType = targetType;
                            MessageManager.getInstance().addMessage(messageModel);
                            return messageModel;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MessageModel>() {
                    @Override
                    public void call(MessageModel messageModel) {
                        if (listener != null && StringUtils.isNotBlank(messageModel)) {
                            listener.onResponse(messageModel);
                        }

                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }


    public void SendServiceMessageRequest(String serviceAccountId, String content, int type, final CommonLoadingListener<Boolean> listener) {

        SendServiceMessageRequest sendServiceMessageRequest = new SendServiceMessageRequest();
        sendServiceMessageRequest.init();
        sendServiceMessageRequest.ServiceAccountId = serviceAccountId;
        sendServiceMessageRequest.content = content;
        sendServiceMessageRequest.Type = type;
        new RequestHandler<CommonResponse>()
                .operation(Operation.SEND_SERVICE_MESSAGE)
                .request(sendServiceMessageRequest)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        listener.onResponse(true);
                    }

                    @Override
                    public void onError(int errorCode, CommonResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        listener.onResponse(false);
                    }
                });
    }


}
