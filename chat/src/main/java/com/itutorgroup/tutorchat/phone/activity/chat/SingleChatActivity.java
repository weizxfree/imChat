package com.itutorgroup.tutorchat.phone.activity.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerActivity;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerDetailActivity;
import com.itutorgroup.tutorchat.phone.adapter.ChatMsgAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.beans.MessageModelByPage;
import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ClearChatHistoryEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageEvent;
import com.itutorgroup.tutorchat.phone.domain.event.OnlineEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateMessageReadStatusEvent;
import com.itutorgroup.tutorchat.phone.domain.inter.ChatType;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageStatus;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.inter.OnRetrySendClickLintener;
import com.itutorgroup.tutorchat.phone.domain.request.SendMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.response.SendMessageResponse;
import com.itutorgroup.tutorchat.phone.domain.response.UploadVoiceFileResponse;
import com.itutorgroup.tutorchat.phone.ui.FaceRelativeLayout;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.popup.AltMessagePopWindow;
import com.itutorgroup.tutorchat.phone.ui.popup.MessagePopWindow;
import com.itutorgroup.tutorchat.phone.ui.xlistview.XListView;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.MD5Util;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AudioSensorManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.FileManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MyActivityManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ServiceAccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler.RequestListener;
import com.itutorgroup.tutorchat.phone.utils.permission.PermissionsActivity;
import com.itutorgroup.tutorchat.phone.utils.permission.PermissionsManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;
import com.itutorgroup.tutorchat.phone.utils.voice.AudioRecorderButton;
import com.itutorgroup.tutorchat.phone.utils.voice.MediaManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tom_zxzhang on 2016/8/29.
 */
public class SingleChatActivity extends BaseActivity implements FaceRelativeLayout.onClickSendListener, XListView.IXListViewListener, OnRetrySendClickLintener {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;
    @InjectView
    EditText et_sendmessage;
    @InjectView
    XListView mlistView;
    @InjectExtra(key = "user_id")
    private String userId;
    @InjectExtra(key = "message_id")
    private String messageId;
    @InjectExtra(key = "service_account_id")
    private String serviceAccountId;
    @InjectView
    private FaceRelativeLayout faceRelativeLayout;
    @InjectView
    private AudioRecorderButton audioRecorderLayout;
    private int page = 1, scrollItemsCount, cancelMessageType;
    private boolean isTop, isServiceAccount, isBottom = true;
    private String unReadMessageId, targetID;
    private MessagePopWindow messagePopWindow;
    private AltMessagePopWindow altMessagePopWindow;
    private GestureDetectorCompat mGestureDetectorCompat;
    private ChatMsgAdapter adapter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_single);
        initListener();
        initView();
        loadUserInfoUpdate();
        registerReceiver(mCloseSystemDialogReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void loadUserInfoUpdate() {
        if (!TextUtils.isEmpty(userId)) {
            ObjectUpdateHelper.autoUpdate(ObjectUpdateHelper.TABLE_NAME_USER, userId);
        }
    }

    private int getChatType() {
        return isServiceAccount ? ChatType.SERVICE_ACCOUNT : ChatType.SINGLE;
    }

    private void initListener() {
        faceRelativeLayout.setOnClickSendListener(this);
        faceRelativeLayout.setContentView(mlistView);
        mlistView.setPullRefreshEnable(true);
        mlistView.setPullLoadEnable(false);
        mlistView.setXListViewListener(this);
        mlistView.setOnScrollListener(onXScrollListener);
        audioRecorderLayout.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(final float seconds, final String filePath) {
                final byte[] voiceBytes = FileUtils.File2byte(filePath);
                if (StringUtils.isBlank(voiceBytes) || voiceBytes.length == 0) {
                    ToastUtil.show(R.string.no_permissions);
                    return;
                }
                if (Math.ceil(seconds) == 0) {
                    ToastUtil.show(R.string.voice_time_is_too_short);
                    return;
                }
                final String content = MD5Util.getMD5String(voiceBytes);
                MessageManager.getInstance().createSendMessage(content, MessageType.VOICE, targetID, new CommonLoadingListener<MessageModel>() {
                    @Override
                    public void onResponse(MessageModel messageModel) {
                        if (StringUtils.isNotBlank(adapter)) {
                            adapter.addMsgToBottom(messageModel);
                            setSelectionBottom();
                        }
                        FileManager.getInstance().reNameVoiceFile(filePath, content);
                        sendVoiceMessage(messageModel);
                    }
                }, getChatType(), Math.ceil(seconds));

            }
        });
        audioRecorderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (MediaManager.isPlaying()) {
                        MediaManager.destroy();
                        AudioSensorManager.getInstance().unRegister();
                        if (StringUtils.isNotBlank(adapter)) {
                            adapter.stopVoiceAnim();
                        }
                    }
                }
                return false;
            }
        });


        et_sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isNotBlank(adapter)) {
                    setSelectionBottom();
                }
            }
        });

    }


    private void initData() {
        if (StringUtils.isEmpty(messageId)) {
            getDataByPage();
        } else if (!TextUtils.isEmpty(userId)) {
            MessageManager.getInstance().queryMessagesByMessageId(messageId, userId, new CommonLoadingListener<MessageModelByPage>() {
                @Override
                public void onResponse(MessageModelByPage messageModelByPage) {
                    if (Preconditions.isBlank(messageModelByPage.list))
                        return;
                    page = messageModelByPage.page;
                    adapter = new ChatMsgAdapter(mContext, messageModelByPage.list, true);
                    mlistView.setAdapter(adapter);
                    mlistView.setSelection(mlistView.getHeaderViewsCount() + messageModelByPage.index - 1);
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckUnreadMessageIsOver();
        if (!TextUtils.isEmpty(userId)) {
            MessageManager.getInstance().clearNotificationByTargetId(userId);
            ConversationManager.getInstance().loadDraft(userId, new CommonLoadingListener<String>() {
                @Override
                public void onResponse(String s) {
                    faceRelativeLayout.restoreTextWithoutWatcher(s);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!TextUtils.isEmpty(userId)) {
            ConversationManager.getInstance().setDraft(userId, et_sendmessage.getText().toString());
        }
    }

    private void initView() {
        AudioSensorManager.getInstance().setFloatView(mContext, mHeaderLayout);
        if (!TextUtils.isEmpty(userId)) {
            targetID = userId;
            UserInfoManager.getInstance().getUserInfo(userId, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(final UserInfo userInfo) {
                    mHeaderLayout.title(userInfo.Name)
                            .autoCancel((Activity) mContext)
                            .rightImage(R.drawable.more, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(SingleChatActivity.this, ChatDetailActivity.class).putExtra("user_info", userInfo));
                                }
                            });
//                    getDataByPage();
                }
            });
        } else if (!TextUtils.isEmpty(serviceAccountId)) {
            targetID = serviceAccountId;
            isServiceAccount = true;
            ServiceAccountModel accountModel = ServiceAccountManager.getInstance().getServiceAccount(serviceAccountId);
            if (StringUtils.isNotBlank(accountModel)) {
                mHeaderLayout.title(accountModel.Name).autoCancel(this);
            }
        }
        faceRelativeLayout.setTargetID(targetID, false);
        mGestureDetectorCompat = new GestureDetectorCompat(mContext, new MyGestureListener());
        mHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
        initData();
    }


    @Override
    public void retrySend(MessageModel messageModel) {
        switch (messageModel.Type) {
            case MessageType.TEXT:
                sendMessageToServer(messageModel);
                break;
            case MessageType.VOICE:
                sendVoiceMessage(messageModel);
                break;
            case MessageType.PIC:
                sendPicMessage(messageModel);
                break;
        }
    }

    @Override
    public void cancelSend(MessageModel messageModel) {
        sendMessageToServer(messageModel, true);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isTop) {
                mlistView.smoothScrollToPosition(0);
            } else {
                mlistView.startRefresh();
            }
            return true;
        }
    }


    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        if (Preconditions.isNotBlank(event.list) && !TextUtils.isEmpty(userId)) {
            MessageManager.getInstance().getMySingleMessage(userId, event.list, new CommonLoadingListener<List<MessageModel>>() {
                @Override
                public void onResponse(List<MessageModel> modelList) {
                    if (StringUtils.isBlank(adapter) || StringUtils.isBlank(modelList))
                        return;
                    checkCancelMessageStatus(modelList);
                    adapter.addMsgListToBottom(modelList);
                    if (isBottom) {
                        setSelectionBottom();
                    }
                    CheckScrollMessageIsOver();
                    MessageManager.getInstance().setMessageReadRequest(modelList, MessageManager.SINGLE_MESSAGE_ACTIVITY_NAME);
                    if (MyActivityManager.getInstance().isTopActivityResumed()) {
                        ConversationManager.getInstance().setConversationRead(userId);
                        ConversationManager.getInstance().updateMessageConversation(userId, null, modelList);
                    }
                }
            });

        }
    }


    @Override
    public void onClickSend() {
        if (StringUtils.isEmpty(et_sendmessage.getText().toString()) || "".equals(et_sendmessage.getText().toString().trim())) {
            et_sendmessage.setText("");
            return;
        }
        if (AppUtils.isFastClick()) {
            return;
        }
        final String content = et_sendmessage.getText().toString();
        et_sendmessage.setText("");
        MessageManager.getInstance().createSendMessage(content, MessageType.TEXT, targetID, new CommonLoadingListener<MessageModel>() {
            @Override
            public void onResponse(final MessageModel messageModel) {
                if (StringUtils.isNotBlank(adapter)) {
                    adapter.addMsgToBottom(messageModel);
                    setSelectionBottom();
                }
                Observable.just("").subscribeOn(Schedulers.io())
                        .map(new Func1<String, MessageModel>() {
                            @Override
                            public MessageModel call(String s) {
                                messageModel.Content = FaceConversionUtil.getInstace().sendExpressionZhToServer(content);
                                return messageModel;
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<MessageModel>() {
                            @Override
                            public void call(MessageModel messageModel) {
                                sendMessageToServer(messageModel);
                            }
                        });
            }
        }, getChatType());


    }

    @Subscribe
    public void onUpdateMessageReadStatusEvent(UpdateMessageReadStatusEvent event) {
        if (adapter == null)
            return;
        MessageManager.getInstance().getSingleReadMessage(adapter.getList(), event.readMessageId, new CommonLoadingListener<List<MessageModel>>() {
            @Override
            public void onResponse(List<MessageModel> modelList) {
                if (Preconditions.isNotBlank(modelList)) {
                    setAdapterData(modelList);
                }
            }
        });
    }


    private synchronized void sendMessageToServer(final MessageModel messageModel, boolean... isCancel) {

        if (isServiceAccount) {
            MessageManager.getInstance().SendServiceMessageRequest(serviceAccountId, messageModel.Content, messageModel.Type, new CommonLoadingListener<Boolean>() {
                @Override
                public void onResponse(Boolean aBoolean) {
                    if (aBoolean) {
                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                    } else {
                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE;
                    }
                    messageModel.IsHavePermissionAccessReadStatus = 0;
                    if (StringUtils.isNotBlank(adapter)) {
                        adapter.notifyDataSetChanged();
                    }
                    MessageDao.getInstance().updateMessageSendStatus(messageModel.MessageID, messageModel.MessageSendStatus);
                }
            });
        } else if (!TextUtils.isEmpty(userId)) {
            final SendMessageRequest sendMessageRequest = new SendMessageRequest();
            if (isCancel.length > 0 && isCancel[0]) {
                sendMessageRequest.Type = MessageType.WITH_DRAWAL;
                sendMessageRequest.Content = messageModel.MessageID;
            } else {
                sendMessageRequest.Type = messageModel.Type;
                sendMessageRequest.Content = messageModel.Content;
            }
            sendMessageRequest.init();
            sendMessageRequest.Priority = 1;
            sendMessageRequest.ReceiverID = messageModel.ReceiverID;
            sendMessageRequest.LocalID = messageModel.LocalId;
            new RequestHandler<SendMessageResponse>()
                    .operation(Operation.SEND_MESSAGE)
                    .request(sendMessageRequest)
                    .exec(SendMessageResponse.class, new RequestListener<SendMessageResponse>() {
                        @Override
                        public void onResponse(final SendMessageResponse response, Bundle bundle) {
                            if (StringUtils.isEmpty(response.MessageID) && !response.LocalID.equals(sendMessageRequest.LocalID))
                                return;
                            if (StringUtils.isNotEmpty(messageModel.MessageID) && messageModel.MessageID.equals(AccountManager.getInstance().getCurrentUserId() + messageModel.LocalId) && sendMessageRequest.Type != MessageType.WITH_DRAWAL) {
                                MessageManager.getInstance().updateLocalMessageByResponse(messageModel.MessageID, response.MessageID, MessageStatus.MESSAGE_SEND_OK, response.isReceipt, response.InsertTime, new CommonLoadingListener() {
                                    @Override
                                    public void onResponse(Object o) {
                                        messageModel.MessageID = response.MessageID;
                                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                                        messageModel.IsHavePermissionAccessReadStatus = response.isReceipt;
                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            } else if (StringUtils.isNotEmpty(messageModel.MessageID) && sendMessageRequest.Type == MessageType.WITH_DRAWAL) {

                                MessageManager.getInstance().getWithDrawalMessageContent(messageModel, new CommonLoadingListener<String>() {
                                    @Override
                                    public void onResponse(String s) {
                                        messageModel.Type = MessageType.WITH_DRAWAL;
                                        messageModel.Content = s;
                                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                                        MessageDao.getInstance().updateCancelMessage(messageModel.MessageID, messageModel.Content, MessageStatus.MESSAGE_SEND_OK);
                                        if (adapter != null) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(int errorCode, SendMessageResponse response, Exception e, Bundle bundle) {
                            super.onError(errorCode, response, e, bundle);
                            if (messageModel.Type == MessageType.WITH_DRAWAL) {
                                ToastUtil.show(getString(R.string.msg_operation_cancel_fail));
                                adapter.notifyDataSetChanged();
                                return;
                            }
                            messageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE; // 发送失败
                            adapter.notifyDataSetChanged();
                            MessageDao.getInstance().updateMessageSendStatus(messageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
                        }
                    });
        }
    }


    @Subscribe
    public void onClearChatHistoryEvent(ClearChatHistoryEvent event) {
        page = 1;
        getDataByPage();
    }


    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                page = page + 1;
                getDataByPage();
            }
        }, 500);
    }


    @Override
    public void onLoadMore() {

    }

    XListView.OnXScrollListener onXScrollListener = new XListView.OnXScrollListener() {
        @Override
        public void onXScrolling(View view) {

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                    isBottom = AppUtils.isListViewReachBottomEdge(view);
                    if (isBottom) {
                        if (altMessagePopWindow != null) {
                            altMessagePopWindow.dismiss();
                        }
                        if (mlistView != null) {
                            mlistView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        }
                    } else {
                        if (mlistView != null) {
                            mlistView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            scrollItemsCount = totalItemCount - firstVisibleItem - 2;
            if (firstVisibleItem == 0) {
                isTop = true;
            } else {
                isTop = false;
            }
            try {
                if (messagePopWindow != null && messagePopWindow.isShowing() && StringUtils.isNotBlank(adapter) && Preconditions.isNotBlank(adapter.getList())
                        && StringUtils.isNotEmpty(unReadMessageId) && TextUtils.equals(unReadMessageId, adapter.getList().get(firstVisibleItem).getMessageID())) {
                    messagePopWindow.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private void getDataByPage() {

        if (StringUtils.isEmpty(targetID))
            return;
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                List<MessageModel> list = null;
                try {
                    list = MessageDao.getInstance().queryMessageByPage(targetID, page);
                    if (Preconditions.isBlank(list)) {
                        list = new ArrayList<>();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                mlistView.stopRefresh(true);
                if (list.size() < Constant.PAGE_NUMBER) {
                    mlistView.setPullRefreshEnable(false);
                } else {
                    mlistView.setPullRefreshEnable(true);
                }
                if (page == 1) {
                    setAdapterData(list);
                    setSelectionBottom();
                    if (adapter != null) {
                        adapter.setIsServiceAccount(isServiceAccount);
                    }
                    ConversationManager.getInstance().updateMessageConversation(targetID, null, list);
                } else {
                    if (adapter != null) {
                        adapter.addMsgListToTop(list);
                        mlistView.setSelection(mlistView.getHeaderViewsCount() + list.size() - 1);
                    }
                }
            }
        }.execute();
    }

    private void setAdapterData(List<MessageModel> list) {
        if (adapter == null) {
            adapter = new ChatMsgAdapter(mContext, list, true);
            mlistView.setAdapter(adapter);
        } else {
            adapter.setData(list);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionsManager.REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            ToastUtil.show(getString(R.string.no_permissions));
        }
        if (requestCode == Constant.REQ_FROM_PHOTO && resultCode == RESULT_OK || requestCode == PhotoPickerActivity.REQUEST_PREVIEW && resultCode == PhotoPickerDetailActivity.RESULT_SEND) {
            ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
            final boolean sendOriginal = data.getBooleanExtra(PhotoPickerActivity.KEY_SEND_ORIGINAL, false);
            if (list != null && list.size() > 0) {
                for (String path : list) {
                    new AsyncTask<String, Void, byte[]>() {
                        @Override
                        protected byte[] doInBackground(String... params) {
                            try {
                                return FileManager.getInstance().getFileBytes(params[0], sendOriginal);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(byte[] bytes) {
                            try {
                                if (bytes.length == 0) {
                                    ToastUtil.show(R.string.pics_error);
                                    return;
                                }
                                MessageManager.getInstance().createSendMessage(MD5Util.getMD5String(bytes), MessageType.PIC, targetID, new CommonLoadingListener<MessageModel>() {
                                    @Override
                                    public void onResponse(MessageModel messageModel) {
                                        if (StringUtils.isNotBlank(adapter)) {
                                            adapter.addMsgToBottom(messageModel);
                                            setSelectionBottom();
                                        }
                                        sendPicMessage(messageModel);
                                    }
                                }, getChatType());
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute(path);
                }
            }
        }

        if (requestCode == Constant.REQ_FROM_CAMERA && resultCode == RESULT_OK) {
            ArrayList path = new ArrayList();
            path.add(Constant.CAMERA_FILE.getAbsolutePath());
            Intent intent = new Intent(mContext, PhotoPickerDetailActivity.class);
            intent.putExtra("position", 0);
            intent.putStringArrayListExtra("selected", path);
            intent.putExtra("original", false);
            intent.putExtra("max", 9);
            intent.putStringArrayListExtra("data", path);
            startActivityForResult(intent, PhotoPickerActivity.REQUEST_PREVIEW);
        }


    }


    private synchronized void sendPicMessage(final MessageModel PicMessageModel) {
        String path = FileManager.getInstance().getPathByFileId(PicMessageModel.Content);
        FileManager.getInstance().CheckFileIsExitRequest(FileUtils.File2byte(path), new CommonLoadingListener<Boolean>() {
            @Override
            public void onResponse(Boolean o) {
                if (o) {
                    sendMessageToServer(PicMessageModel);
                } else {
                    PicMessageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE;
                    adapter.notifyDataSetChanged();
                    MessageDao.getInstance().updateMessageSendStatus(PicMessageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
                }
            }
        });
    }

    private void sendVoiceMessage(final MessageModel voiceMessageModel) {
        final String filePath = FileManager.getInstance().getVoicePathByFileId(voiceMessageModel.Content);
        FileManager.getInstance().UploadVoiceFile(FileUtils.File2byte(filePath), voiceMessageModel.VoiceTime, new RequestListener<UploadVoiceFileResponse>() {
            @Override
            public void onResponse(UploadVoiceFileResponse response, Bundle bundle) {
                super.onResponse(response, bundle);
                sendMessageToServer(voiceMessageModel);
            }

            @Override
            public void onError(int errorCode, UploadVoiceFileResponse response, Exception e, Bundle bundle) {
                super.onError(errorCode, response, e, bundle);
                voiceMessageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE;
                adapter.notifyDataSetChanged();
                MessageDao.getInstance().updateMessageSendStatus(voiceMessageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
            }
        });
    }


    /**
     * 检查未读消息是否超过10条，如果超过10条显示
     */
    private void CheckUnreadMessageIsOver() {

        if (StringUtils.isEmpty(targetID))
            return;

        MessageManager.getInstance().getUnReadMessagesByTargetID(targetID, new CommonLoadingListener<List<MessageModel>>() {
            @Override
            public void onResponse(final List<MessageModel> modelList) {
                MessageManager.getInstance().setSingleMessageReaded(targetID, new CommonLoadingListener<Void>() {
                    @Override
                    public void onResponse(Void aVoid) {
                        ConversationManager.getInstance().setConversationRead(targetID);
                    }
                });
                if (Preconditions.isBlank(modelList))
                    return;
                unReadMessageId = modelList.get(0).MessageID;
                if (modelList.size() >= Constant.UNREAD_MESSAGE_COUNT_WILL_SHOW) {
                    if (messagePopWindow == null) {
                        messagePopWindow = new MessagePopWindow(SingleChatActivity.this);
                    }
                    mHeaderLayout.post(new Runnable() {
                        public void run() {
                            messagePopWindow.showAsDropDown(mHeaderLayout, ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth(), PixelUtil.dp2px(25));
                            messagePopWindow.setText(modelList.size());
                            messagePopWindow.mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageManager.getInstance().queryMessagesByMessageId(modelList.get(0).MessageID, targetID, new CommonLoadingListener<MessageModelByPage>() {
                                        @Override
                                        public void onResponse(final MessageModelByPage messageModelByPage) {
                                            if (StringUtils.isBlank(messageModelByPage) || Preconditions.isBlank(messageModelByPage.list))
                                                return;
                                            page = messageModelByPage.page;
                                            setAdapterData(messageModelByPage.list);
                                            mlistView.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mlistView.smoothScrollToPositionFromTop(mlistView.getHeaderViewsCount() + messageModelByPage.index - 1, 0);
                                                    messagePopWindow.dismiss();
                                                }
                                            }, 300);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * 当滚动条目大于N时有新消息时，给个悬浮窗可以直接滚动到底部
     */
    private void CheckScrollMessageIsOver() {
        if (!isBottom && scrollItemsCount >= Constant.SCROLL_MESSAGE_COUNT_WILL_SHOW) {
            if (altMessagePopWindow == null) {
                altMessagePopWindow = new AltMessagePopWindow(SingleChatActivity.this);
                altMessagePopWindow.setContent(LPApp.getInstance().getResources().getString(R.string.msg_new_come));
            }
            mlistView.post(new Runnable() {
                @Override
                public void run() {
                    altMessagePopWindow.showAsDropDown(mlistView, ((Activity) mContext).getWindowManager().getDefaultDisplay().getWidth(), -PixelUtil.dp2px(50));
                    altMessagePopWindow.mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isBottom = true;
                            altMessagePopWindow.dismiss();
                            setSelectionBottom();
                        }
                    });
                }
            });

        } else {
            if (altMessagePopWindow != null) {
                altMessagePopWindow.dismiss();
            }
        }
    }

    private void setSelectionBottom() {
        mlistView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter == null)
                    return;
                mlistView.setSelection(mlistView.getHeaderViewsCount() + adapter.getCount() - 1);
            }
        }, 200);
    }


    private void checkCancelMessageStatus(List<MessageModel> modelList) {
        if (Preconditions.isBlank(modelList) || StringUtils.isBlank(adapter) || Preconditions.isBlank(adapter.getList()))
            return;
        final Iterator modelIterator = modelList.iterator();
        while (modelIterator.hasNext()) {
            final MessageModel messageModel = (MessageModel) modelIterator.next();
            if (messageModel.Type == MessageType.WITH_DRAWAL) {
                Iterator it = adapter.getList().iterator();
                boolean needRemove = false;
                while (it.hasNext()) {
                    final MessageModel messageModel1 = (MessageModel) it.next();
                    if (messageModel1.MessageID.equals(messageModel.MessageID)) {
                        if (messageModel1.Type == MessageType.VOICE && MediaManager.isPlaying()) {
                            MediaManager.destroy();
                            AudioSensorManager.getInstance().unRegister();
                        }
                        messageModel1.Type = MessageType.WITH_DRAWAL;
                        needRemove = true;
                        MessageManager.getInstance().getWithDrawalMessageContent(messageModel1, new CommonLoadingListener<String>() {
                            @Override
                            public void onResponse(String s) {
                                messageModel1.Content = s;
                                adapter.notifyDataSetChanged();
//                                MessageDao.getInstance().modifyMessageContent(messageModel.MessageID,s);
                            }
                        });

                    }
                }
                if (needRemove) {
                    modelIterator.remove();
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(getApplicationContext()).pauseRequests();
    }

    @Subscribe
    public void onOnlineEvent(OnlineEvent event) {
        page = 1;
        getDataByPage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mCloseSystemDialogReceiver);
        AudioSensorManager.getInstance().unRegister();
        MediaManager.destroy();
        if (StringUtils.isNotBlank(adapter)) {
            adapter.stopVoiceAnim();
        }
        AudioSensorManager.getInstance().release();
    }

    private BroadcastReceiver mCloseSystemDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                Observable.just(this)
                        .subscribeOn(Schedulers.io())
                        .throttleFirst(100, TimeUnit.MILLISECONDS, Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<BroadcastReceiver>() {
                            @Override
                            public void call(BroadcastReceiver broadcastReceiver) {
                                AudioSensorManager.getInstance().unRegister();
                                MediaManager.destroy();
                                if (StringUtils.isNotBlank(adapter)) {
                                    adapter.stopVoiceAnim();
                                }
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        }
    };
}
