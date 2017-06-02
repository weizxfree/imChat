package com.itutorgroup.tutorchat.phone.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.MainActivity;
import com.itutorgroup.tutorchat.phone.activity.chat.ServiceAccountActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupListActivity;
import com.itutorgroup.tutorchat.phone.activity.search.SearchActivity;
import com.itutorgroup.tutorchat.phone.adapter.ContactsSortAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseFragment;
import com.itutorgroup.tutorchat.phone.domain.beans.UserInfoVo;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.PinnedSectionItem;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ContactsEvent;
import com.itutorgroup.tutorchat.phone.domain.event.GlobalActionEvent;
import com.itutorgroup.tutorchat.phone.domain.request.GetAllContractRequest;
import com.itutorgroup.tutorchat.phone.domain.response.GetAllContractResponse;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.SideBar;
import com.itutorgroup.tutorchat.phone.ui.common.scroll.MyVerticalScrollLinearLayout;
import com.itutorgroup.tutorchat.phone.ui.popup.HomePopWindow;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;
import com.itutorgroup.tutorchat.phone.utils.pinned.PinnedSectionItemUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;
import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tom_zxzhang on 2016/8/18.
 * 联系人页面
 */
public class ContactsFragment extends BaseFragment implements MainActivity.IPagerFragmentListener {


    private PopupWindow mPopupWindow;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.lv_contacts)
    private ListView mLvContacts;

    @InjectView(id = R.id.side_bar)
    private SideBar mSideBar;

    @InjectView(id = R.id.tv_tip_current_letter)
    TextView mTvLetterTip;

    @InjectView(id = R.id.ll_list_container)
    MyVerticalScrollLinearLayout mScrollContainer;

    @InjectView(id = R.id.view_empty)
    View mEmptyView;

    private ContactsSortAdapter mAdapter;

    private List<UserInfo> userList;

    private List<UserInfoVo> userInfoVoList;

    private GestureDetectorCompat mGestureDetectorCompat;

    public static ContactsFragment newInstance() {
        ContactsFragment newFragment = new ContactsFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, null);
        Injector.injectInto(this, view);
        initView();
        onContactsEvent(ContactsEvent.getInstance());
        initData();
        return view;
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.contacts))
                .leftImage(R.drawable.search_nav, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SearchActivity.class);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE, SearchManager.SEARCH_TYPE_CONTACTS);
                        intent.putExtra(SearchActivity.EXTRA_SEARCH_SOURCE, SearchActivity.SEARCH_SOURCE_NETWORK);
                        getActivity().startActivity(intent);
                    }
                })
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
        mLvContacts.setEmptyView(mEmptyView);
    }

    @OnClick(id = R.id.ll_group_chat_list)
    public void onGroupListClick() {
        startActivity(new Intent(mContext, GroupListActivity.class));
    }

    @OnClick(id = R.id.ll_service_number_list)
    public void onServiceAccountClick() {
        startActivity(new Intent(mContext, ServiceAccountActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
//        initData();
        mScrollContainer.onResume();
        autoUpdateContacts();
    }

    private void autoUpdateContacts() {
        long mLastRequestTime = TicksUtil.getContactsListTick();
        if (mLastRequestTime != 0) {
            ObjectUpdateHelper.autoUpdateContacts(false);
        }
    }

    @Override
    public void onShowFragment() {
//        initData();
        autoUpdateContacts();
        EventBusManager.getInstance().post(GlobalActionEvent.getInstance(GlobalActionEvent.ACTION_AUTO_GET_SERVICE_ACCOUNT_LIST));
    }

    private void initData() {
        final GetAllContractRequest request = new GetAllContractRequest(2, AccountManager.getInstance().getCurrentUserId(), AccountManager.getInstance().getToken());
        new RequestHandler<GetAllContractResponse>()
                .operation(Operation.GET_ALL_CONTRACT)
                .request(request)
                .exec(GetAllContractResponse.class, new RequestHandler.RequestListener<GetAllContractResponse>() {
                    @Override
                    public void onResponse(final GetAllContractResponse response, Bundle bundle) {
                        if (request.Ticks == response.LastUpdateTime) {
                            return;
                        }
                        userList = response.Users;
                        Observable.just("")
                                .subscribeOn(Schedulers.io())
                                .map(new Func1<String, ArrayList<PinnedSectionItem<UserInfoVo>>>() {
                                    @Override
                                    public ArrayList<PinnedSectionItem<UserInfoVo>> call(String s) {
                                        ContactsManager.getInstance().saveContactsToDB(response.Users);
                                        ContactsManager.getInstance().saveGroupsToDB(response.Groups);
                                        userInfoVoList = UserInfoVo.getUserInfoVo(userList);
                                        return PinnedSectionItemUtil.parseUserVO(userInfoVoList);
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<ArrayList<PinnedSectionItem<UserInfoVo>>>() {
                                    @Override
                                    public void call(ArrayList<PinnedSectionItem<UserInfoVo>> pinnedList) {
                                        setDataToUI(pinnedList);
                                    }
                                }, CommonUtil.ACTION_EXCEPTION);
                    }
                });
    }

    private boolean mIsListenerInited = false;

    private void initListener() {
        if (mIsListenerInited) {
            return;
        }

        mSideBar.showTop();
        //设置右侧[A-Z]快速导航栏触摸监听
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置

                if (StringUtils.isNotBlank(mAdapter)) {
                    int position = mAdapter.getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        mLvContacts.setSelection(position);
                        mTvLetterTip.setText(s);
                    }
                    if ("↑".equals(s)) {
                        mScrollContainer.scrollToChildPosition(0);
                    } else if (mAdapter.getCount() > 0) {
                        mScrollContainer.scrollToChildPosition(1);
                    }
                }
            }
        });

        mSideBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTvLetterTip.setVisibility(View.INVISIBLE);
                            }
                        }, 1000);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mTvLetterTip.setVisibility(View.VISIBLE);
                        mHandler.removeCallbacksAndMessages(null);
                        break;
                }
                return false;
            }
        });

        mLvContacts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (StringUtils.isNotBlank(mAdapter)) {
                    int sectionForPosition = mAdapter.getSectionForPosition(firstVisibleItem);
                    mSideBar.updateLetterIndexView(sectionForPosition);
                }

            }
        });
        mIsListenerInited = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLvContacts.setOnScrollListener(null);
        mLvContacts.setAdapter(null);
        mPopupWindow = null;
    }

    private void showPopupWindow(View parent) {
        if (mPopupWindow == null) {
            mPopupWindow = new HomePopWindow(getActivity());
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    backgroundAlpha(1.0f);
                }
            });
        }
        mPopupWindow.showAsDropDown(parent, -PixelUtil.dp2px(80), -PixelUtil.dp2px(10));
        backgroundAlpha(0.5f);
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    @Subscribe
    public void onContactsEvent(ContactsEvent event) {
        ContactsManager.getInstance().getMyContacts(new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> list) {
                if (list == null || list.size() == 0) {
                    setDataToUI(null);
                    return;
                }
                Observable.just(list)
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<List<UserInfo>, List<UserInfoVo>>() {
                            @Override
                            public List<UserInfoVo> call(List<UserInfo> list) {
                                return UserInfoVo.getUserInfoVo(list);
                            }
                        })
                        .map(new Func1<List<UserInfoVo>, ArrayList<PinnedSectionItem<UserInfoVo>>>() {
                            @Override
                            public ArrayList<PinnedSectionItem<UserInfoVo>> call(List<UserInfoVo> userInfoVos) {
                                return PinnedSectionItemUtil.parseUserVO(userInfoVos);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<PinnedSectionItem<UserInfoVo>>>() {
                            @Override
                            public void call(ArrayList<PinnedSectionItem<UserInfoVo>> pinnedList) {
                                setDataToUI(pinnedList);
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        });
    }

    private void setDataToUI(final ArrayList<PinnedSectionItem<UserInfoVo>> pinnedList) {
        if (getActivity() == null) {
            return;
        }
        Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (mAdapter == null) {
                            mAdapter = new ContactsSortAdapter(getActivity(), pinnedList, false);
                            mLvContacts.setAdapter(mAdapter);
                        } else {
                            mAdapter.setData(pinnedList);
                        }
                        initListener();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mLvContacts.scrollTo(0, 0);
            return true;
        }
    }
}



