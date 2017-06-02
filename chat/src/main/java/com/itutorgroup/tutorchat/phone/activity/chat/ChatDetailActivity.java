package com.itutorgroup.tutorchat.phone.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ContactsConstraintDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.UserSettingsEvent;
import com.itutorgroup.tutorchat.phone.domain.response.AddContactResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserResponse;
import com.itutorgroup.tutorchat.phone.domain.response.RemoveContactResponse;
import com.itutorgroup.tutorchat.phone.ui.common.CommonSwitchButton;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.UserInfoGroup;
import com.itutorgroup.tutorchat.phone.ui.common.item.SwitchItemView;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.TopChatManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.message.ConversationUtil;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class ChatDetailActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.btn_collection)
    TextView mBtnCollection;

    @InjectExtra(key = "user_info")
    UserInfo userInfo;

    @InjectView(id = R.id.switch_top_chat)
    SwitchItemView mSwitchTopChat;

    @InjectView
    SwitchItemView chatSettingNotDisturb;

    @InjectView
    SwitchItemView chatSettingShieldContact;

    @InjectView(id = R.id.ll_user_settings)
    LinearLayout mLLUserSettings;

    @InjectView(id = R.id.group_user_info)
    UserInfoGroup mGroupUserInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        if (AccountManager.getInstance().getCurrentUser().equals(userInfo)) {
            mLLUserSettings.setVisibility(View.GONE);
        }
        loadTicksInfo();
    }

    private void loadTicksInfo() {
        if (userInfo != null && !TextUtils.isEmpty(userInfo.UserID)) {
            ContactsManager.getInstance().getUserInfo(userInfo.UserID, 0, new RequestHandler.RequestListener<GetUserResponse>() {
                @Override
                public void onResponse(final GetUserResponse response, Bundle bundle) {
                    if (response.User != null && !TextUtils.isEmpty(response.User.UserID)) {
                        userInfo = response.User;
                        initView();
                    }
                }
            });
        }
    }

    @OnClick(id = {R.id.btn_send_message, R.id.btn_collection, R.id.nav_clear_chat_history})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:
                startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", userInfo.UserID));
                break;
            case R.id.btn_collection:
                Observable.just("")
                        .subscribeOn(Schedulers.io())
                        .filter(new Func1<String, Boolean>() {
                            @Override
                            public Boolean call(String s) {
                                return userInfo != null && !TextUtils.isEmpty(userInfo.UserID);
                            }
                        })
                        .map(new Func1<Object, Boolean>() {
                            @Override
                            public Boolean call(Object o) {
                                return ContactsConstraintDao.getInstance().isMyContact(userInfo.UserID);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean isContact) {
                                if (isContact) {
                                    removeContact();
                                } else {
                                    addContact();
                                }
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
                break;
            case R.id.nav_clear_chat_history:
                clearHistory();
                break;
        }
    }

    private void removeContact() {
        ContactsManager.getInstance().RemoveContact(ChatDetailActivity.this, userInfo.UserID, ContactsManager.CONTACT_TYPE_PERSONAL, new RequestHandler.RequestListener<RemoveContactResponse>() {
            @Override
            public void onResponse(RemoveContactResponse response, Bundle bundle) {
                ToastUtil.show(R.string.common_successful_operation);
                mBtnCollection.setText(R.string.collection);
                ContactsConstraintDao.getInstance().removeContact(userInfo.UserID);
            }
        });
    }

    private void addContact() {
        ContactsManager.getInstance().addContact(mContext, userInfo.UserID, ContactsManager.CONTACT_TYPE_PERSONAL,
                new RequestHandler.RequestListener<AddContactResponse>() {
                    @Override
                    public void onResponse(AddContactResponse response, Bundle bundle) {
                        ToastUtil.show(R.string.common_successful_operation);
                        mBtnCollection.setText(R.string.remove_collection);
                        ContactsConstraintDao.getInstance().saveMyContactsConstraint(userInfo);
                    }
                });
    }

    private void clearHistory() {
        ConversationUtil.performClearChatHistory(ChatDetailActivity.this, userInfo.UserID, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLastUserInfo();
    }


    private void getLastUserInfo() {
        if (userInfo != null && !TextUtils.isEmpty(userInfo.UserID)) {
            UserInfoManager.getInstance().getUserInfo(userInfo.UserID, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo user) {
                    userInfo = user;
                    initView();
                }
            });
        }
    }

    private void initView() {

        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        return ContactsConstraintDao.getInstance().isMyContact(userInfo.UserID);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isContact) {
                        if (isContact) {
                            mBtnCollection.setText(R.string.remove_collection);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
        mHeaderLayout.mLayoutLeftContainer.removeAllViews();
        mHeaderLayout.title(getString(R.string.title_chat_desc))
                .autoCancel(this)
                .transparent();

        mGroupUserInfo.setUserInfo(userInfo);

        boolean isTopChat = TopChatManager.getInstance().isTop(userInfo.UserID);
        mSwitchTopChat.mSwitchButton.initSwitchState(isTopChat);

        UserSettingManager userSettingManager = UserSettingManager.getInstance();
        boolean isDisturb = userSettingManager.isTargetIsDisturb(userInfo.UserID);
        chatSettingNotDisturb.mSwitchButton.initSwitchState(isDisturb);
        boolean isShield = userSettingManager.isTargetIsShield(userInfo.UserID);
        chatSettingShieldContact.mSwitchButton.initSwitchState(isShield);

        mSwitchTopChat.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                ConversationManager.getInstance().setConversationTopChat(userInfo.UserID, TopModel.ID_TYPE_USER, isSwitchOn);
            }
        });
        chatSettingNotDisturb.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().updateChatSetting(userInfo.UserID, isSwitchOn, 1);
            }
        });

        chatSettingShieldContact.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().updateChatSetting(userInfo.UserID, isSwitchOn, 2);
            }
        });
    }

    @Subscribe
    public void onUserSettingsEvent(UserSettingsEvent event) {
        initView();
    }
}
