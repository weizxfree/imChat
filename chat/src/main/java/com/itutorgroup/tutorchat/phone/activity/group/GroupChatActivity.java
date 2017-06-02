/**
 *
 */
package com.itutorgroup.tutorchat.phone.activity.group;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerActivity;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerDetailActivity;
import com.itutorgroup.tutorchat.phone.adapter.ChatMsgAdapter;
import com.itutorgroup.tutorchat.phone.adapter.group.GroupMemberSelectAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.beans.MessageModelByPage;
import com.itutorgroup.tutorchat.phone.domain.beans.SystemMessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ClearChatHistoryEvent;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageReadEvent;
import com.itutorgroup.tutorchat.phone.domain.event.OnlineEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateGroupAnnoucementReadStatusEvent;
import com.itutorgroup.tutorchat.phone.domain.inter.ChatType;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageStatus;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.inter.OnHeadImgLongClickListener;
import com.itutorgroup.tutorchat.phone.domain.inter.OnRetrySendClickLintener;
import com.itutorgroup.tutorchat.phone.domain.request.SendGroupMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.response.SendMessageResponse;
import com.itutorgroup.tutorchat.phone.domain.response.UploadVoiceFileResponse;
import com.itutorgroup.tutorchat.phone.ui.BadgeView;
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
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AudioSensorManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.FileManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MyActivityManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.ScreenUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;
import com.itutorgroup.tutorchat.phone.utils.voice.AudioRecorderButton;
import com.itutorgroup.tutorchat.phone.utils.voice.MediaManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.eventbus.ThreadMode;
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
 * 群聊页面
 *
 * @author tom_zxzhang
 */
public class GroupChatActivity extends BaseActivity implements FaceRelativeLayout.onClickSendListener, FaceRelativeLayout.OnInputEqualAtListener, XListView.IXListViewListener, OnHeadImgLongClickListener, OnRetrySendClickLintener {


    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;
    @InjectExtra(key = "message_id")
    private String messageId;
    @InjectExtra(key = "GroupId")
    private String groupId;
    @InjectExtra(key = "isCreate")
    private Boolean isCreate;
    @InjectView
    private EditText et_sendmessage;
    @InjectView
    private XListView listview;
    @InjectView
    private FaceRelativeLayout faceRelativeLayout;
    @InjectView
    private AudioRecorderButton audioRecorderLayout;
    private static final int MSG_ATMEMEBER_FROM_GROUP = 0x11;
    private static final int CREATE_GROUP = 1;
    private static final int REQUEST_SELECT_MEMBER = 0x13;
    private int page = 1, scrollItemsCount, currentAltPosition, firstPosition, lastvisiblePosition;
    private boolean isTop, isBottom = true;
    private StringBuilder inViteName;
    private GroupInfo groupInfo;
    private ChatMsgAdapter adapter;
    private ArrayList<UserInfo> AltReceiversUserInfoList;
    private GestureDetectorCompat mGestureDetectorCompat;
    private BadgeView badgeView;
    private MessagePopWindow unReadMessagePopWindow;
    private AltMessagePopWindow newMessagePopWindow;
    private AltMessagePopWindow altMessagePopWindow;
    private String unReadAltMessageId, unReadAllMessageId;
    private int SCREEN_WIDTH;
    private boolean mJumpToMessage = false;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        SCREEN_WIDTH = ScreenUtil.getScreenWidth(LPApp.getInstance());
        initListener();
        initView();
        mHeaderLayout.mTvTitle.setMaxWidth(PixelUtil.dp2px(240));
        mHeaderLayout.mTvTitle.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        loadGroupInfoUpdate();
        registerReceiver(mCloseSystemDialogReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private void loadGroupInfoUpdate() {
        if (!TextUtils.isEmpty(groupId)) {
            ObjectUpdateHelper.autoUpdate(ObjectUpdateHelper.TABLE_NAME_GROUP, groupId);
        }
    }

    private void initListener() {
        faceRelativeLayout.setOnClickSendListener(this);
        faceRelativeLayout.setTargetID(groupId, true);
        faceRelativeLayout.setOnInputEqualAtListener(this);
        faceRelativeLayout.setContentView(listview);
        listview.setPullRefreshEnable(true);
        listview.setPullLoadEnable(false);
        listview.setXListViewListener(this);
        listview.setOnScrollListener(onXScrollListener);
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
                MessageManager.getInstance().createSendMessage(content,
                        MessageType.VOICE, groupId, getIDsFromAltUserInfoList(AltReceiversUserInfoList), true, new CommonLoadingListener<MessageModel>() {
                            @Override
                            public void onResponse(MessageModel messageModel) {
                                if (StringUtils.isNotBlank(adapter)) {
                                    adapter.addMsgToBottom(messageModel);
                                    setSelectionBottom();
                                }
                                if (Preconditions.isNotBlank(AltReceiversUserInfoList)) {
                                    AltReceiversUserInfoList.clear();
                                }
                                FileManager.getInstance().reNameVoiceFile(filePath, content);
                                sendVoiceMessage(messageModel);
                            }
                        }, ChatType.GROUP, Math.ceil(seconds));
            }
        });
        et_sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isNotBlank(adapter)) {
                    listview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listview.setSelection(listview.getBottom());
                        }
                    }, 500);
                }
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

    }

    @Override
    public void onClickSend() {
        if (showSpecialInfoToast(et_sendmessage.getText().toString()))
            return;
        if (StringUtils.isEmpty(et_sendmessage.getText().toString()) || "".equals(et_sendmessage.getText().toString().trim())) {
            et_sendmessage.setText("");
            return;
        }
        if (AppUtils.isFastClick()) {
            return;
        }
        if (StringUtils.isNotEmpty(et_sendmessage.getText().toString())) {
            final String content = et_sendmessage.getText().toString();
            et_sendmessage.setText("");
            MessageManager.getInstance().createSendMessage(content,
                    MessageType.TEXT, groupId, getIDsFromAltUserInfoList(AltReceiversUserInfoList), true, new CommonLoadingListener<MessageModel>() {
                        @Override
                        public void onResponse(final MessageModel messageModel) {
                            if (StringUtils.isNotBlank(adapter)) {
                                adapter.addMsgToBottom(messageModel);
                                setSelectionBottom();
                            }
                            if (Preconditions.isNotBlank(AltReceiversUserInfoList)) {
                                AltReceiversUserInfoList.clear();
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
                    }, ChatType.GROUP);
        }
    }

    private void initData() {
        if (StringUtils.isNotEmpty(messageId) && StringUtils.isNotEmpty(groupId)) {
            mJumpToMessage = true;
            MessageManager.getInstance().queryMessagesByMessageId(messageId, groupId, new CommonLoadingListener<MessageModelByPage>() {
                @Override
                public void onResponse(MessageModelByPage messageModelByPage) {
                    if (Preconditions.isBlank(messageModelByPage.list))
                        return;
                    page = messageModelByPage.page;
                    adapter = new ChatMsgAdapter(mContext, messageModelByPage.list);
                    listview.setAdapter(adapter);
                    listview.setSelection(listview.getHeaderViewsCount() + messageModelByPage.index - 1);
                    mJumpToMessage = false;
                }
            });
        }
    }


    private synchronized void sendMessageToServer(final MessageModel messageModel, boolean... isCancel) {
        final SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.init();
        if (isCancel.length > 0 && isCancel[0]) {
            request.Type = MessageType.WITH_DRAWAL;
            request.Content = messageModel.MessageID;
        } else {
            request.Type = messageModel.Type;
            request.Content = messageModel.Content;
        }
        request.Priority = 1;
        request.GroupId = groupId;
        request.AltReceivers = messageModel.AltReceivers;
        request.LocalID = messageModel.LocalId;
        new RequestHandler<SendMessageResponse>()
                .operation(Operation.SEND_GROUP_MESSAGE)
                .request(request)
                .exec(SendMessageResponse.class, new RequestHandler.RequestListener<SendMessageResponse>() {
                    @Override
                    public void onResponse(final SendMessageResponse response, Bundle bundle) {

                        if (StringUtils.isNotEmpty(messageModel.MessageID) && messageModel.MessageID.equals(AccountManager.getInstance().getCurrentUserId() + messageModel.LocalId) && request.Type != MessageType.WITH_DRAWAL) {
                            MessageManager.getInstance().updateLocalMessageByResponse(messageModel.MessageID, response.MessageID, MessageStatus.MESSAGE_SEND_OK, response.isReceipt, response.InsertTime, new CommonLoadingListener() {
                                @Override
                                public void onResponse(Object o) {
                                    String oldId = messageModel.MessageID;
                                    messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                                    messageModel.MessageID = response.MessageID;
                                    messageModel.IsHavePermissionAccessReadStatus = response.isReceipt;
                                    if (adapter != null) {
                                        adapter.onMessageSendSuccess(oldId, messageModel);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });

                        } else if (StringUtils.isNotBlank(response) && StringUtils.isNotEmpty(response.MessageID) && request.Type == MessageType.WITH_DRAWAL) {
                            MessageManager.getInstance().getWithDrawalMessageContent(messageModel, new CommonLoadingListener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    messageModel.Type = MessageType.WITH_DRAWAL;
                                    messageModel.Content = s;
                                    messageModel.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
                                    }
                                    MessageDao.getInstance().updateCancelMessage(messageModel.MessageID, messageModel.Content, MessageStatus.MESSAGE_SEND_OK);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int errorCode, SendMessageResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        if (messageModel.Type == MessageType.WITH_DRAWAL) {
                            ToastUtil.show(getString(R.string.msg_operation_cancel_fail));
                            return;
                        }
                        messageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE; // 发送失败
                        if (StringUtils.isNotBlank(adapter)) {
                            adapter.notifyDataSetChanged();
                        }
                        MessageDao.getInstance().updateMessageSendStatus(messageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
                    }
                });
    }


    private void initView() {
        AudioSensorManager.getInstance().setFloatView(mContext, mHeaderLayout);
        badgeView = new BadgeView(mContext);
        AltReceiversUserInfoList = new ArrayList<UserInfo>();
        mGestureDetectorCompat = new GestureDetectorCompat(mContext, new MyGestureListener());
        mHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
        initData();
        GroupManager.getInstance().getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
            @Override
            public void onResponse(GroupInfo info) {
                groupInfo = info;
                updateTitle();
                initTitleView(groupInfo);
                if (StringUtils.isNotBlank(groupInfo)) {
                    if (isCreate) {
                        View headerView = LayoutInflater.from(mContext).inflate(R.layout.cell_header_view, null);
                        listview.addHeaderView(headerView);
                        List<String> list = new ArrayList<>();
                        inViteName = new StringBuilder();
                        for (GroupUserInfo userInfo : groupInfo.GroupUsers) {
                            if (!userInfo.UserID.equals(AccountManager.getInstance().getCurrentUserId())) {
                                list.add(userInfo.UserID);
                                inViteName.append(UserInfoDao.getInstance().selectWithId(userInfo.UserID).Name + ",");
                            }
                        }
                        MessageModel messageModel = new MessageModel();
                        messageModel.Type = MessageType.SYSTEM_MESSAGE;
                        messageModel.CreateTime = System.currentTimeMillis();
                        SystemMessageModel systemMessageModel = new SystemMessageModel();
                        systemMessageModel.Master = AccountManager.getInstance().getCurrentUserId();
                        systemMessageModel.OP = CREATE_GROUP;
                        systemMessageModel.values = list;
                        messageModel.PosterID = AccountManager.getInstance().getCurrentUserId();
                        messageModel.GroupId = groupId;
                        messageModel.Content = JSON.toJSONString(systemMessageModel);
                        messageModel.MessageID = AccountManager.getInstance().getCurrentUserId() + messageModel.CreateTime;
                        messageModel.currentUserId = AccountManager.getInstance().getCurrentUserId();
                        ((TextView) headerView.findViewById(R.id.createGroupInfo)).setText(MessageManager.getInstance().getResult(CREATE_GROUP, getString(R.string.msg_system_master_isself), inViteName.deleteCharAt(inViteName.length() - 1).toString()));
                        ((TextView) headerView.findViewById(R.id.tv_sendtime)).setText(TimeUtils.formatTimeString(messageModel.CreateTime));
                        MessageManager.getInstance().addMessage(messageModel);
                    }

                }
            }
        });
    }


    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ATMEMEBER_FROM_GROUP:
                startActivityForResult(new Intent(mContext, GroupSelectMemberAtActivity.class).putExtra("group_id", groupId), REQUEST_SELECT_MEMBER);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case GroupMemberSelectAdapter.RESULT_OK:
                if (!TextUtils.isEmpty(groupId)) {
                    ConversationManager.getInstance().setDraft(groupId, null);
                }
                UserInfo userInfo = (UserInfo) data.getSerializableExtra("user_info");
                et_sendmessage.getText().append(userInfo.Name + " ");
                AltReceiversUserInfoList.add(userInfo);
                break;
        }
        if (requestCode == Constant.REQ_FROM_CAMERA && resultCode == RESULT_OK) {
            ArrayList path = new ArrayList();
            path.add(Constant.CAMERA_FILE.getAbsolutePath());
            Intent intent = new Intent(mContext, PhotoPickerDetailActivity.class);
            intent.putExtra("position", 0);
            intent.putStringArrayListExtra("selected", path);
            intent.putExtra("original", true);
            intent.putExtra("max", 9);
            intent.putStringArrayListExtra("data", path);
            startActivityForResult(intent, PhotoPickerActivity.REQUEST_PREVIEW);
        }

        if (requestCode == Constant.REQ_FROM_PHOTO && resultCode == RESULT_OK || requestCode == PhotoPickerActivity.REQUEST_PREVIEW && resultCode == PhotoPickerDetailActivity.RESULT_SEND) {
            ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
            final boolean sendOriginal = data.getBooleanExtra(PhotoPickerActivity.KEY_SEND_ORIGINAL, false);
            if (list != null && list.size() > 0) {
                for (String path : list) {
                    new AsyncTask<String, Void, byte[]>() {
                        @Override
                        protected byte[] doInBackground(String... params) {
                            return FileManager.getInstance().getFileBytes(params[0], sendOriginal);
                        }

                        @Override
                        protected void onPostExecute(byte[] bytes) {
                            if (bytes.length == 0) {
                                ToastUtil.show(R.string.pics_error);
                                return;
                            }
                            MessageManager.getInstance().createSendMessage(MD5Util.getMD5String(bytes),
                                    MessageType.PIC, groupId, getIDsFromAltUserInfoList(AltReceiversUserInfoList), true, new CommonLoadingListener<MessageModel>() {
                                        @Override
                                        public void onResponse(MessageModel messageModel) {
                                            if (StringUtils.isNotBlank(adapter)) {
                                                adapter.addMsgToBottom(messageModel);
                                                setSelectionBottom();
                                            }
                                            if (Preconditions.isNotBlank(AltReceiversUserInfoList)) {
                                                AltReceiversUserInfoList.clear();
                                            }
                                            sendPicMessage(messageModel);
                                        }
                                    }, ChatType.GROUP);

                        }
                    }.execute(path);
                }
            }
        }

    }


    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        MessageManager.getInstance().getMyGroupMessage(groupId, event.list, new CommonLoadingListener<List<MessageModel>>() {
            @Override
            public void onResponse(List<MessageModel> modelList) {
                if (Preconditions.isBlank(modelList) || StringUtils.isBlank(adapter))
                    return;
                checkCancelMessageStatus(modelList);
                checkScrollMessageIsOver();
                adapter.addMsgListToBottom(modelList);
                if (isBottom) {
                    setSelectionBottom();
                }
                MessageManager.getInstance().setMessageReadRequest(modelList, MessageManager.GROUP_MESSAGE_ACTIVITY_NAME);
                if (MyActivityManager.getInstance().isTopActivityResumed()) {
                    ConversationManager.getInstance().setConversationRead(groupId);
                    ConversationManager.getInstance().updateMessageConversation(groupId, groupId, modelList);
                }

            }
        });


    }

    @Subscribe
    public void onGroupInfoEvent(GroupInfoEvent event) {
        if (event != null && event.groupInfo != null && event.groupInfo.GroupID.equals(groupId)) {
            groupInfo = GroupInfoDao.getInstance().selectWithId(groupId);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTitle();
                }
            });
        }
    }


    private void getDataByPage() {
        if (mJumpToMessage) {
            return;
        }
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                List<MessageModel> list = null;
                try {
                    list = MessageDao.getInstance().queryMessageByPage(groupId, page);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                if (Preconditions.isBlank(list))
                    list = new ArrayList<>();
                listview.stopRefresh(true);
                if (list.size() < Constant.PAGE_NUMBER) {
                    listview.setPullRefreshEnable(false);
                } else {
                    listview.setPullRefreshEnable(true);
                }
                if (page == 1) {
                    setAdapterData(list);
                    CheckUnreadMessageIsOver();
                    setSelectionBottom();
                    ConversationManager.getInstance().updateMessageConversation(groupId, groupId, list);
                } else {
                    if (adapter != null) {
                        adapter.addMsgListToTop(list);
                        listview.setSelection(listview.getHeaderViewsCount() + list.size() - 1);
                    }
                }
            }
        }.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (StringUtils.isNotEmpty(groupId)) {
            page = 1;
            getDataByPage();
            MessageManager.getInstance().clearNotificationByTargetId(groupId);
            ConversationManager.getInstance().loadDraft(groupId, new CommonLoadingListener<String>() {
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
        if (!TextUtils.isEmpty(groupId)) {
            ConversationManager.getInstance().setDraft(groupId, et_sendmessage.getText().toString());
        }
    }

    @Subscribe
    public void onClearChatHistoryEvent(ClearChatHistoryEvent event) {
        page = 1;
        getDataByPage();
    }


    @Override
    public void equalAt() {
        Message msg = mHandler.obtainMessage(MSG_ATMEMEBER_FROM_GROUP);
        mHandler.sendMessageDelayed(msg, 500);
    }

    @Override
    public void deleteAltMember(String name) {
        if (StringUtils.isEmpty(name))
            return;
        if (Preconditions.isNotBlank(AltReceiversUserInfoList)) {
            for (UserInfo userInfo : AltReceiversUserInfoList) {
                if (userInfo.Name.equals(name)) {
                    AltReceiversUserInfoList.remove(userInfo);
                    break;
                }
            }
        }
    }


    private ArrayList<String> getIDsFromAltUserInfoList(List<UserInfo> AltReceiversUserInfoList) {
        if (Preconditions.isBlank(AltReceiversUserInfoList))
            return null;
        ArrayList<String> altReceiversList = new ArrayList<String>();
        for (UserInfo userInfo : AltReceiversUserInfoList) {
            altReceiversList.add(userInfo.UserID);
        }
        return altReceiversList;
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

    @Override
    public void headImgLongClick(UserInfo userInfo) {
        if (StringUtils.isBlank(userInfo))
            return;
        et_sendmessage.getText().append("@" + userInfo.Name + " ");
        AltReceiversUserInfoList.add(userInfo);
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
        FileManager.getInstance().UploadVoiceFile(FileUtils.File2byte(filePath), voiceMessageModel.VoiceTime, new RequestHandler.RequestListener<UploadVoiceFileResponse>() {
            @Override
            public void onResponse(UploadVoiceFileResponse response, Bundle bundle) {
                super.onResponse(response, bundle);
                sendMessageToServer(voiceMessageModel);
            }

            @Override
            public void onError(int errorCode, UploadVoiceFileResponse response, Exception e, Bundle bundle) {
                super.onError(errorCode, response, e, bundle);
                voiceMessageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE; // 发送失败
                adapter.notifyDataSetChanged();
                MessageDao.getInstance().updateMessageSendStatus(voiceMessageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
            }
        });
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isTop) {
                listview.smoothScrollToPosition(0);
            } else {
                listview.startRefresh();
            }
            return true;
        }
    }

    private void initTitleView(final GroupInfo groupInfo) {
        MessageManager.getInstance().queryGroupAnnouncementUnReadCount(groupId, new CommonLoadingListener<Long>() {
            @Override
            public void onResponse(Long count) {
                if (count > 0) {
                    badgeView.setVisvible(true);
                    badgeView.setCount(count);
                } else {
                    badgeView.setVisvible(false);
                }
                mHeaderLayout.mLayoutRightContainer.addView(badgeView);
                mHeaderLayout.mLayoutRightContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(mContext, GroupAnnouncementHistoryListActivity.class).putExtra("GroupId", groupId));
                    }
                });
                mHeaderLayout
                        .autoCancel((Activity) mContext)
                        .rightImage(R.drawable.ic_group_more, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(mContext, GroupDetailActivity.class).putExtra("group_id", groupId));
                            }
                        });
            }
        });

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
                        if (newMessagePopWindow != null) {
                            newMessagePopWindow.dismiss();
                        }
                        if (listview != null) {
                            listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        }
                    } else {
                        if (listview != null) {
                            listview.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            scrollItemsCount = totalItemCount - firstVisibleItem - 2;
            firstPosition = 0;
            lastvisiblePosition = 0;
            if (firstVisibleItem >= 1) {
                firstPosition = firstVisibleItem - 1;
            } else {
                firstPosition = 0;
            }
            lastvisiblePosition = firstPosition + visibleItemCount - 1;
            if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                lastvisiblePosition = lastvisiblePosition - 1;
            }
            if (firstVisibleItem == 0) {
                isTop = true;
            } else {
                isTop = false;
            }
            try {
                if (StringUtils.isNotBlank(adapter) && Preconditions.isNotBlank(adapter.getList())) {
                    if (altMessagePopWindow != null && altMessagePopWindow.isShowing() && StringUtils.isNotEmpty(unReadAltMessageId) && unReadAltMessageId.equals(adapter.getList().get(firstVisibleItem).getMessageID())) {
                        altMessagePopWindow.dismiss();
                    }
                    if (unReadMessagePopWindow != null && unReadMessagePopWindow.isShowing() && StringUtils.isNotEmpty(unReadAllMessageId) && unReadAllMessageId.equals(adapter.getList().get(firstVisibleItem).getMessageID())) {
                        unReadMessagePopWindow.dismiss();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };


    @Subscribe
    public void onUpdateGroupAnnoucementReadStatusEvent(UpdateGroupAnnoucementReadStatusEvent event) {
        updateBadgeViewCount();
    }

    /**
     * 更新群公告数量
     */
    private void updateBadgeViewCount() {
        if (StringUtils.isEmpty(groupId))
            return;
        MessageManager.getInstance().queryGroupAnnouncementUnReadCount(groupId, new CommonLoadingListener<Long>() {
            @Override
            public void onResponse(Long count) {
                if (count > 0) {
                    badgeView.setVisvible(true);
                    badgeView.setCount(count);
                } else {
                    badgeView.setVisvible(false);
                }
            }
        });
    }

    public boolean showSpecialInfoToast(String speicalCharacter) {
        switch (speicalCharacter) {
            case "///:group_id":
                ToastUtil.show(groupId);
                return true;
        }

        return false;
    }

    private void updateTitle() {
        if (groupInfo == null) {
            return;
        }
        if (TextUtils.isEmpty(groupInfo.GroupName)) {
            mHeaderLayout.mTvTitle.setText(getString(R.string.title_group_name, getString(R.string.group_chat), groupInfo.GroupUsers.size()));
        } else {
            mHeaderLayout.mTvTitle.setText(getString(R.string.title_group_name, groupInfo.GroupName, groupInfo.GroupUsers.size()));
        }
    }


    /**
     * 查询未读数目有无超过10条
     */
    private void CheckUnreadMessageIsOver() {
        MessageManager.getInstance().getUnReadMessagesByTargetID(groupId, new CommonLoadingListener<List<MessageModel>>() {
            @Override
            public void onResponse(final List<MessageModel> modelList) {
                MessageManager.getInstance().setMessageReadRequest(modelList, MessageManager.GROUP_MESSAGE_ACTIVITY_NAME);
                queryHasAltMessagesByMessageId();
                if (Preconditions.isBlank(modelList))
                    return;
                unReadAllMessageId = modelList.get(0).MessageID;
                if (modelList.size() >= Constant.UNREAD_MESSAGE_COUNT_WILL_SHOW) {
                    if (unReadMessagePopWindow == null) {
                        unReadMessagePopWindow = new MessagePopWindow(GroupChatActivity.this);
                    }
                    mHeaderLayout.post(new Runnable() {
                        public void run() {
                            unReadMessagePopWindow.showAsDropDown(mHeaderLayout, SCREEN_WIDTH, PixelUtil.dp2px(25));
                            unReadMessagePopWindow.setText(modelList.size());
                            unReadMessagePopWindow.mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageManager.getInstance().queryMessagesByMessageId(modelList.get(0).MessageID, groupId, new CommonLoadingListener<MessageModelByPage>() {
                                        @Override
                                        public void onResponse(final MessageModelByPage messageModelByPage) {

                                            if (StringUtils.isBlank(messageModelByPage) || Preconditions.isBlank(messageModelByPage.list))
                                                return;
                                            page = messageModelByPage.page;
                                            setAdapterData(messageModelByPage.list);
                                            listview.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listview.smoothScrollToPositionFromTop(listview.getHeaderViewsCount() + messageModelByPage.index - 1, 0);
                                                    if (altMessagePopWindow != null && altMessagePopWindow.isShowing() && StringUtils.isNotEmpty(unReadAltMessageId) && unReadAltMessageId.equals(unReadAllMessageId)) {
                                                        altMessagePopWindow.dismiss();
                                                    }
                                                    if (unReadMessagePopWindow != null && unReadMessagePopWindow.isShowing()) {
                                                        unReadMessagePopWindow.dismiss();
                                                    }
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
    private void checkScrollMessageIsOver() {
        if (!isBottom && scrollItemsCount >= Constant.SCROLL_MESSAGE_COUNT_WILL_SHOW) {
            if (newMessagePopWindow == null) {
                newMessagePopWindow = new AltMessagePopWindow(GroupChatActivity.this);
                newMessagePopWindow.setContent(LPApp.getInstance().getString(R.string.msg_new_come));
            }
            listview.post(new Runnable() {
                @Override
                public void run() {
                    newMessagePopWindow.showAsDropDown(listview, SCREEN_WIDTH, -PixelUtil.dp2px(50));
                    newMessagePopWindow.mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isBottom = true;
                            newMessagePopWindow.dismiss();
                            setSelectionBottom();
                        }
                    });
                }
            });
        }
    }


    /**
     * 查询是否有人@你,当元素不处于当前屏幕时显示
     */
    public void queryHasAltMessagesByMessageId() {

        MessageManager.getInstance().queryHasAltMessagesByMessageId(groupId, new CommonLoadingListener<List<MessageModel>>() {
            @Override
            public void onResponse(final List<MessageModel> list) {

                MessageManager.getInstance().setGroupMessageReaded(groupId);
                ConversationManager.getInstance().setConversationRead(groupId);
                if (Preconditions.isBlank(list))
                    return;
                unReadAltMessageId = list.get(0).MessageID;
                if (Preconditions.isNotBlank(list) && checkIsNotInCurrentScreen(list)) {
                    if (altMessagePopWindow == null) {
                        altMessagePopWindow = new AltMessagePopWindow(GroupChatActivity.this);
                    }
                    mHeaderLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            altMessagePopWindow.showAsDropDown(listview, SCREEN_WIDTH, -PixelUtil.dp2px(50));
                            altMessagePopWindow.mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageManager.getInstance().queryMessagesByMessageId(list.get(currentAltPosition).MessageID, groupId, new CommonLoadingListener<MessageModelByPage>() {
                                        @Override
                                        public void onResponse(final MessageModelByPage messageModelByPage) {

                                            if (Preconditions.isBlank(messageModelByPage.list))
                                                return;
                                            page = messageModelByPage.page;
                                            setAdapterData(messageModelByPage.list);
//                                          adapter.setData(messageModelByPage.list);
                                            listview.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    listview.smoothScrollToPositionFromTop(listview.getHeaderViewsCount() + messageModelByPage.index - 1, 0);
                                                    currentAltPosition++;
                                                    if (currentAltPosition == list.size()) {
                                                        if (unReadMessagePopWindow != null && unReadMessagePopWindow.isShowing() && StringUtils.isNotEmpty(unReadAllMessageId) && unReadAllMessageId.equals(unReadAltMessageId)) {
                                                            unReadMessagePopWindow.dismiss();
                                                        }
                                                        if (altMessagePopWindow != null && altMessagePopWindow.isShowing()) {
                                                            altMessagePopWindow.dismiss();
                                                        }
                                                    }
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



    private void setAdapterData(List<MessageModel> list) {
        if (adapter == null) {
            adapter = new ChatMsgAdapter(mContext, list, false);
            listview.setAdapter(adapter);
        } else {
            adapter.setData(list);
        }
    }


    private void setSelectionBottom() {

        listview.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter == null)
                    return;
                listview.setSelection(listview.getHeaderViewsCount() + adapter.getCount() - 1);
            }
        }, 200);
    }


    private boolean checkIsNotInCurrentScreen(List<MessageModel> messageModels) {
        if (StringUtils.isBlank(adapter) || adapter.getCount() == 0 || Preconditions.isBlank(messageModels)) {
            return false;
        }
        try {
            for (MessageModel messageModel1 : messageModels) {
                for (MessageModel messageModel : adapter.getList().subList(firstPosition, lastvisiblePosition + 1)) {
                    if (!messageModel.MessageID.equals(messageModel1.MessageID)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void checkCancelMessageStatus(List<MessageModel> modelList) {
        if (Preconditions.isBlank(modelList) || StringUtils.isBlank(adapter) || Preconditions.isBlank(adapter.getList()))
            return;
        Iterator modelIterator = modelList.iterator();
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
                        }
                        messageModel1.Type = MessageType.WITH_DRAWAL;
                        needRemove = true;
                        MessageManager.getInstance().getWithDrawalMessageContent(messageModel1, new CommonLoadingListener<String>() {
                            @Override
                            public void onResponse(String s) {
                                messageModel1.Content = s;
                                adapter.notifyDataSetChanged();
//                                MessageDao.getInstance().modifyMessageContent(messageModel.MessageID, s);
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

    @Subscribe(ThreadMode.BackgroundThread)
    public void onMessageReadEvent(MessageReadEvent event) {
        if (event.mList != null && event.mList.size() > 0) {
            for (String id : event.mList) {
                if (adapter != null && !TextUtils.isEmpty(id)) {
                    List<MessageModel> list = adapter.getList();
                    if (list != null && list.size() > 0) {
                        for (MessageModel model : list) {
                            if (model.Type == MessageType.GROUPANNOUNCEMENT && id.equals(model.MessageID)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initTitleView(groupInfo);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                return;
                            }
                        }
                    }
                }
            }
        }
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
