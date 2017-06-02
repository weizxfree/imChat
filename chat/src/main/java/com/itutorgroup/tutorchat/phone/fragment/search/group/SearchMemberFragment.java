package com.itutorgroup.tutorchat.phone.fragment.search.group;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.ContactsSortAdapter;
import com.itutorgroup.tutorchat.phone.ui.common.SideBar;
import com.itutorgroup.tutorchat.phone.domain.beans.UserInfoVo;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.PinnedSectionItem;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.pinned.PinnedSectionItemUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/13.
 */
public abstract class SearchMemberFragment extends FrameLayout {

    ListView mLvContacts;
    View mEmptyView;
    SideBar mSideBar;
    TextView mTvSideLetter;

    private List<String> mGroupExistsMemberIdList;
    private List<String> mCurrentUserInfoList;

    private ContactsSortAdapter mAdapter;

    ContactsSortAdapter.CheckBoxListener mOnContactsChangeListener;

    private Handler mHandler = new Handler();

    private String mSearchKey;

    public SearchMemberFragment(Context context) {
        super(context);
        init(context);
    }

    public SearchMemberFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchMemberFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = createView(LayoutInflater.from(context));
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(view);

        initView();
    }

    public View createView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_select_pinned, null);
        mLvContacts = (ListView) view.findViewById(R.id.lv_contacts);
        mEmptyView = view.findViewById(R.id.tv_no_result);
        mSideBar = (SideBar) view.findViewById(R.id.side_bar);
        mTvSideLetter = (TextView) view.findViewById(R.id.tv_tip_current_letter);
        initView();
        return view;
    }

    public final void initView() {
        //设置右侧[A-Z]快速导航栏触摸监听
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置

                if (StringUtils.isNotBlank(mAdapter)) {
                    int position = mAdapter.getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        mLvContacts.setSelection(position);
                        mTvSideLetter.setText(s);
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
                                mTvSideLetter.setVisibility(View.INVISIBLE);
                            }
                        }, 1000);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mTvSideLetter.setVisibility(View.VISIBLE);
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
    }

    public void showEmptyView() {
        mLvContacts.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
    }

    public void showNoResult() {
        mLvContacts.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    public final void search(String text) {
        mSearchKey = text;
        doSearch(text);
    }

    public abstract void doSearch(String text);

    public void parseData(List<UserInfo> list) {
        if (list == null || list.size() == 0) {
            showNoResult();
            return;
        }
        Observable.just(list)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<UserInfo>, List<UserInfoVo>>() {
                    @Override
                    public List<UserInfoVo> call(List<UserInfo> infos) {
                        return UserInfoVo.getUserInfoVo(infos);
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
                    public void call(ArrayList<PinnedSectionItem<UserInfoVo>> pinnedSectionItems) {
                        updateDataToUI(pinnedSectionItems);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void updateDataToUI(ArrayList<PinnedSectionItem<UserInfoVo>> list) {
        if (mAdapter == null) {
            mAdapter = new ContactsSortAdapter(getContext(), list, mGroupExistsMemberIdList);
            mAdapter.setCheckBoxListener(mOnContactsChangeListener);
            mLvContacts.setAdapter(mAdapter);
        } else {
            mAdapter.setData(list);
        }
        mAdapter.setFilterKey(mSearchKey);
        mAdapter.setCurrentSelected(mCurrentUserInfoList);
        mAdapter.setDisableList(mGroupExistsMemberIdList);
        mLvContacts.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    public void setOnSelectedContactsChangeListener(ContactsSortAdapter.CheckBoxListener listener) {
        mOnContactsChangeListener = listener;
    }

    public void setDisableList(List<String> list) {
        mGroupExistsMemberIdList = list;
    }

    public void setDefaultCheckedList(List<String> list) {
        mCurrentUserInfoList = list;
    }

    public void clearAllChecked() {
        if (mAdapter != null) {
            mAdapter.clearAllChecked();
        }
    }

    public void refreshData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void removeSelectedItem(String id) {
        if (mAdapter != null) {
            mAdapter.removeSelectedId(id);
        }
    }
}
