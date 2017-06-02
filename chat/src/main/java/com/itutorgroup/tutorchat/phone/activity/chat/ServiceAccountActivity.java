package com.itutorgroup.tutorchat.phone.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.service.ServiceAccountListAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;
import com.itutorgroup.tutorchat.phone.domain.event.ServiceAccountListEvent;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.ServiceAccountManager;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.eventbus.Subscribe;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class ServiceAccountActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.list_view)
    ListView mListView;

    @InjectView(id = R.id.edt_search)
    EditText mEdtSearch;

    @InjectView(id = R.id.view_empty)
    View mEmptyView;

    private String mFilterKey;
    private List<ServiceAccountModel> mData;
    private ServiceAccountListAdapter mAdapter;

    private static final int MSG_SEARCH_CONTENT = 0x11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_account);
        initView();
        loadData();
        initSearchEditText();
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
                        InputMethodUtil.hideInputMethod(ServiceAccountActivity.this);
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

    private void updateUI() {
        if (mData == null || mData.size() == 0) {
            setData(mData);
        } else {
            Observable.just(mData)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<List<ServiceAccountModel>, List<ServiceAccountModel>>() {
                        @Override
                        public List<ServiceAccountModel> call
                                (List<ServiceAccountModel> serviceAccountModels) {
                            if (TextUtils.isEmpty(mFilterKey)) {
                                return serviceAccountModels;
                            }
                            List<ServiceAccountModel> list = new ArrayList<>();
                            for (ServiceAccountModel model : serviceAccountModels) {
                                if (model != null && !TextUtils.isEmpty(model.Name) && model.Name.toLowerCase().contains(mFilterKey.toLowerCase())) {
                                    list.add(model);
                                }
                            }
                            return list;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<ServiceAccountModel>>() {
                        @Override
                        public void call(List<ServiceAccountModel> list) {
                            setData(list);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    private void setData(List<ServiceAccountModel> list) {
        if (mAdapter == null) {
            mAdapter = new ServiceAccountListAdapter(ServiceAccountActivity.this, list);
            mAdapter.setFilterKey(mFilterKey);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(ServiceAccountActivity.this);
        } else {
            mAdapter.setFilterKey(mFilterKey);
            mAdapter.setData(list);
        }
    }

    private void loadData() {
        mData = ServiceAccountManager.getInstance().getServiceAccountList();
        updateUI();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.service_number)).autoCancel(this);
        ServiceAccountManager.getInstance().requestServiceAccountList();
        mListView.setEmptyView(mEmptyView);
    }

    @Subscribe
    public void onServiceAccountListEvent(ServiceAccountListEvent event) {
        loadData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter != null && mAdapter.getCount() > position) {
            ServiceAccountModel model = mAdapter.getItem(position);
            if (model != null) {
                String serviceAccountId = model.ServiceAccountId;
                Intent intent = new Intent(ServiceAccountActivity.this, SingleChatActivity.class);
                intent.putExtra("service_account_id", serviceAccountId);
                startActivity(intent);
            }
        }
    }
}
