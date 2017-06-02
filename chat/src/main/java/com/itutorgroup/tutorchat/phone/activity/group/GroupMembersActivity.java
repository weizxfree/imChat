/**
 *
 */
package com.itutorgroup.tutorchat.phone.activity.group;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ChatDetailActivity;
import com.itutorgroup.tutorchat.phone.adapter.chat.MessageRecipientAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.ClearEditText;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 群组成员页面
 *
 * @author tom_zxzhang
 */
public class GroupMembersActivity extends BaseActivity {

    @InjectExtra(key = "group_id")
    private String mGroupId;

    private List<UserInfo> mUserList;

    @InjectView(id = R.id.lv_recipients)
    private ListView mListView;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.edt_search)
    ClearEditText mEdtSearch;

    private MessageRecipientAdapter mAdapter;

    private String mFilterKey;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        initView();
    }

    private void initView() {
        int size = mUserList == null ? 0 : mUserList.size();
        mHeaderLayout.title(getString(R.string.group_member, size))
                .autoCancel(GroupMembersActivity.this);

        GroupManager.getInstance().getUserInfoListById(mGroupId, -1, new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(final List<UserInfo> list) {
                Observable.just(list).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<UserInfo>>() {
                            @Override
                            public void call(List<UserInfo> list) {
                                setData(list);
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        });

        initSearchEditText();

    }

    private void setData(List<UserInfo> list) {
        mUserList = list;
        int size = mUserList == null ? 0 : mUserList.size();
        mHeaderLayout.mLayoutLeftContainer.removeAllViews();
        mHeaderLayout.mLayoutRightContainer.removeAllViews();
        mHeaderLayout.title(getString(R.string.group_member, size))
                .autoCancel(GroupMembersActivity.this);

        updateUI();
    }

    private void initSearchEditText() {
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
                        List<UserInfo> list = new ArrayList<>();
                        for (UserInfo user : userInfos) {
                            if (TextUtils.isEmpty(mFilterKey)) {
                                list.add(user);
                            } else {
                                if (user.Name.toLowerCase().contains(mFilterKey.toLowerCase())
                                        || user.ChineseName.toLowerCase().contains(mFilterKey.toLowerCase())) {
                                    list.add(user);
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
                            mAdapter = new MessageRecipientAdapter(GroupMembersActivity.this, list, mFilterKey, mGroupId, true, new CommonLoadingListener<UserInfo>() {
                                @Override
                                public void onResponse(UserInfo userInfo) {
                                    startActivity(new Intent(GroupMembersActivity.this, ChatDetailActivity.class).putExtra("user_info", userInfo));
                                }
                            });
                            mListView.setAdapter(mAdapter);
                        } else {
                            mAdapter.setFilterKey(mFilterKey);
                            mAdapter.replaceAll(list);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    @Subscribe
    public void onGroupInfoEvent(GroupInfoEvent event) {
        if (mGroupId.equals(event.groupInfo.GroupID)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initView();
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodUtil.hideInputMethod(GroupMembersActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
