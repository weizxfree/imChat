package com.itutorgroup.tutorchat.phone.activity.chat;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.settings.ConversationTopSortAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.request.v2.SetChatOrderRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.recycler.DividerItemDecoration;
import com.itutorgroup.tutorchat.phone.ui.recycler.SimpleItemTouchHelperCallback;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.TopChatManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/26.
 */
public class ConversationTopSortActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.recycler_conversation)
    RecyclerView mRecyclerView;

    private TextView mMenuSave;

    private ItemTouchHelper mItemTouchHelper;

    private ConversationTopSortAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_conversation);
        initView();
        loadData();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_top_chat_sort)).autoCancel(this);
        mMenuSave = mHeaderLayout.addRightText(getString(R.string.done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTopSort();
            }
        });
        mMenuSave.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    private void saveTopSort() {
        List<TopModel> list = mAdapter.getData();
        TopChatManager.getInstance().saveTopSort(list);

        TopChatManager.getInstance().requestSetChatOrder(ConversationTopSortActivity.this, list, new RequestHandler.RequestListener() {
            @Override
            public void onResponse(CommonResponse response, Bundle bundle) {
                finish();
            }
        });
    }

    private void loadData() {
        Observable.just(mRecyclerView)
                .subscribeOn(Schedulers.io())
                .map(new Func1<RecyclerView, List<TopModel>>() {
                    @Override
                    public List<TopModel> call(RecyclerView recyclerView) {
                        return TopChatManager.getInstance().getTopModelList();
                    }
                })
                .filter(new Func1<List<TopModel>, Boolean>() {
                    @Override
                    public Boolean call(List<TopModel> list) {
                        return list != null && list.size() > 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<TopModel>>() {
                    @Override
                    public void call(List<TopModel> list) {
                        mAdapter = new ConversationTopSortAdapter(ConversationTopSortActivity.this, list, mOnDragTouchListener);
                        mAdapter.setSortCallback(mSortListener);
                        mRecyclerView.setAdapter(mAdapter);
                        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
                        mItemTouchHelper = new ItemTouchHelper(callback);
                        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private View.OnTouchListener mOnDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ConversationTopSortAdapter.MyViewHolder holder = (ConversationTopSortAdapter.MyViewHolder) v.getTag(R.id.tag_holder);
            mItemTouchHelper.startDrag(holder);
            return true;
        }
    };

    private CommonLoadingListener mSortListener = new CommonLoadingListener() {
        @Override
        public void onResponse(Object o) {
            mMenuSave.setEnabled(true);
        }
    };
}
