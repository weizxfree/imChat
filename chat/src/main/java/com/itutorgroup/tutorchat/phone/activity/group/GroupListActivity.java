/**
 *
 */
package com.itutorgroup.tutorchat.phone.activity.group;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.group.GroupListAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ContactsEvent;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.ClearEditText;
import com.itutorgroup.tutorchat.phone.ui.popup.HomePopWindow;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import java.util.ArrayList;
import java.util.Collections;
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
 * 群列表页面
 *
 * @author tom_zxzhang
 */
public class GroupListActivity extends BaseActivity {

    @InjectView(id = R.id.listView)
    private ListView mListView;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.edt_search)
    ClearEditText mEdtSearch;

    @InjectView(id = R.id.view_empty)
    View mEmptyView;

    GroupListAdapter mAdapter;

    private HomePopWindow mPopupWindow;

    private String mFilterKey;
    private List<GroupInfo> mData;

    private View mMaskView;

    @InjectExtra(key = "mode_select")
    boolean mIsSelectMode;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        initView();
    }

    private void showPopupWindow(View view) {
        if (mPopupWindow == null) {
            mPopupWindow = new HomePopWindow(GroupListActivity.this);
            mPopupWindow.hideSearchContacts();
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (mMaskView != null) {
                        mMaskView.setVisibility(View.GONE);
                    }
                }
            });
        }
        mPopupWindow.showAsDropDown(view, -PixelUtil.dp2px(67), -PixelUtil.dp2px(10));
        if (mMaskView == null) {
            mMaskView = new View(mContext);
            mMaskView.setBackgroundColor(Color.parseColor("#88000000"));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            getWindow().addContentView(mMaskView, lp);
        }
        mMaskView.setVisibility(View.VISIBLE);
        InputMethodUtil.hideInputMethod(GroupListActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.group_chat)).autoCancel(this);
        initData();

        if (!mIsSelectMode && UserSettingManager.getInstance().isHaveCreateGroupRight()) {
            mHeaderLayout.rightImage(R.drawable.add_nav, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupWindow(mHeaderLayout.mLayoutRightContainer);
                }
            });
        }

        initSearchEditText();
        mListView.setEmptyView(mEmptyView);
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
        mEdtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_SEARCH:
                        InputMethodUtil.hideInputMethod(GroupListActivity.this);
                        break;
                }
                return false;
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

    private void initData() {
        GroupManager.getInstance().getGroupListInContactByGroupIds(new CommonLoadingListener<List<GroupInfo>>() {
            @Override
            public void onResponse(List<GroupInfo> groupInfos) {
                if (groupInfos != null && groupInfos.size() != 0) {
                    Collections.reverse(groupInfos);
                }
                mData = groupInfos;
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (mData == null || mData.size() == 0) {
            setData(mData);
            return;
        }
        Observable.just(mData)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<GroupInfo>, List<GroupInfo>>() {
                    @Override
                    public List<GroupInfo> call(List<GroupInfo> userInfos) {
                        List<GroupInfo> list = new ArrayList<>();
                        for (GroupInfo group : userInfos) {
                            if (TextUtils.isEmpty(mFilterKey)) {
                                list.add(group);
                            } else if ((!TextUtils.isEmpty(group.GroupName) && group.GroupName.toLowerCase().contains(mFilterKey.toLowerCase()))
                                    || checkDefaultName(mFilterKey.toLowerCase(), group)) {
                                list.add(group);
                            }
                        }
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<GroupInfo>>() {
                    @Override
                    public void call(List<GroupInfo> list) {
                        setData(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void setData(List<GroupInfo> list) {
        if (mAdapter == null) {
            mAdapter = new GroupListAdapter(mContext, list);
            mAdapter.setFilterKey(mFilterKey);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setFilterKey(mFilterKey);
            mAdapter.setData(list);
        }
    }

    private boolean checkDefaultName(String key, GroupInfo group) {
        if (TextUtils.isEmpty(group.GroupName) || "群聊".equals(group.GroupName)) {
            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + group.GroupID, "");
            return (!TextUtils.isEmpty(cacheName) && cacheName.toLowerCase().contains(key));
        }
        return false;
    }

    @Subscribe
    public void onContactsEvent(ContactsEvent event) {
        initData();
    }
}
