package com.itutorgroup.tutorchat.phone.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.itutorgroup.tutorchat.phone.IKernelServiceInterface;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.account.LoginActivity;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageEvent;
import com.itutorgroup.tutorchat.phone.domain.event.NetworkEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.CheckClientVersionRequest;
import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;
import com.itutorgroup.tutorchat.phone.domain.request.ConnectRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CheckClientVersionResponse;
import com.itutorgroup.tutorchat.phone.domain.response.ConnectResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetRightByUserIDResponse;
import com.itutorgroup.tutorchat.phone.fragment.main.ContactsFragment;
import com.itutorgroup.tutorchat.phone.fragment.main.ConversationFragment;
import com.itutorgroup.tutorchat.phone.fragment.main.MyFragment;
import com.itutorgroup.tutorchat.phone.service.DataService;
import com.itutorgroup.tutorchat.phone.service.ReceiveService;
import com.itutorgroup.tutorchat.phone.ui.dialog.UpdateVersionDialog;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.message.ConversationUtil;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.AvatarPhotoHelper;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;
import com.jude.swipbackhelper.SwipeBackHelper;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.InjectViews;
import cn.salesuite.saf.inject.annotation.OnClick;
import cn.salesuite.saf.utils.StringUtils;

public class MainActivity extends BaseActivity implements AvatarPhotoHelper.IPickPhotoListener {

    @InjectView(id = R.id.view_pager_main)
    private ViewPager mViewPager;

    @InjectViews(ids = {R.id.tab_message, R.id.tab_contacts, R.id.tab_me})
    View[] mTabs;

    @InjectView(id = R.id.tv_unread_count)
    TextView mTvUnreadCount;

    @InjectView(id = R.id.tab_newMessage)
    TextView mTvNewMessage;

    private ConversationFragment mChatFragment;
    private ContactsFragment mContactsFragment;
    private MyFragment mMyFragment;
    private List<Fragment> mFragmentList;
    private PagerAdapter mAdapter;
    Intent mReceiveServiceIntent;

    private AvatarPhotoHelper mAvatarPhotoHelper;

    private int mCurrentIndex;

    ReceiveService receiveService;

    private boolean isWifiInterupte = false;

    public static CheckClientVersionResponse.UpdateVersion updateVersion;

    @Override
    public void onOpenPicker() {
        mAvatarPhotoHelper.showChooseAvatarDialog(findViewById(R.id.container));
    }

    public interface IPagerFragmentListener {
        void onShowFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PushManager.getInstance().initialize(this.getApplicationContext());
        CheckClientVersion();
        initView();
        ConnectTask();
        initListener();
        startService();
        AppManager.initAutoStartPermission(this);
        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onConversationEvent(ConversationEvent.getInstance());
        if (mService == null) {
            Intent intent = new Intent(this, DataService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void startService() {
        if (Kernel.getInstance().isPullEnabled()) {
            mReceiveServiceIntent = new Intent(mContext, ReceiveService.class);
            bindService(mReceiveServiceIntent, conn, Context.BIND_AUTO_CREATE);
            startService(mReceiveServiceIntent);
        }

        Kernel.getInstance().startTcpService(this);
    }

    private IKernelServiceInterface mService;

    public IKernelServiceInterface getKernel() {
        return mService;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IKernelServiceInterface.Stub.asInterface(service);
            mChatFragment.resetKernel();
            Kernel.getInstance().resumeTcpIfDied(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mChatFragment.resetKernel();
        }
    };

    private void stopService() {
        if (StringUtils.isNotBlank(mReceiveServiceIntent)) {
            try {
                if (StringUtils.isNotBlank(receiveService)) {
                    receiveService.shutdown();
                }
                if (StringUtils.isNotBlank(conn)) {
                    unbindService(conn);
                }
                stopService(mReceiveServiceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initView() {
        mAvatarPhotoHelper = new AvatarPhotoHelper(this);
        mChatFragment = ConversationFragment.newInstance();
        mContactsFragment = ContactsFragment.newInstance();
        mMyFragment = MyFragment.newInstance();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mChatFragment);
        mFragmentList.add(mContactsFragment);
        mFragmentList.add(mMyFragment);
        mAdapter = new PagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(0);
        mTabs[0].setSelected(true);

    }

    private long mFirstClickConversationTime;

    @OnClick(id = {R.id.tab_message, R.id.tab_contacts, R.id.tab_message_rl})
    void onTabClick(View view) {
        switch (view.getId()) {
            case R.id.tab_message:
                mViewPager.setCurrentItem(0);
                if (mFirstClickConversationTime + 2000 > System.currentTimeMillis()) {
                    mChatFragment.onDoubleClickTab();
                    mFirstClickConversationTime = 0;
                } else {
                    mFirstClickConversationTime = System.currentTimeMillis();
                }
                break;
            case R.id.tab_contacts:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.tab_message_rl:
                mViewPager.setCurrentItem(2);
                if (mTvNewMessage.isShown()) {
                    AppManager.getInstance().saveNewVersionClick(mContext);
                    mTvNewMessage.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void initListener() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabs[mCurrentIndex].setSelected(false);
                mCurrentIndex = position;
                mTabs[position].setSelected(true);
                if (mFragmentList.get(position) instanceof IPagerFragmentListener) {
                    ((IPagerFragmentListener) mFragmentList.get(position)).onShowFragment();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private void ConnectTask() {
        getRightByUserId();
        UserSettingManager.getInstance().loadUserSettings();

        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
//            EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(NetworkError.ERROR_INVALID_USER_ID));
            return;
        }
        ConnectRequest connectRequest = new ConnectRequest(
                Constant.MESSAGE_DEVICE_TYPE, currentUserId, AccountManager.getInstance().getToken(), AccountManager.getInstance().getClientId(), Constant.DEVICE_TYPE);
        connectRequest.LanguageType = AppManager.getInstance().getCurrentLanguageType();
        ConnectAsyncTask(connectRequest);
    }


    /**
     * 连接接口
     */
    private void ConnectAsyncTask(ConnectRequest connectRequest) {
        new RequestHandler<ConnectResponse>()
                .operation(Operation.CONNECT)
                .bundle(new NBundle().addIgnoreToastErrorCode(NetworkError.ERROR_INVALID_USER_ID).build())
                .request(connectRequest)
                .exec(ConnectResponse.class, new RequestHandler.RequestListener<ConnectResponse>() {
                    @Override
                    public void onResponse(ConnectResponse t, Bundle bundle) {
                    }

                    @Override
                    public void onError(int errorCode, ConnectResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        if (e == null) {
                            EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(errorCode));
                        } else {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNullResponse(Bundle bundle) {
                        super.onNullResponse(bundle);
                        EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(-1));
                    }
                });
    }

    private void getRightByUserId() {
        final CommonRequest request = new CommonRequest();
        request.init();
        new RequestHandler()
                .operation(Operation.GET_RIGHT_BY_USER_ID)
                .request(request)
                .exec(GetRightByUserIDResponse.class, new RequestHandler.RequestListener<GetRightByUserIDResponse>() {
                    @Override
                    public void onResponse(GetRightByUserIDResponse response, Bundle bundle) {
                        AccountManager.getInstance().setRightList(response.RightCodeList);
                    }
                });
    }


    class PagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> list;

        public PagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int index) {
            return list.get(index);
        }

        public void setList(List<Fragment> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }


    private static long mFirstBackTime;

    @Override
    public void onBackPressed() {
        if (mFirstBackTime + 2000 < System.currentTimeMillis()) {
            ToastUtil.show(R.string.finish_by_back_again);
        } else {
//            System.exit(0);
            super.onBackPressed();
        }
        mFirstBackTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatFragment = null;
        mContactsFragment = null;
        mMyFragment = null;
        mFragmentList = null;
        mViewPager.setOnPageChangeListener(null);
        mAdapter = null;
        mTabs = null;
        stopService();
        unbindService(mConnection);
    }

    @Subscribe
    public void onUpdateUserInfoEvent(UpdateCurrentUserInfoEvent event) {
        if (event.mState != 0) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra(LoginActivity.EXTRA_ERROR_INFO, event.mState);
            startActivity(intent);
            finish();
        }
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        mHandler.sendEmptyMessageDelayed(100, 200);
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 100) {
            onConversationEvent(null);
        }
    }

    @Subscribe
    public void onConversationEvent(ConversationEvent event) {
        if (Kernel.getInstance().isTcpReceiveOfflineMessage(mService)) {
            return;
        }
        ConversationManager.getInstance().loadAllUnreadCount(new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer i) {
                ConversationUtil.setUnreadCountAndBg(mTvUnreadCount, i);
            }
        });
    }


    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            receiveService = ((ReceiveService.MsgBinder) service).getService();

        }
    };

    @Subscribe
    public void onWifiEvent(NetworkEvent event) {
        if (event.mIsConnect) {
            if (isWifiInterupte) {
                startService();
                isWifiInterupte = false;
            }
        } else {
            stopService();
            isWifiInterupte = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAvatarPhotoHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void CheckClientVersion() {
        final CheckClientVersionRequest request = new CheckClientVersionRequest();
        request.init();
        request.versionNum = AppUtils.getVersionCode(mContext);
        request.clientType = Constant.DEVICE_TYPE;
        new RequestHandler()
                .operation(Operation.CLIENT_UPDATE)
                .request(request)
                .exec(CheckClientVersionResponse.class, new RequestHandler.RequestListener<CheckClientVersionResponse>() {
                    @Override
                    public void onResponse(CheckClientVersionResponse response, Bundle bundle) {
                        if (StringUtils.isNotBlank(response) && StringUtils.isNotBlank(response.Version)) {
                            Constant.updateVersion = response.Version;
                            if (request.versionNum < response.Version.VersionNum) {
                                if (!AppManager.getInstance().getNewVersionHasClick(mContext)) {
                                    mTvNewMessage.setVisibility(View.VISIBLE);
                                }
                                UpdateVersionDialog.showUpdateVersionDialog((Activity) mContext, response.Version.IsForce);
                            }
                        }
                    }

                    @Override
                    public void onError(int errorCode, CheckClientVersionResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                    }
                });
    }


}
