package com.itutorgroup.tutorchat.phone.activity.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.settings.AppDetailActivity;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.fragment.search.SearchFragment;
import com.itutorgroup.tutorchat.phone.fragment.search.SearchLocalFragment;
import com.itutorgroup.tutorchat.phone.fragment.search.SearchNetworkFragment;
import com.itutorgroup.tutorchat.phone.ui.common.edittext.ClearEditText;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;
import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class SearchActivity extends BaseActivity {

//    @InjectView(id = R.id.group_search_empty)
//    View mDefaultView;

    @InjectView(id = R.id.edt_search)
    ClearEditText mEdtSearch;

    @InjectView(id = R.id.ll_group_search_item)
    LinearLayout mGroupSearchItem;

    private SearchFragment mSearchFragment;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    public static final int SEARCH_SOURCE_LOCAL = 0x21;
    public static final int SEARCH_SOURCE_NETWORK = 0x22;

    public static final int SEARCH_MODE_NORMAL = 0x31;
    public static final int SEARCH_MODE_SELECT = 0x32;

    public static final String EXTRA_SEARCH_TYPE = "search_type";
    public static final String EXTRA_SEARCH_SOURCE = "search_source";
    public static final String EXTRA_SEARCH_KEY = "search_key";

    public static final String EXTRA_MODE = "search_mode";

    private int mSearchType;
    private int mSearchSource;
    private String mDefaultKey;
    private int mSearchMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        mSearchType = intent.getIntExtra(EXTRA_SEARCH_TYPE, SearchManager.SEARCH_TYPE_ALL);
//        if (mSearchType != SearchManager.SEARCH_TYPE_ALL) {
//            mDefaultView.setVisibility(View.GONE);
//        }
        mSearchSource = intent.getIntExtra(EXTRA_SEARCH_SOURCE, SEARCH_SOURCE_LOCAL);
        mGroupSearchItem.setVisibility(mSearchSource == SEARCH_SOURCE_LOCAL ? View.VISIBLE : View.GONE);
        mDefaultKey = intent.getStringExtra(EXTRA_SEARCH_KEY);
        mSearchMode = intent.getIntExtra(EXTRA_MODE, SEARCH_MODE_NORMAL);
    }

    private void initView() {
        if (mSearchSource == SEARCH_SOURCE_NETWORK && mSearchType == SearchManager.SEARCH_TYPE_CONTACTS) {
            mSearchFragment = SearchNetworkFragment.newInstance();
            mSearchFragment.setSearchMode(mSearchMode);
        } else {
            mSearchFragment = SearchLocalFragment.getInstance();
            mSearchFragment.setSearchType(mSearchType);
        }
        initFragment(false);
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
                initFragment(!isEmpty);

                mHandler.removeMessages(MSG_SEARCH_CONTENT);

                if (isEmpty) {
                    mSearchFragment.showEmptyView();
                } else {
                    Message msg = mHandler.obtainMessage(MSG_SEARCH_CONTENT, text);
                    mHandler.sendMessageDelayed(msg, mSearchSource == SEARCH_SOURCE_LOCAL ? 10 : 1000);
                    checkMagicCode(text);
                }
            }
        });

        if (mSearchSource == SEARCH_SOURCE_LOCAL && !TextUtils.isEmpty(mDefaultKey)) {
            mEdtSearch.setText(mDefaultKey);
            mEdtSearch.setSelection(mDefaultKey.length());
        }
    }

    private void checkMagicCode(String text) {
        String appVersion = AppUtils.getVersionName(SearchActivity.this);
        if (!TextUtils.isEmpty(text) && text.equals("#*" + appVersion + "*#")) {
            mHandler.removeMessages(MSG_SEARCH_CONTENT);
            Intent intent = new Intent(SearchActivity.this, AppDetailActivity.class);
            intent.putExtra(AppDetailActivity.EXTRA_TYPE, AppDetailActivity.TYPE_APP_INFO);
            startActivity(intent);
            finish();
        }
    }

    private void initFragment(boolean show) {
        FragmentTransaction trx = getSupportFragmentManager()
                .beginTransaction();
        if (!mSearchFragment.isAdded()) {
            trx.replace(R.id.frame_content, mSearchFragment);
        }
        if (show) {
            trx.show(mSearchFragment).commit();
        } else {
            trx.hide(mSearchFragment).commit();
        }
    }

    @OnClick(id = R.id.tv_title_cancel)
    void onCancelClick() {
        InputMethodUtil.hideInputMethod(SearchActivity.this);
        finish();
    }
/*
    @OnClick(id = {R.id.tv_search_contacts, R.id.tv_search_group, R.id.tv_search_chat_history})
    void onSearchItemClick(View v) {
        mSearchType = SearchManager.SEARCH_TYPE_ALL;
        switch (v.getId()) {
            case R.id.tv_search_contacts:
                mSearchType = SearchManager.SEARCH_TYPE_CONTACTS;
                break;
            case R.id.tv_search_group:
                mSearchType = SearchManager.SEARCH_TYPE_GROUP;
                break;
            case R.id.tv_search_chat_history:
                mSearchType = SearchManager.SEARCH_TYPE_CHAT_MESSAGE;
                break;
        }
        mSearchFragment.setSearchType(mSearchType);
        mDefaultView.setVisibility(View.GONE);
    }
*/

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEARCH_CONTENT:
                performSearch(msg);
                break;
        }
    }

    private void performSearch(Message msg) {
        if (msg != null && !StringUtils.isBlank(msg.obj)) {
            String text = (String) msg.obj;
            if (text.equals(mEdtSearch.getText().toString())) {
                mSearchFragment.search(text);
            }
        } else {
            mSearchFragment.showEmptyView();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodUtil.hideInputMethod(SearchActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
