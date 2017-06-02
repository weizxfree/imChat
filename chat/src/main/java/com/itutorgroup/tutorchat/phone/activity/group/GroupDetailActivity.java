/**
 *
 */
package com.itutorgroup.tutorchat.phone.activity.group;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.settings.AppDetailActivity;
import com.itutorgroup.tutorchat.phone.adapter.group.GroupUserInfoAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInContactDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UserSettingsEvent;
import com.itutorgroup.tutorchat.phone.domain.response.AddContactResponse;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.domain.response.RemoveContactResponse;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.ui.common.CommonSwitchButton;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.ui.common.item.NavItemView;
import com.itutorgroup.tutorchat.phone.ui.common.item.SwitchItemView;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.TopChatManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.message.ConversationUtil;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import cn.salesuite.saf.eventbus.Subscribe;
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
 * 群组详情页面
 *
 * @author tom_zxzhang
 */
public class GroupDetailActivity extends BaseActivity {


    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;
    @InjectView
    SwitchItemView saveAddress;
    @InjectView
    GridView mGridView;
    @InjectExtra(key = "group_id")
    private String groupId;
    @InjectView(id = R.id.groupName)
    NavItemView mNavGroupName;
    @InjectView(id = R.id.nav_group_announcement)
    NavItemView mNavGroupAnnouncement;
    @InjectView(id = R.id.switch_top_chat)
    SwitchItemView mSwitchTopChat;
    @InjectView
    TextView groupMembers;

    @InjectView(id = R.id.avatar_view)
    AvatarView mAvatarView;

    @InjectView(id = R.id.imv_add_group_people)
    CircleImageView mImvAddPeople;

    @InjectView(id = R.id.imv_remove_group_people)
    CircleImageView mImvRemovePeople;

    @InjectView(id = R.id.ll_admin_preference)
    LinearLayout mLLAdminPreference;

    @InjectView(id = R.id.ll_manager_preference)
    LinearLayout mLLManagerPreference;

    @InjectView
    SwitchItemView messageNotDisturb;

    public final static int REQUEST_CODE = 0x01;

    public final static int HANDER_MESSAGE_CODE = 0x02;

    private GroupInfo mGroupInfo;
    private GestureDetectorCompat mGestureDetectorCompat;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        mGestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
        loadGroupTicksInfo();
        initData();
        initView();
        initListener();
    }

    private void loadGroupTicksInfo() {
        ContactsManager.getInstance().getGroupInfo(groupId, 0, null);
    }

    @OnClick(id = R.id.groupName)
    private void updateGroupName() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        GroupManager.getInstance().getGroupRightByUserId(groupId, currentUserId, new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer integer) {
                if (integer <= 1) {
                    startActivityForResult(new Intent(mContext, GroupNameUpdateActivity.class).putExtra("group_id", groupId), REQUEST_CODE);
                }
            }
        });
    }

    @OnClick(id = R.id.nav_group_announcement)
    private void GroupAnnouncementClick() {
        startActivity(new Intent(mContext, GroupAnnouncementActivity.class).putExtra("group_id", groupId));
    }

    @OnClick(id = R.id.nav_clear_chat_history)
    void onClearChatHistoryClick() {
        ConversationUtil.performClearChatHistory(GroupDetailActivity.this, groupId, false);
    }

    @OnClick(id = R.id.nav_change_group_admin)
    void onChangeGroupAdminClick() {
//        Intent intent = new Intent(GroupDetailActivity.this, GroupMembersActivity.class);
//        intent.putExtra("group_id", groupId);
//        intent.putExtra(GroupMembersActivity.EXTRA_TYPE, GroupMembersActivity.TYPE_CHANGE_ADMIN);
        Intent intent = new Intent(GroupDetailActivity.this, GroupEditActivity.class);
        intent.putExtra("group_id", groupId);
        intent.putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_CHANGE_ADMIN);
        startActivity(intent);
    }

    @OnClick(id = R.id.nav_set_group_manager)
    void onSetGroupManagerClick() {
//        Intent intent = new Intent(GroupDetailActivity.this, GroupMembersActivity.class);
//        intent.putExtra("group_id", groupId);
//        intent.putExtra(GroupMembersActivity.EXTRA_TYPE, GroupMembersActivity.TYPE_SET_MANAGER);
        Intent intent = new Intent(GroupDetailActivity.this, GroupEditActivity.class);
        intent.putExtra("group_id", groupId);
        intent.putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_SET_MANAGER);
        startActivity(intent);
    }

    @OnClick(id = R.id.leaveGroup)
    private void setLeaveGroup() {

        GroupManager.getInstance().LeaveGroup(groupId, new RequestHandler.RequestListener<CommonResponse>() {
            @Override
            public void onResponse(CommonResponse response, Bundle bundle) {
                ConversationManager.getInstance().deleteConversationByGroupId(groupId);
            }
        });

    }

    private void initListener() {

        saveAddress.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {

                ContactsManager contactsManager = ContactsManager.getInstance();
                if (isSwitchOn) {
                    contactsManager.addContact(mContext, groupId, ContactsManager.CONTACT_TYPE_GROUP,
                            new RequestHandler.RequestListener<AddContactResponse>() {
                                @Override
                                public void onResponse(AddContactResponse response, Bundle bundle) {
                                    GroupInContactDao.getInstance().add(groupId);
                                }
                            });
                } else {
                    contactsManager.RemoveContact(GroupDetailActivity.this, groupId, ContactsManager.CONTACT_TYPE_GROUP, new RequestHandler.RequestListener<RemoveContactResponse>() {
                        @Override
                        public void onResponse(RemoveContactResponse response, Bundle bundle) {
                            GroupInContactDao.getInstance().remove(groupId);
                        }
                    });
                }
            }
        });

        mSwitchTopChat.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {

            @Override
            public void onSwitched(boolean isSwitchOn) {
                ConversationManager.getInstance().setConversationTopChat(groupId, TopModel.ID_TYPE_GROUP, isSwitchOn);
            }
        });

        messageNotDisturb.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().updateGroupSetting(groupId, isSwitchOn, 1);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE == requestCode) {
            if (GroupNameUpdateActivity.RESULT_CODE_RENAME_GROUP == resultCode) {
                if (StringUtils.isNotBlank(data)) {
                    mNavGroupName.mTvSummary.setText(data.getStringExtra("group_name"));
                }
            }
        }
    }

    private void initData() {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        GroupManager.getInstance().getUserInfoListById(groupId, 20, new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> userInfoList) {
                Message message = Message.obtain();
                message.obj = userInfoList;
                mHandler.sendMessage(message);
            }
        });

        Observable.just(groupId)
                .observeOn(Schedulers.io())
                .map(new Func1<String, GroupInfo>() {
                    @Override
                    public GroupInfo call(String id) {
                        return GroupInfoDao.getInstance().selectWithId(id);
                    }
                })
                .filter(new Func1<GroupInfo, Boolean>() {
                    @Override
                    public Boolean call(GroupInfo groupInfo) {
                        return groupInfo != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GroupInfo>() {
                    @Override
                    public void call(GroupInfo groupInfo) {
                        mGroupInfo = groupInfo;
                        mNavGroupName.mTvSummary.setText(TextUtils.isEmpty(groupInfo.GroupName) ? "" : groupInfo.GroupName);
                        mNavGroupAnnouncement.mTvSummary.setText(TextUtils.isEmpty(groupInfo.AnnouncementText) ? getString(R.string.group_announcement_empty) : groupInfo.AnnouncementText);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        List<UserInfo> list = (List<UserInfo>) msg.obj;
        if (list != null) {
            if (list.size() > 9) {
                list = list.subList(0, 9);
            }
            GroupUserInfoAdapter adapter = new GroupUserInfoAdapter(mContext, list);
            adapter.setOnMoreGroupMemberClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GroupMembersActivity.class).putExtra("group_id", groupId));
                }
            });
            mGridView.setAdapter(adapter);
        }
    }


    @OnClick(id = {R.id.imv_add_group_people, R.id.imv_remove_group_people})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.imv_add_group_people:
                startActivity(new Intent(mContext, GroupEditActivity.class).putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_ADD_PEOPLE).putExtra("group_id", groupId));
                break;
            case R.id.imv_remove_group_people:
                startActivity(new Intent(mContext, GroupEditActivity.class).putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_REMOVE_PEOPLE).putExtra("group_id", groupId));
                break;
        }
    }


    private void updateTitle(final GroupInfo groupInfo) {
        if (TextUtils.isEmpty(groupInfo.GroupName)) {
            mHeaderLayout.mTvTitle.setText(R.string.group_chat);
            mNavGroupName.mTvSummary.setText(R.string.group_chat);
        } else {
            mHeaderLayout.mTvTitle.setText(groupInfo.GroupName);
            mNavGroupName.mTvSummary.setText(groupInfo.GroupName);
        }
    }

    private void initView() {

        int size = 0;
        mAvatarView.setGroupId(groupId);
        GroupInfo groupInfo = GroupInfoDao.getInstance().selectWithId(groupId);
        if (StringUtils.isNotBlank(groupInfo)) {
            size = groupInfo.GroupUsers.size();
            updateTitle(groupInfo);
        }
        groupMembers.setText(getString(R.string.group_members, size + ""));
        mHeaderLayout.mLayoutLeftContainer.removeAllViews();
        mHeaderLayout.transparent().autoCancel(GroupDetailActivity.this);

        boolean isTopChat = TopChatManager.getInstance().isTop(groupId);
        mSwitchTopChat.mSwitchButton.initSwitchState(isTopChat);
        boolean isSaveToContact = GroupInContactDao.getInstance().isSaveToContact(groupId);
        saveAddress.mSwitchButton.initSwitchState(isSaveToContact);
        messageNotDisturb.mSwitchButton.initSwitchState(UserSettingManager.getInstance().isTargetIsDisturb(groupId));
        initRight();

        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        GroupManager.getInstance().getGroupRightByUserId(groupId, currentUserId, new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer integer) {
                if (integer > 1) {
                    mNavGroupName.setEnabled(false);
                    mNavGroupName.setNavAlpha();
                }
            }
        });

        groupMembers.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
    }

    private void initRight() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        GroupManager.getInstance().getGroupRightByUserId(groupId, currentUserId, new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer integer) {
                mImvAddPeople.setVisibility(integer <= 1 ? View.VISIBLE : View.GONE);
                mImvRemovePeople.setVisibility(integer <= 1 ? View.VISIBLE : View.GONE);
//                mNavGroupName.setVisibility(integer <= 1 ? View.VISIBLE : View.GONE);
                mLLAdminPreference.setVisibility(integer == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Subscribe
    public void onGroupInfoEvent(GroupInfoEvent event) {
        if (groupId.equals(event.groupInfo.GroupID)) {
            initData();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    initView();
                }
            });
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (!TextUtils.isEmpty(groupId) && e.getAction() == MotionEvent.ACTION_DOWN && !TextUtils.equals(BuildConfig.BUILD_TYPE, "product")) {
                Intent intent = new Intent(GroupDetailActivity.this, AppDetailActivity.class);
                intent.putExtra(AppDetailActivity.EXTRA_TYPE, AppDetailActivity.TYPE_GROUP_INFO);
                intent.putExtra(AppDetailActivity.EXTRA_GROUP, mGroupInfo);
                startActivity(intent);
            }
            return true;
        }
    }

    @Subscribe
    public void onUserSettingsEvent(UserSettingsEvent event) {
        initView();
    }
}
