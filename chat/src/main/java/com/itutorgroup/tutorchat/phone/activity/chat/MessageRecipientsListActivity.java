package com.itutorgroup.tutorchat.phone.activity.chat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.chat.MessageRecipientAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.request.ViewGroupReadRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CheckIsReadResponse;
import com.itutorgroup.tutorchat.phone.domain.response.ViewGroupReadResponse;
import com.itutorgroup.tutorchat.phone.ui.SegmentControlView;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.ClearEditText;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class MessageRecipientsListActivity extends BaseActivity implements SegmentControlView.OnSegmentChangedListener {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.lv_recipients)
    ListView mListView;

    @InjectView(id = R.id.edt_search)
    ClearEditText mEdtSearch;

    @InjectExtra(key = "message")
    MessageModel mMessage;

    @InjectView(id = R.id.segment_read_status)
    SegmentControlView mSegmentReadStatus;

    private GroupInfo mGroupInfo;

    private MessageRecipientAdapter mAdapter;

    private List<String> mUnReadUserIdList;
    private List<String> mReadUserIdList;
    private List<UserInfo> mUserList;

    private boolean mIsUnRead = true;
    private String mFilterKey;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipients_list);
        initView();
        loadData();
    }

    private void loadData() {
        String groupId = mMessage.GroupId;
        if (!TextUtils.isEmpty(groupId)) {
            GroupManager.getInstance().getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
                @Override
                public void onResponse(GroupInfo groupInfo) {
                    mGroupInfo = groupInfo;
                    getStatusUserList();
                }
            });
        } else {
            getMessageReadStatus();
        }
    }

    private void getMessageReadStatus() {
        ArrayList<MessageModel> list = new ArrayList<>();
        list.add(mMessage);
        if (mMessage.IsRead == 0) {
            MessageManager.getInstance().checkMessagesReadState(list, true, new CommonLoadingListener<List<CheckIsReadResponse.ReadModel>>() {
                @Override
                public void onResponse(List<CheckIsReadResponse.ReadModel> list) {
                    if (list != null && list.size() == 1) {
                        CheckIsReadResponse.ReadModel model = list.get(0);
                        boolean isRead = model.ReadCount == 1;
                        mUnReadUserIdList = new ArrayList<>();
                        mReadUserIdList = new ArrayList<>();
                        mUserList = new ArrayList<>();
                        if (isRead) {
                            mReadUserIdList.add(mMessage.targetId);
                        } else {
                            mUnReadUserIdList.add(mMessage.targetId);
                        }
                        UserInfoManager.getInstance().getUserInfo(mMessage.targetId, new CommonLoadingListener<UserInfo>() {
                            @Override
                            public void onResponse(UserInfo userInfo) {
                                mUserList.add(userInfo);
                                setTabText(mUnReadUserIdList.size(), mReadUserIdList.size());
                                updateUI();
                            }
                        });
                    }
                }
            });
        } else {
            mUnReadUserIdList = new ArrayList<>();
            mReadUserIdList = new ArrayList<>();
            mUserList = new ArrayList<>();
            mReadUserIdList.add(mMessage.targetId);
            UserInfoManager.getInstance().getUserInfo(mMessage.targetId, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo userInfo) {
                    mUserList.add(userInfo);
                    setTabText(mUnReadUserIdList.size(), mReadUserIdList.size());
                    updateUI();
                }
            });
        }
    }

    private void getStatusUserList() {
        final ViewGroupReadRequest request = new ViewGroupReadRequest();
        request.init();
        request.GroupID = mGroupInfo.GroupID;
        request.MessageID = mMessage.MessageID;

        new RequestHandler<ViewGroupReadResponse>()
                .request(request)
                .operation(Operation.VIEW_GROUP_READ)
                .exec(ViewGroupReadResponse.class, new RequestHandler.RequestListener<ViewGroupReadResponse>() {
                    @Override
                    public void onResponse(ViewGroupReadResponse response, Bundle bundle) {
                        ViewGroupReadResponse.GroupReadStatusModel model = response.GroupReadStatusInfo;
                        if (model != null) {
                            setTabText(model.UnReadCount, model.ReadCount);
                            parseReadUserData(model.ReadStatusList);
                        }
                    }
                });
    }

    private void setTabText(int unread, int read) {
        String[] texts = new String[]{
                getString(R.string.message_recipients_state_unread, unread),
                getString(R.string.message_recipients_state_read, read),
        };
        mSegmentReadStatus.setTexts(texts);
    }

    private void parseReadUserData(ArrayList<ViewGroupReadResponse.GroupReadModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }

        new AsyncTask<ArrayList<ViewGroupReadResponse.GroupReadModel>, Void, Void>() {

            @Override
            protected Void doInBackground(ArrayList<ViewGroupReadResponse.GroupReadModel>... params) {
                ArrayList<String>[] array = getStatusListArray(params[0]);
                mUnReadUserIdList = array[0];
                mReadUserIdList = array[1];
                UserInfoManager.getInstance().forceGetUserList(array[2], new CommonLoadingListener<List<UserInfo>>() {
                    @Override
                    public void onResponse(List<UserInfo> list) {
                        mUserList = list;
                        updateUI();
                    }
                });
                return null;
            }
        }.execute(list);
    }

    private ArrayList<String>[] getStatusListArray(ArrayList<ViewGroupReadResponse.GroupReadModel> list) {
        ArrayList<String> readList = new ArrayList<>();
        ArrayList<String> unReadList = new ArrayList<>();
        ArrayList<String> allList = new ArrayList<>();
        for (ViewGroupReadResponse.GroupReadModel model : list) {
            switch (model.IsRead) {
                case 0:
                    unReadList.add(model.UserID);
                    break;
                case 1:
                    readList.add(model.UserID);
                    break;
            }
            allList.add(model.UserID);
        }
        return new ArrayList[]{unReadList, readList, allList};
    }

    private void updateUI() {

        Observable.just(mUserList)
                .subscribeOn(Schedulers.io())
                .filter(new Func1<List<UserInfo>, Boolean>() {
                    @Override
                    public Boolean call(List<UserInfo> userInfos) {
                        return userInfos != null && userInfos.size() != 0;
                    }
                })
                .map(new Func1<List<UserInfo>, List<UserInfo>>() {
                    @Override
                    public List<UserInfo> call(List<UserInfo> userInfos) {
                        List<String> userIdList = mIsUnRead ? mUnReadUserIdList : mReadUserIdList;
                        List<UserInfo> list = new ArrayList<>();
                        for (UserInfo user : userInfos) {
                            if (userIdList.contains(user.UserID)) {
                                if (TextUtils.isEmpty(mFilterKey)) {
                                    list.add(user);
                                } else {
                                    if (user.Name.toLowerCase().contains(mFilterKey.toLowerCase())
                                            || user.ChineseName.toLowerCase().contains(mFilterKey.toLowerCase())) {
                                        list.add(user);
                                    }
                                }
                            }
                        }
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<UserInfo>>() {
                    @Override
                    public void call(List<UserInfo> list) {
                        if (mAdapter == null) {
                            mAdapter = new MessageRecipientAdapter(MessageRecipientsListActivity.this, list, mFilterKey);
                            mListView.setAdapter(mAdapter);
                        } else {
                            mAdapter.setFilterKey(mFilterKey);
                            mAdapter.replaceAll(list);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_message_recipients_list)).autoCancel(this);

        mSegmentReadStatus.setOnSegmentChangedListener(this);

        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                boolean isEmpty = TextUtils.isEmpty(text);
                mHandler.removeMessages(MSG_SEARCH_CONTENT);
                Message msg = mHandler.obtainMessage(MSG_SEARCH_CONTENT, text);
                mHandler.sendMessageDelayed(msg, isEmpty ? 0 : 1000);
            }
        });
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEARCH_CONTENT:
                mFilterKey = (String) msg.obj;
                updateUI();
                break;
        }
    }

    @Override
    public void onSegmentChanged(int newSelectedIndex) {
        mIsUnRead = newSelectedIndex == 0;
        updateUI();
    }
}
