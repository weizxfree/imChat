package com.itutorgroup.tutorchat.phone.fragment.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.SearchResultGroup;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.List;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class SearchLocalFragment extends SearchFragment {

    private int mSearchType;

    @InjectView(id = R.id.result_contacts)
    SearchResultGroup mContacts;

    @InjectView(id = R.id.result_group)
    SearchResultGroup mGroup;

    @InjectView(id = R.id.result_messages)
    SearchResultGroup mMessage;

    @InjectView(id = R.id.tv_no_result)
    TextView mTvNoResult;

    public static SearchLocalFragment getInstance() {
        SearchLocalFragment newFragment = new SearchLocalFragment();
        Bundle bundle = new Bundle();
        newFragment.setArguments(bundle);
        return newFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result_local, null);
        Injector.injectInto(this, view);
        initView();
        return view;
    }

    @Override
    public void setSearchType(int type) {
        mSearchType = type;
    }

    @Override
    public void initView() {

    }

    @Override
    public void search(final String text) {
        mSearchKey = text;
        SearchManager.getInstance().search(mSearchType, text, new SearchManager.OnSearchListener() {
            @Override
            public void onResponse(String key, List[] listArray) {
                if (listArray == null || listArray.length != 3) {
                    showNoResult(true);
                    return;
                }
                initSearchResult(text, listArray[0], listArray[1], listArray[2]);
            }
        });
    }

    private void initSearchResult(String key, List<UserInfo> userList, List<SearchManager.SearchGroupBean> groupList, List<MessageModel> messageList) {
        showNoResult((
                userList == null || userList.size() == 0)
                && (groupList == null || groupList.size() == 0)
                && (messageList == null || messageList.size() == 0));

        if (!TextUtils.isEmpty(key) && key.equals(mSearchKey)) {
            dispatchSearchResult(mContacts, key, userList);
            dispatchSearchResult(mGroup, key, groupList);
            dispatchSearchResult(mMessage, key, messageList);
        }
    }

    private void showNoResult(boolean noResult) {
        mTvNoResult.setVisibility(noResult ? View.VISIBLE : View.GONE);
    }

    private void dispatchSearchResult(SearchResultGroup view, String key, List list) {
        if (list != null && list.size() != 0) {
            view.setData(key, list);
            view.setVisibility(View.VISIBLE);
        } else {
            view.clear();
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyView() {
        dispatchSearchResult(mContacts, null, null);
        dispatchSearchResult(mGroup, null, null);
        dispatchSearchResult(mMessage, null, null);
        showNoResult(false);
    }

}
