package com.itutorgroup.tutorchat.phone.fragment.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.search.SearchActivity;
import com.itutorgroup.tutorchat.phone.adapter.search.SearchOnlineContactsAdapter;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.response.SearchUserByKeyResponse;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.EnableStateUtil;

import java.util.List;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class SearchNetworkFragment extends SearchFragment {

    @InjectView(id = R.id.tv_tip_user_not_exists)
    TextView mTvUserNotExists;

    @InjectView(id = R.id.tv_search_by_key)
    TextView mTvSearchByKey;

    @InjectView(id = R.id.divider_search_result)
    View mTvDivider;

    @InjectView(id = R.id.ll_search_key)
    LinearLayout mLLSearchKey;

    @InjectView(id = R.id.lv_search_result)
    ListView mLvResult;

    private View mFooterView;

    private SearchOnlineContactsAdapter mAdapter;

    public static SearchNetworkFragment newInstance() {
        SearchNetworkFragment newFragment = new SearchNetworkFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public void setSearchMode(int mode) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result_network, null);
        Injector.injectInto(this, view);
        initView();
        return view;
    }

    @Override
    public void initView() {
        refreshSearchUI(null);
    }

    @Override
    public void search(final String text) {
        mSearchKey = text;
        ContactsManager contactsManager = ContactsManager.getInstance();
        contactsManager.searchUserByKey(text, new RequestHandler.RequestListener<SearchUserByKeyResponse>() {
            @Override
            public void onResponse(SearchUserByKeyResponse response, Bundle bundle) {
                updateDataToUI(text, response.UserList);
            }
        });
        refreshSearchUI(text);
    }

    private void refreshSearchUI(String text) {
        if (TextUtils.isEmpty(text)) {
            EnableStateUtil.setViewVisible(mLLSearchKey, View.GONE);
            EnableStateUtil.setViewVisible(mTvDivider, View.GONE);
        } else {
            String tmp = getString(R.string.search_by_key, text);
            SpannableString ss = new SpannableString(tmp);
            ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bg_actionbar)), ss.length() - text.length(), ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvSearchByKey.setText(ss);
            EnableStateUtil.setViewVisible(mLLSearchKey, View.VISIBLE);
            EnableStateUtil.setViewVisible(mTvDivider, View.VISIBLE);
        }
    }

    public void updateDataToUI(String key, List<UserInfo> userList) {
        if (!TextUtils.isEmpty(key) && !key.equals(mSearchKey)) {
            if (userList != null) {
                return;
            }
        }
        if (mAdapter == null) {
            mAdapter = new SearchOnlineContactsAdapter(getActivity(), key, userList);
            mLvResult.setAdapter(mAdapter);
        } else {
            mAdapter.setFilterKey(key);
            mAdapter.replaceAll(userList);
            mLvResult.setSelection(0);
        }

        if (userList == null || userList.size() == 0) {
            EnableStateUtil.setViewVisible(mTvUserNotExists, View.VISIBLE);
            EnableStateUtil.setViewVisible(mTvDivider, View.GONE);
        } else {
            EnableStateUtil.setViewVisible(mTvUserNotExists, View.GONE);
        }

        if (userList != null && userList.size() >= 10) {
            if (mFooterView == null) {
                mFooterView = LayoutInflater.from(mContext).inflate(R.layout.footer_search_more_result, null);
            }
            if (mLvResult.getFooterViewsCount() == 0) {
                try {
                    mLvResult.addFooterView(mFooterView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (mFooterView != null) {
                try {
                    mLvResult.removeFooterView(mFooterView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void showEmptyView() {
        refreshSearchUI(null);
        updateDataToUI(mSearchKey, null);
        EnableStateUtil.setViewVisible(mTvDivider, View.GONE);
        EnableStateUtil.setViewVisible(mTvUserNotExists, View.GONE);
    }
}
