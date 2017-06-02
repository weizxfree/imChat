package com.itutorgroup.tutorchat.phone.fragment.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.igexin.sdk.PushManager;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.MainActivity;
import com.itutorgroup.tutorchat.phone.activity.account.LoginActivity;
import com.itutorgroup.tutorchat.phone.activity.settings.SettingFontSizeActivity;
import com.itutorgroup.tutorchat.phone.activity.settings.SettingLanguageActivity;
import com.itutorgroup.tutorchat.phone.app.BaseFragment;
import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GlobalActionEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UserSettingsEvent;
import com.itutorgroup.tutorchat.phone.domain.request.DisconnectRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CheckClientVersionResponse;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.ui.common.CommonSwitchButton;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.UserInfoGroup;
import com.itutorgroup.tutorchat.phone.ui.common.item.AbsItemView;
import com.itutorgroup.tutorchat.phone.ui.common.item.NavItemView;
import com.itutorgroup.tutorchat.phone.ui.common.item.SwitchItemView;
import com.itutorgroup.tutorchat.phone.ui.dialog.ConfirmDialog;
import com.itutorgroup.tutorchat.phone.ui.dialog.UpdateVersionDialog;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.message.ConversationUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.AvatarPhotoHelper;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tom_zxzhang on 2016/8/18.
 */
public class MyFragment extends BaseFragment implements MainActivity.IPagerFragmentListener {

    @InjectView(id = R.id.newMessageWarn)
    SwitchItemView newMessageWarn;
    @InjectView
    SwitchItemView soundSetting;
    @InjectView
    SwitchItemView vibrateSetting;
    @InjectView
    SwitchItemView notDisturbStateView;
    @InjectView(id = R.id.item_app_version)
    AbsItemView mItemAppVersion;
    @InjectView(id = R.id.item_server_env)
    AbsItemView mServerEnv;
    @InjectView(id = R.id.item_local_env)
    NavItemView mLocalEnv;
    @InjectView(id = R.id.item_font_size)
    NavItemView mFontSize;

    @InjectView(id = R.id.group_user_info)
    UserInfoGroup mGroupUserInfo;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    private AvatarPhotoHelper.IPickPhotoListener mPickPhotoListener;

    public static final String UPDATE_SERVICE = "com.itutorgroup.tutorchat.phone.service.UpdateService";

    public static MyFragment newInstance() {
        MyFragment newFragment = new MyFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AvatarPhotoHelper.IPickPhotoListener) {
            mPickPhotoListener = (AvatarPhotoHelper.IPickPhotoListener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, null);
        Injector.injectInto(this, view);
        initView();
        initListener();
        return view;
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_personal_center));
        UserInfo user = AccountManager.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        SettingsModel settings = UserSettingManager.getInstance().getMySettings();
        initSettingsState(settings);
        initProfile(user);
        String appVersion = AppUtils.getVersionName(getContext());
        mItemAppVersion.mTvSummary.setText(appVersion);
        if (!TextUtils.equals(APIConstant.API_ENV, APIConstant.ENV_PRODUCT)) {
            mServerEnv.mTvSummary.setText(APIConstant.API_ENV);
        }

        loadData();
    }

    private void loadData() {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<MyFragment, String>() {
                    @Override
                    public String call(MyFragment myFragment) {
                        return AppManager.getInstance().getCurrentLocale();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mLocalEnv.mTvSummary.setText(s);
                    }
                }, CommonUtil.ACTION_EXCEPTION);

        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<MyFragment, String>() {
                    @Override
                    public String call(MyFragment myFragment) {
                        return AppManager.getInstance().getCurrentFontScale();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mFontSize.mTvSummary.setText(s);
                    }
                }, CommonUtil.ACTION_EXCEPTION);

    }

    private void initProfile(UserInfo user) {
        mGroupUserInfo.setUserInfo(user);
    }

    private String formatNullText(String text) {
        if (TextUtils.isEmpty(text)) {
            return getString(R.string.user_info_item_blank);
        } else {
            return text;
        }
    }

    @OnClick(id = R.id.imv_header)
    void onAvatarClick() {
        if (mPickPhotoListener != null) {
            mPickPhotoListener.onOpenPicker();
        }
    }

    @OnClick(id = R.id.btn_logout)
    void onLogoutClick() {
        new ConfirmDialog(getActivity())
                .message(getString(R.string.dialog_message_confirm_logout))
                .cancelText(getString(R.string.cancel))
                .confirmText(getString(R.string.confirm))
                .confirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DisConnectRequest();
                        AccountManager.getInstance().logout();
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                }).show();
    }

    @OnClick(id = R.id.item_clear_all_chat_history)
    void onClick(View view) {
        ConversationUtil.performRemoveAllConversation(getActivity());
    }

    @OnClick(id = R.id.item_server_env)
    void onServerEnvClick(View view) {
        AppManager.getInstance().showServerEnv(getActivity());
    }

    @OnClick(id = R.id.item_local_env)
    void onLocalEnvClick(View view) {
        startActivity(new Intent(getActivity(), SettingLanguageActivity.class));
    }

    @OnClick(id = R.id.item_font_size)
    void onFontSizeClick(View view) {
        startActivity(new Intent(getActivity(), SettingFontSizeActivity.class));
    }

    @Subscribe
    public void onUpdateUserInfoEvent(UpdateCurrentUserInfoEvent event) {
        if (event.mCurrentUserInfo != null && event.mState == 0) {
            initView();
        }
    }

    @Subscribe
    public void onUserSettingsEvent(UserSettingsEvent event) {
        initSettingsState(event.model);
    }

    private void initSettingsState(SettingsModel settings) {
        if (settings == null) {
            return;
        }
        soundSetting.mSwitchButton.initSwitchState(AccountManager.getInstance().isSoundEnabled());
        vibrateSetting.mSwitchButton.initSwitchState(AccountManager.getInstance().isVibrateEnabled());
        newMessageWarn.mSwitchButton.initSwitchState(settings.NewsNoticed == 1);
        if (UserSettingManager.getInstance().queryIsHaveDisturbRight()) {
            notDisturbStateView.setVisibility(View.VISIBLE);
            notDisturbStateView.mSwitchButton.initSwitchState(settings.IsDisturb == 1);
        } else {
            notDisturbStateView.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        newMessageWarn.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
//                if (isSwitchOn) {
//                    PushManager.getInstance().turnOnPush(getContext());
//                } else {
//                    PushManager.getInstance().turnOffPush(getContext());
//                }
                UserSettingManager.getInstance().setNewsNoticeDisturb(isSwitchOn ? 1 : 0);
            }
        });
        soundSetting.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().setSoundEnable(isSwitchOn);
            }
        });

        vibrateSetting.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().setVibrateEnable(isSwitchOn);
            }
        });

        notDisturbStateView.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                UserSettingManager.getInstance().setDisturbSate(isSwitchOn);
            }
        });


    }

    private void DisConnectRequest() {
        DisconnectRequest disconnectRequest = new DisconnectRequest();
        disconnectRequest.init();
        disconnectRequest.DeviceID = PushManager.getInstance().getClientid(mContext);
        disconnectRequest.DeviceType = Constant.DEVICE_TYPE;
        new RequestHandler<>()
                .operation(Operation.DISCONNECT)
                .request(disconnectRequest)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<>());
    }

    @Override
    public void onShowFragment() {
        EventBusManager.getInstance().post(GlobalActionEvent.getInstance(GlobalActionEvent.ACTION_AUTO_REFRESH_CURRENT_USER_INFO));
        UserSettingManager.getInstance().loadUserSettings();
        checkVersionStatus();
        if (mGroupUserInfo != null && mGroupUserInfo.getUserInfo() == null) {
            mGroupUserInfo.setUserInfo(AccountManager.getInstance().getCurrentUser());
        }
    }

    private void checkVersionStatus() {
        final CheckClientVersionResponse.UpdateVersion updateVersion = Constant.updateVersion;
        if (updateVersion != null && AppUtils.getVersionCode(mContext) < updateVersion.VersionNum && mItemAppVersion != null) {
            mItemAppVersion.mSubTvTitle.setVisibility(View.VISIBLE);
            mItemAppVersion.mSubTvTitle.setText("NEW");
            mItemAppVersion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppUtils.isServiceRunning(mContext, UPDATE_SERVICE)) {
                        ToastUtil.show(R.string.str_client_version_update_is_downloading);
                    } else {
                        UpdateVersionDialog.showUpdateVersionDialog(mContext, updateVersion.IsForce);
                    }
                }
            });
        } else {
            if (mItemAppVersion != null) {
                mItemAppVersion.mSubTvTitle.setVisibility(View.GONE);
                mItemAppVersion.setOnClickListener(null);
            }
        }
    }


}



