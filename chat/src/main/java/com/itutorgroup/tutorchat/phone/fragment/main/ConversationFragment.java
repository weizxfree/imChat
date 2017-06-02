package com.itutorgroup.tutorchat.phone.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.MainActivity;
import com.itutorgroup.tutorchat.phone.activity.search.SearchActivity;
import com.itutorgroup.tutorchat.phone.adapter.ConversationAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseFragment;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.beans.ConversationItem;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.MessageEvent;
import com.itutorgroup.tutorchat.phone.domain.event.NetworkEvent;
import com.itutorgroup.tutorchat.phone.domain.event.SystemNoticeEvent;
import com.itutorgroup.tutorchat.phone.domain.event.TcpStateChangeEvent;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.receiver.MainGlobalReceiver;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.SystemNoticeView;
import com.itutorgroup.tutorchat.phone.ui.common.scroll.MyVerticalScrollLinearLayout;
import com.itutorgroup.tutorchat.phone.ui.popup.HomePopWindow;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SystemNoticeManager;

import java.util.ArrayList;
import java.util.List;

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
 * 会话聊天页面数据从缓存取
 */
public class ConversationFragment extends BaseFragment implements AbsListView.OnScrollListener, MainActivity.IPagerFragmentListener {

    @InjectView(id = R.id.lv_conversation)
    ListView mLvConversation;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.ll_list_container)
    MyVerticalScrollLinearLayout mListContainer;

    @InjectView(id = R.id.notice_container)
    FrameLayout mNoticeContainer;

    @InjectView(id = R.id.view_empty)
    View mEmptyView;

    private View mWifiTipView;
    private SystemNoticeView mNoticeView;

    private ConversationAdapter mAdapter;

    private List<ConversationItem> mData;

    private PopupWindow mPopupWindow;

    private GestureDetectorCompat mGestureDetectorCompat;

    private boolean mIsResume;

    private boolean mIsWifiViewVisible;
    private long mFirstLoadTime = 0;

    private MainActivity mKernelProxy;

    public static ConversationFragment newInstance() {
        ConversationFragment newFragment = new ConversationFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, null);
        Injector.injectInto(this, view);
        initView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsResume = false;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mAdapter != null) {
//            mAdapter.notifyDataSetChanged();
//        }
        mIsResume = true;
        onConversationEvent(ConversationEvent.getInstance());
        mListContainer.onResume();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.app_name))
                .rightImage(R.drawable.add_nav, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupWindow(mHeaderLayout.mLayoutRightContainer);
                    }
                });
        mGestureDetectorCompat = new GestureDetectorCompat(getActivity(), new MyGestureListener());
        mHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });

        mWifiTipView = new SystemNoticeView(getActivity(), SystemNoticeView.TYPE_WIFI_DISCONNECT);
        mNoticeView = new SystemNoticeView(getActivity(), SystemNoticeView.TYPE_SYSTEM_NOTICE);

        getContext().sendBroadcast(new Intent(MainGlobalReceiver.ACTION_REQUEST_NETWORK_STATE));
        mAdapter = new ConversationAdapter(mContext, null);
        mLvConversation.setAdapter(mAdapter);
        mLvConversation.setOnScrollListener(this);
        mLvConversation.setEmptyView(mEmptyView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof MainActivity) {
            mKernelProxy = (MainActivity) activity;
        }
    }

    @OnClick(id = R.id.search)
    public void onSearchClick(View view) {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLvConversation.setOnScrollListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        updateTitle();
    }

    private void updateTitle() {
        Observable.just(AppManager.getInstance().hasNetwork())
                .subscribeOn(Schedulers.io())
                .map(new Func1<Boolean, String>() {
                    @Override
                    public String call(Boolean hasNetwork) {
                        if (hasNetwork && mKernelProxy != null) {
                            boolean connect = Kernel.getInstance().isTcpConnect(mKernelProxy.getKernel());
                            boolean online = Kernel.getInstance().isTcpOnline(mKernelProxy.getKernel());
                            if (!connect) {
                                return getString(R.string.title_conversation_connect_tcp);
                            } else if (connect && !online) {
                                return getString(R.string.title_conversation_receive_offline_msg);
                            }
                        }
                        return LPApp.getInstance().getString(R.string.app_name);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String title) {
                        if (mHeaderLayout != null) {
                            mHeaderLayout.title(title);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void showPopupWindow(View view) {
        if (mPopupWindow == null) {
            mPopupWindow = new HomePopWindow(getActivity());
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    backgroundAlpha(1.0f);
                }
            });
        }
        mPopupWindow.showAsDropDown(view, -PixelUtil.dp2px(80), -PixelUtil.dp2px(10));
        backgroundAlpha(0.5f);
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    private Runnable mDelayRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            EventBusManager.getInstance().post(ConversationEvent.getInstance());
        }
    };

    private void loadData() {
        if (mFirstLoadTime == 0) {
            mFirstLoadTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - mFirstLoadTime < 1000) {
            mHandler.removeCallbacks(mDelayRefreshRunnable);
            mHandler.postDelayed(mDelayRefreshRunnable, 1000);
            return;
        }
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .filter(mFilterCanLoadData)
                .map(new Func1<ConversationFragment, List<ConversationItem>>() {
                    @Override
                    public List<ConversationItem> call(ConversationFragment conversationFragment) {
                        List<ConversationItem> list = ConversationManager.getInstance().getConversationList(null);
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ConversationItem>>() {
                    @Override
                    public void call(List<ConversationItem> list) {
                        setDataToList(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void setDataToList(List<ConversationItem> list) {
        if (mAdapter == null) {
            mAdapter = new ConversationAdapter(mContext, list);
            mLvConversation.setAdapter(mAdapter);
        } else {
            mAdapter.setData(list);
        }
        mData = list;
        if (mData == null || mData.size() == 0) {
            mListContainer.scrollToChildPosition(0);
        }
        updateTitle();
    }

    public void onDoubleClickTab() {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ConversationFragment, Integer>() {
                    @Override
                    public Integer call(ConversationFragment conversationFragment) {
                        int position = 0;
                        if (mData != null && mData.size() > 0) {
                            int len = mData.size();
                            for (int i = 0; i < len; i++) {
                                ConversationItem item = mData.get(i);
                                if (item.unReadCount > 0) {
                                    position = mLvConversation.getHeaderViewsCount() + i;
                                    break;
                                }
                            }
                        }
                        return mLvConversation.getFirstVisiblePosition() == position ? 0 : position;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer i) {
                        mListContainer.scrollToChildPosition(i == 0 ? 0 : 1);
                        mLvConversation.setSelection(i);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: // 停止滚动
                mAdapter.setScrollState(false);
                mAdapter.notifyDataSetChanged();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                mAdapter.setScrollState(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mAdapter.setScrollState(true);
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void resetKernel() {
        updateTitle();
    }

    @Override
    public void onShowFragment() {
        updateTitle();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mListContainer.scrollToChildPosition(0);
            return true;
        }
    }

    @Subscribe
    public void onConversationEvent(ConversationEvent event) {
        if (mIsResume) {
            if (event != null && event.mState == ConversationEvent.STATE_REFRESH) {
                if (event.mIdList != null && event.mIdList.size() > 0) {
                    updatePartConversation(event.mIdList);
                }
            } else {
                loadData();
                updateTitle();
            }
        }
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        if (mIsResume) {
            if (event != null && event.list != null) {
                updateMessageToConversation(event.list);
            }
        }
    }

    private void updateMessageToConversation(List<MessageModel> messageList) {
        Observable.just(messageList)
                .subscribeOn(Schedulers.io())
                .filter(mFilterCanLoadData)
                .map(new Func1<List<MessageModel>, List<ConversationItem>>() {
                    @Override
                    public List<ConversationItem> call(List<MessageModel> list) {
                        List<ConversationItem> conversationList = new ArrayList<>();
                        List<String> recallMessageTargetList = new ArrayList<>();
                        for (MessageModel model : list) {
                            if (model != null) {
                                conversationList.add(ConversationManager.getInstance().getConversationItemByMessageModel(model));
                            }
                        }
                        return conversationList;
                    }
                })
                .filter(CommonUtil.FUNC_FILTER_LIST_NOT_EMPTY)
                .map(mFuncMergeConversationList)
                .map(mFuncSortConversation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActionSetData, CommonUtil.ACTION_EXCEPTION);
    }

    private void updatePartConversation(List<String> idList) {
        Observable.just(idList)
                .subscribeOn(Schedulers.io())
                .filter(mFilterCanLoadData)
                .map(new Func1<List<String>, List<ConversationItem>>() {
                    @Override
                    public List<ConversationItem> call(List<String> list) {
                        return ConversationManager.getInstance().getConversationList(list);
                    }
                })
                .filter(CommonUtil.FUNC_FILTER_LIST_NOT_EMPTY)
                .map(mFuncMergeConversationList)
                .map(mFuncSortConversation)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActionSetData, CommonUtil.ACTION_EXCEPTION);
    }

    private Func1<List<ConversationItem>, List<ConversationItem>> mFuncMergeConversationList = new Func1<List<ConversationItem>, List<ConversationItem>>() {
        @Override
        public List<ConversationItem> call(List<ConversationItem> list) {
            if (mData == null) {
                mData = new ArrayList<>();
            }
            for (ConversationItem item : list) {
                if (item == null) {
                    continue;
                }
                boolean newConversation = true;
                for (ConversationItem t : mData) {
                    if (t == null) {
                        continue;
                    }

                    if (TextUtils.equals(t.targetId, item.targetId)) {
                        if (item.messageModel != null && t.messageModel != null
                                && item.messageModel.Type == MessageType.WITH_DRAWAL
                                && !TextUtils.equals(t.messageModel.MessageID, item.messageModel.MessageID)) {
                            newConversation = false;
                            break;
                        }
                        t.copyFrom(item);
                        newConversation = false;
                        break;
                    }
                }
                if (newConversation) {
                    mData.add(item);
                }
            }
            return mData;
        }
    };

    private Func1<List<ConversationItem>, List<ConversationItem>> mFuncSortConversation
            = new Func1<List<ConversationItem>, List<ConversationItem>>() {
        @Override
        public List<ConversationItem> call(List<ConversationItem> list) {
            return ConversationManager.getInstance().sortConversationList(list);
        }
    };

    private Action1<List<ConversationItem>> mActionSetData = new Action1<List<ConversationItem>>() {
        @Override
        public void call(List<ConversationItem> list) {
            setDataToList(list);
        }
    };

    private Func1<Object, Boolean> mFilterCanLoadData = new Func1<Object, Boolean>() {
        @Override
        public Boolean call(Object o) {
            return !(AppManager.getInstance().hasNetwork() && Kernel.getInstance().isTcpReceiveOfflineMessage(mKernelProxy.getKernel()));
        }
    };

    @Subscribe
    public void onTcpStateChangeEvent(TcpStateChangeEvent event) {
        updateTitle();
    }

    @Subscribe
    public void onWifiEvent(NetworkEvent event) {
        mNoticeContainer.removeAllViews();
        if (event.mIsConnect) {
            mIsWifiViewVisible = false;
            SystemNoticeManager.getInstance().loadSystemNotice();
        } else {
            mIsWifiViewVisible = true;
            mNoticeContainer.addView(mWifiTipView);
        }
        onConversationEvent(ConversationEvent.getInstance());
    }

    @Subscribe
    public void onSystemNoticeEvent(SystemNoticeEvent event) {
        Observable.just(event)
                .subscribeOn(Schedulers.io())
                .map(new Func1<SystemNoticeEvent, SystemNoticeModel>() {
                    @Override
                    public SystemNoticeModel call(SystemNoticeEvent systemNoticeEvent) {
                        return systemNoticeEvent.model;
                    }
                })
                .filter(new Func1<SystemNoticeModel, Boolean>() {
                    @Override
                    public Boolean call(SystemNoticeModel model) {
                        return !mIsWifiViewVisible;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SystemNoticeModel>() {
                    @Override
                    public void call(SystemNoticeModel model) {
                        mNoticeContainer.removeAllViews();
                        if (model != null) {
                            mNoticeContainer.addView(mNoticeView);
                            mNoticeView.setContent(model.MsgBody);
                        }
                        loadData();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }
}



