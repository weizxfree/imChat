package com.itutorgroup.tutorchat.phone.activity.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.adapter.ContactsSortAdapter;
import com.itutorgroup.tutorchat.phone.adapter.search.FullyLinearLayoutManager;
import com.itutorgroup.tutorchat.phone.adapter.search.SelectedRecipientsRecyclerAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupUserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.migration.IMigrationListener;
import com.itutorgroup.tutorchat.phone.domain.migration.MigrateHelper;
import com.itutorgroup.tutorchat.phone.domain.request.CreateGroupRequest;
import com.itutorgroup.tutorchat.phone.domain.request.EditGroupRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CreateGroupResponse;
import com.itutorgroup.tutorchat.phone.domain.response.EditGroupResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetGroupInfoResponse;
import com.itutorgroup.tutorchat.phone.fragment.search.group.SearchDataMemberFragment;
import com.itutorgroup.tutorchat.phone.fragment.search.group.SearchMemberFragment;
import com.itutorgroup.tutorchat.phone.fragment.search.group.SearchRemoteMemberFragment;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.dialog.ConfirmDialog;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;
import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/12.
 */
public class GroupEditActivity extends BaseActivity implements ContactsSortAdapter.CheckBoxListener {

    @InjectExtra(key = "group_id")
    private String mGroupId;

    private List<String> mGroupExistsMemberIdList;

    @InjectView(id = R.id.recycler_choose_contacts)
    RecyclerView mRecyclerSelected;

    private SelectedRecipientsRecyclerAdapter mSelectedAdapter;

    @InjectView(id = R.id.common_actionbar)
    private HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.edt_search)
    EditText mEdtSearch;

    private Drawable mIconSearch;

    @InjectView(id = R.id.frame_content)
    FrameLayout mFrameContent;

    private TextView mTvMenuConfirm;

    private List<UserInfo> mSelectedUserInfoList;

    public static final int REQUEST_CODE_SEARCH_CONTACTS = 0x11;

    public static final String EXTRA_EDIT_TYPE = "edit_type";
    public static final int TYPE_ADD_PEOPLE = 0x21;
    public static final int TYPE_REMOVE_PEOPLE = 0x22;
    public static final int TYPE_CREATE_GROUP = 0x23;

    public static final int TYPE_CHANGE_ADMIN = 0x24;
    public static final int TYPE_SET_MANAGER = 0x25;

    private int mType;

    public static final int RESULT_CODE_OK = 0x110;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    private SearchRemoteMemberFragment mSearchRemoteFragment;
    private SearchDataMemberFragment mSearchDataFragment;

    private SearchMemberFragment mCurrentFragment;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        mType = getIntent().getIntExtra(EXTRA_EDIT_TYPE, TYPE_ADD_PEOPLE);
        initView();
        mSelectedUserInfoList = new ArrayList<>();

        mSearchDataFragment = new SearchDataMemberFragment(this);
        mSearchDataFragment.setOnSelectedContactsChangeListener(this);
        mFrameContent.addView(mSearchDataFragment);

        loadData();

        showFragment(mSearchDataFragment);
        mRecyclerSelected.setLayoutManager(new FullyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        initListener();
    }

    private CommonLoadingListener<List<UserInfo>> mLoadGroupManagerListener = new CommonLoadingListener<List<UserInfo>>() {
        @Override
        public void onResponse(List<UserInfo> list) {
            if (list == null || list.size() == 0) {
                return;
            }
            Observable.just(list)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<List<UserInfo>, List<String>[]>() {
                        @Override
                        public List<String>[] call(List<UserInfo> list) {
                            IMigrationListener<GroupUserInfo, String> listener = new IMigrationListener<GroupUserInfo, String>() {
                                @Override
                                public String migrate(GroupUserInfo src) {
                                    return src.UserID;
                                }
                            };
                            List<GroupUserInfo> srcList = GroupUserInfoDao.getInstance().queryGroupManagerInfoList(mGroupId);
                            List<String> managerList = MigrateHelper.migration(srcList, listener);
                            srcList = GroupUserInfoDao.getInstance().queryGroupAdminList(mGroupId);
                            List<String> adminList = MigrateHelper.migration(srcList, listener);
                            return new List[]{adminList, managerList};
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<String>[]>() {
                        @Override
                        public void call(List<String>[] listArr) {
                            List<String> admin = listArr[0];
                            mSearchDataFragment.setDisableList(admin);
                            if (mType == TYPE_SET_MANAGER) {
                                List<String> list = listArr[1];
                                mSearchDataFragment.setDefaultCheckedList(list);
                                if (list == null || list.size() == 0) {
                                    return;
                                }
                                UserInfoManager.getInstance().forceGetUserList(list, new CommonLoadingListener<List<UserInfo>>() {
                                    @Override
                                    public void onResponse(List<UserInfo> list) {
                                        mSelectedUserInfoList.addAll(list);
                                        onSelectedListChanged();
                                        onSelectedListChanged();
                                    }
                                });
                            }
                            mSearchDataFragment.refresh();
                        }
                    }, CommonUtil.ACTION_EXCEPTION);

        }
    };

    private void loadData() {
        switch (mType) {
            case TYPE_CREATE_GROUP:
                mSearchRemoteFragment = new SearchRemoteMemberFragment(this);
                mSearchRemoteFragment.setOnSelectedContactsChangeListener(this);
                mFrameContent.addView(mSearchRemoteFragment);
                loadCreateGroupData();
                break;
            case TYPE_ADD_PEOPLE:
                mSearchRemoteFragment = new SearchRemoteMemberFragment(this);
                mSearchRemoteFragment.setOnSelectedContactsChangeListener(this);
                mFrameContent.addView(mSearchRemoteFragment);
                loadAddGroupMemberData();
                break;
            case TYPE_SET_MANAGER:
                loadGroupMemberListData(mLoadGroupManagerListener);
                break;
            case TYPE_CHANGE_ADMIN:
                mTvMenuConfirm.setVisibility(View.GONE);
                loadGroupMemberListData(mLoadGroupManagerListener);
                break;
            case TYPE_REMOVE_PEOPLE:
                loadGroupMemberListData(null);
                break;
        }
    }

    private void showFragment(SearchMemberFragment currentFragment) {
        currentFragment.setVisibility(View.VISIBLE);
        if (mType == TYPE_ADD_PEOPLE || mType == TYPE_CREATE_GROUP) {
            if (currentFragment == mSearchRemoteFragment) {
                mSearchDataFragment.setVisibility(View.GONE);
            } else if (currentFragment == mSearchDataFragment) {
                mSearchRemoteFragment.setVisibility(View.GONE);
            }
        }
        mCurrentFragment = currentFragment;
    }

    private void initListener() {
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
                if (mType == TYPE_ADD_PEOPLE || mType == TYPE_CREATE_GROUP) {
                    showFragment(isEmpty ? mSearchDataFragment : mSearchRemoteFragment);
                }

                refreshSearchIcon();

                mHandler.removeMessages(MSG_SEARCH_CONTENT);

                if (isEmpty && (mType == TYPE_ADD_PEOPLE || mType == TYPE_CREATE_GROUP)) {
                    mSearchRemoteFragment.showEmptyView();
                } else {
                    Message msg = mHandler.obtainMessage(MSG_SEARCH_CONTENT, text);
                    mHandler.sendMessageDelayed(msg, 1000);
                }
            }
        });

        mEdtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_SEARCH:
                        InputMethodUtil.hideInputMethod(GroupEditActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    private void refreshSearchIcon() {
        String text = mEdtSearch.getText().toString();
        boolean isEmpty = TextUtils.isEmpty(text);

        Drawable[] drawables = mEdtSearch.getCompoundDrawables();
        mEdtSearch.setCompoundDrawables(isEmpty && Preconditions.isBlank(mSelectedUserInfoList) ? mIconSearch : null, drawables[1], drawables[2], drawables[3]);
    }

    private void initView() {
        int title = R.string.select_contacts_people;
        int cancel = R.string.cancel;
        switch (mType) {
            case TYPE_ADD_PEOPLE:
                title = R.string.add_group_people;
                break;
            case TYPE_REMOVE_PEOPLE:
                title = R.string.remove_group_people;
                break;
            case TYPE_CREATE_GROUP:
                title = R.string.create_group;
                break;
            case TYPE_CHANGE_ADMIN:
                title = R.string.change_group_admin;
                break;
            case TYPE_SET_MANAGER:
                title = R.string.set_group_manager;
                break;
        }
        mHeaderLayout.title(getString(title))
                .leftText(getString(cancel), new InputMethodUtil.CancelListener(this));
        mTvMenuConfirm = mHeaderLayout.addRightText(getString(R.string.title_activity_sure), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(R.string.title_activity_sure).equals(mTvMenuConfirm.getText().toString())) {
                    return;
                }

                switch (mType) {
                    case TYPE_CREATE_GROUP:
                        createGroup();
                        break;
                    case TYPE_ADD_PEOPLE:
                    case TYPE_REMOVE_PEOPLE:
                        editGroup();
                        break;
                    case TYPE_SET_MANAGER:
                        setGroupManagerList();
                        break;
                }
            }
        });
        mTvMenuConfirm.setEnabled(false);

        mIconSearch = getResources().getDrawable(R.drawable.search_blue);
        mIconSearch.setBounds(0, 0, mIconSearch.getIntrinsicWidth(), mIconSearch.getIntrinsicHeight());

        findViewById(R.id.rl_choose_group_chat).setVisibility(mType == TYPE_CREATE_GROUP ? View.VISIBLE : View.GONE);
    }

    private void loadCreateGroupData() {
        ContactsManager.getInstance().getMyContacts(new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> list) {
                mSearchDataFragment.setData(list);
            }
        });
    }


    /**
     * 加载群当前成员列表数据
     */
    private void loadGroupMemberListData(final CommonLoadingListener<List<UserInfo>> listener) {
        GroupManager.getInstance().getUserInfoListById(mGroupId, -1, new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> list) {
                Observable.just(list).subscribeOn(Schedulers.io())
                        .map(new Func1<List<UserInfo>, List<UserInfo>>() {
                            @Override
                            public List<UserInfo> call(List<UserInfo> list) {
                                list.remove(AccountManager.getInstance().getCurrentUser());
                                return list;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<UserInfo>>() {
                            @Override
                            public void call(List<UserInfo> list) {
                                mSearchDataFragment.setData(list);
                                if (listener != null) {
                                    listener.onResponse(list);
                                }
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        });
    }

    private void loadAddGroupMemberData() {
        GroupManager.getInstance().getGroupUserInfoList(mGroupId, -1, new CommonLoadingListener<List<GroupUserInfo>>() {
            @Override
            public void onResponse(List<GroupUserInfo> list) {
                mGroupExistsMemberIdList = new ArrayList<>();
                for (GroupUserInfo gui : list) {
                    mGroupExistsMemberIdList.add(gui.UserID);
                }
                mSearchDataFragment.setDisableList(mGroupExistsMemberIdList);
                mSearchRemoteFragment.setDisableList(mGroupExistsMemberIdList);
                ContactsManager.getInstance().getMyContacts(new CommonLoadingListener<List<UserInfo>>() {
                    @Override
                    public void onResponse(List<UserInfo> list) {
                        mSearchDataFragment.setData(list);
                    }
                });
            }
        });
    }

    /**
     * 创建群组
     */
    private void createGroup() {
        if (mSelectedUserInfoList != null && mSelectedUserInfoList.size() == 1) {
            startActivity(new Intent(GroupEditActivity.this, SingleChatActivity.class).putExtra("user_id", mSelectedUserInfoList.get(0).UserID));
            finish();
            return;
        }

        CreateGroupRequest request = new CreateGroupRequest();
        request.init();
        List<String> userIdList = new ArrayList<>();
        for (UserInfo info : mSelectedUserInfoList) {
            userIdList.add(info.UserID);
        }
        request.UserIDList = userIdList;

        new RequestHandler<CreateGroupResponse>()
                .dialog(GroupEditActivity.this)
                .operation(Operation.CREATE_GROUP)
                .request(request)
                .exec(CreateGroupResponse.class, new RequestHandler.RequestListener<CreateGroupResponse>() {
                    @Override
                    public void onResponse(CreateGroupResponse response, Bundle bundle) {
                        if (response.Group != null) {
                            GroupInfoDao.getInstance().add(response.Group);
                            ConversationDao.getInstance().createConversation(response.Group.GroupID, response.Group.GroupID, System.currentTimeMillis());
                            ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
                            event.setRefreshId(response.Group.GroupID);
                            EventBusManager.getInstance().post(event);
                            startActivity(new Intent(mContext, GroupChatActivity.class)
                                    .putExtra("GroupId", response.Group.GroupID).putExtra("isCreate", true)
                            );
                            finish();
                        }
                    }
                });
    }

    private void editGroup() {
        final EditGroupRequest request = new EditGroupRequest();
        request.init();
        request.GroupID = mGroupId;
        List<String> idList = new ArrayList<>();
        for (UserInfo info : mSelectedUserInfoList) {
            idList.add(info.UserID);
        }
        if (mType == TYPE_ADD_PEOPLE) {
            request.AddUserIDList = idList;
        } else if (mType == TYPE_REMOVE_PEOPLE) {
            request.RemoveUserIDList = idList;
        }

        new RequestHandler()
                .dialog(GroupEditActivity.this)
                .request(request)
                .operation(Operation.EDIT_GROUP)
                .exec(EditGroupResponse.class, new RequestHandler.RequestListener<EditGroupResponse>() {
                    @Override
                    public void onResponse(EditGroupResponse response, Bundle bundle) {
                        ContactsManager.getInstance().getGroupInfo(mGroupId, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                            @Override
                            public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                                EventBusManager.getInstance().post(new GroupInfoEvent(response.Group));
                                setResult(RESULT_CODE_OK);
                                finish();
                            }
                        });
                    }
                });
    }

    private void fillImageLayout(List<UserInfo> userInfoList) {
        if (mSelectedAdapter == null) {
            mSelectedAdapter = new SelectedRecipientsRecyclerAdapter(GroupEditActivity.this, userInfoList, this);
            mRecyclerSelected.setAdapter(mSelectedAdapter);
        } else {
            mSelectedAdapter.setData(userInfoList);
            mSelectedAdapter.notifyDataSetChanged();
            mRecyclerSelected.smoothScrollToPosition(userInfoList.size());
        }
    }

    public void onSelectedListChanged() {
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        fillImageLayout(mSelectedUserInfoList);
                        mCurrentFragment.refreshData();
                        if (Preconditions.isNotBlank(mSelectedUserInfoList) && mSelectedUserInfoList.size() > 0) {
                            mTvMenuConfirm.setText(getString(R.string.title_activity_sure_number, mSelectedUserInfoList.size()));
                            mTvMenuConfirm.setEnabled(true);
                        } else {
                            if (mType == TYPE_SET_MANAGER) {
                                mTvMenuConfirm.setText(getString(R.string.title_activity_sure_number, mSelectedUserInfoList.size()));
                                mTvMenuConfirm.setEnabled(true);
                            } else {
                                mTvMenuConfirm.setText(R.string.title_activity_sure);
                                mTvMenuConfirm.setEnabled(false);
                            }
                        }
                        refreshSearchIcon();
                        if (mSelectedUserInfoList != null && mSelectedUserInfoList.size() == 1) {
                            // fill again to fix bug.
                            fillImageLayout(mSelectedUserInfoList);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void performChangeGroupAdmin(final String id) {
        new ConfirmDialog(GroupEditActivity.this)
                .message(getString(R.string.tip_change_group_admin))
                .confirmText(getString(R.string.confirm))
                .confirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupManager.getInstance().attornGroupMaster(mContext, mGroupId, id, new CommonLoadingListener<Void>() {
                            @Override
                            public void onResponse(Void aVoid) {
                                finish();
                            }
                        });
                    }
                })
                .cancel(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSearchDataFragment.clearAllChecked();
                    }
                })
                .onCancel(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mSearchDataFragment.clearAllChecked();
                    }
                })
                .show();
    }

    private void performSetGroupManager(final String id) {
        GroupManager.getInstance().setGroupAdmin(null, mGroupId, id, true, null);
    }

    private void performRemoveGroupManager(final String id) {
        GroupManager.getInstance().setGroupAdmin(null, mGroupId, id, false, null);
    }

    private void setGroupManagerList() {
        if (mSelectedUserInfoList == null) {
            mSelectedUserInfoList = new ArrayList<>();
        }
        List<String> userIdList = new ArrayList<>();
        for (UserInfo info : mSelectedUserInfoList) {
            userIdList.add(info.UserID);
        }
        GroupManager.getInstance().setGroupAdminList(GroupEditActivity.this, mGroupId, userIdList, new CommonLoadingListener<Boolean>() {
            @Override
            public void onResponse(Boolean change) {
                if (change) {
                    setResult(RESULT_CODE_OK);
                    finish();
                } else {
                    ToastUtil.show(R.string.group_manager_no_change);
                }
            }
        });
    }

    @Override
    public void onSelectIdAdd(String id) {
        switch (mType) {
            case TYPE_CHANGE_ADMIN:
                performChangeGroupAdmin(id);
                break;
            case TYPE_SET_MANAGER:
//                performSetGroupManager(id);
            default:
                UserInfoManager.getInstance().getUserInfo(id, new CommonLoadingListener<UserInfo>() {
                    @Override
                    public void onResponse(UserInfo userInfo) {
                        mSelectedUserInfoList.add(userInfo);
                        onSelectedListChanged();
                    }
                });
                break;
        }
    }

    @Override
    public void onSelectIdRemove(String id) {
        switch (mType) {
            case TYPE_SET_MANAGER:
//                performRemoveGroupManager(id);
            default:
                UserInfoManager.getInstance().getUserInfo(id, new CommonLoadingListener<UserInfo>() {
                    @Override
                    public void onResponse(UserInfo userInfo) {
                        mSelectedUserInfoList.remove(userInfo);
                        onSelectedListChanged();
                    }
                });
                break;
        }
        mCurrentFragment.removeSelectedItem(id);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEARCH_CONTENT:
                performSearch(msg);
                break;
        }
    }

    private void performSearch(Message msg) {
        if (msg != null && !StringUtils.isBlank(msg.obj)) {
            String text = (String) msg.obj;
            if (text.equals(mEdtSearch.getText().toString())) {
                List<String> idList = new ArrayList<>();
                for (UserInfo info : mSelectedUserInfoList) {
                    idList.add(info.UserID);
                }
                mCurrentFragment.setDefaultCheckedList(idList);
                mCurrentFragment.search(text);
            }
        } else {
            mCurrentFragment.showEmptyView();
        }
    }

    @OnClick(id = R.id.rl_choose_group_chat)
    public void onChooseGroupChatClick() {
        startActivity(new Intent(GroupEditActivity.this, GroupListActivity.class).putExtra("mode_select", true));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(mEdtSearch.getText().toString())) {
            super.onBackPressed();
        } else {
            mEdtSearch.setText("");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodUtil.hideInputMethod(GroupEditActivity.this);
        return super.dispatchTouchEvent(ev);
    }

    private static long mFirstDelKeyTime;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0
                && mSelectedAdapter != null
                && TextUtils.isEmpty(mEdtSearch.getText().toString())) {
            if (mFirstDelKeyTime + 2000 > System.currentTimeMillis()) {
                if (mSelectedAdapter.getItemCount() > 0) {
                    mRecyclerSelected.smoothScrollToPosition(mSelectedAdapter.getItemCount() - 1);
                }
                if (mSelectedUserInfoList != null && mSelectedUserInfoList.size() > 0) {
                    int size = mSelectedUserInfoList.size();
                    UserInfo userInfo = mSelectedUserInfoList.get(size - 1);
                    onSelectIdRemove(userInfo.UserID);
                }
                mFirstDelKeyTime = 0;
            } else {
                mFirstDelKeyTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
