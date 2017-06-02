/**
 *
 */
package com.itutorgroup.tutorchat.phone.activity.group;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.GroupAnnouncementListAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.ui.SegmentControlView;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.xlistview.XListView;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * 公告栏历史
 *
 * @author tom_zxzhang
 */
public class GroupAnnouncementHistoryListActivity extends BaseActivity implements SegmentControlView.OnSegmentChangedListener {


    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;
    @InjectExtra(key = "GroupId")
    private String groupId;
    @InjectView(id = R.id.lv_group_announcement)
    private XListView listview;
    @InjectView(id = R.id.segment_read_status)
    SegmentControlView mSegmentReadStatus;
    private GroupAnnouncementListAdapter adapter;
    private boolean mIsUnRead = true;
    private List<MessageModel> readList;
    private List<MessageModel> unReadList;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_announcement_list);
        initListener();
        initView();
    }


    private void initListener() {
        listview.setPullRefreshEnable(false);
        listview.setPullLoadEnable(false);
        listview.setOnItemClickListener(itemClickListener);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MessageModel item = unReadList.get(position - 1);
            item.GroupAnnouncementIsRead = 1;
            MessageManager.getInstance().setGroupAnnouncementIsReadFromReceiverID(item);
            readList.add(item);
            Collections.sort(readList, new Comparator<MessageModel>() {
                @Override
                public int compare(MessageModel o1, MessageModel o2) {
                    return (int) (o2.getCreateTime() - o1.getCreateTime());
                }
            });
            unReadList.remove(position - 1);
            setTabText(unReadList.size(), readList.size());
            adapter.notifyDataSetChanged();
        }
    };

    private void initView() {
        mHeaderLayout.title(getResources().getString(R.string.group_notice)).autoCancel(this);
        mSegmentReadStatus.setOnSegmentChangedListener(this);
        getDataByPage();
    }


    private void setTabText(int unread, int read) {
        String[] texts = new String[]{
                getString(R.string.message_recipients_state_unread, unread),
                getString(R.string.message_recipients_state_read, read),
        };
        mSegmentReadStatus.setTexts(texts);
    }


    private void getDataByPage() {
        new AsyncTask<Void, Void, List<MessageModel>>() {
            @Override
            protected List<MessageModel> doInBackground(Void... params) {
                List<MessageModel> list = null;
                try {
                    readList = MessageDao.getInstance().queryGroupAnnoucementListByPage(AccountManager.getInstance().getCurrentUserId(), groupId, true);
                    unReadList = MessageDao.getInstance().queryGroupAnnoucementListByPage(AccountManager.getInstance().getCurrentUserId(), groupId, false);
                    list = mIsUnRead ? unReadList : readList;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<MessageModel> list) {
                listview.stopRefresh(true);
                if (list.size() < Constant.PAGE_NUMBER) {
                    listview.setPullRefreshEnable(false);
                } else {
                    listview.setPullRefreshEnable(true);
                }
                setTabText(unReadList.size(), readList.size());
                updateUI();
            }
        }.execute();
    }


    @Override
    public void onSegmentChanged(int newSelectedIndex) {
        mIsUnRead = newSelectedIndex == 0;
        if (mIsUnRead) {
            listview.setOnItemClickListener(itemClickListener);
        } else {
            listview.setOnItemClickListener(null);
        }
        updateUI();
    }


    private void updateUI() {
        List<MessageModel> messageModelList = mIsUnRead ? unReadList : readList;
        if (adapter == null) {
            adapter = new GroupAnnouncementListAdapter(mContext, messageModelList);
            listview.setAdapter(adapter);
            ConversationManager.getInstance().updateMessageConversation(groupId, groupId, messageModelList);
        }
        adapter.setData(messageModelList);
    }


}
