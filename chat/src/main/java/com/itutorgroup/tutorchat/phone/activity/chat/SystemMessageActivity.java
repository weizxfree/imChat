package com.itutorgroup.tutorchat.phone.activity.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.chat.SystemMessageListAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.SystemNoticeManager;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class SystemMessageActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.lv_message)
    ListView mLvMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_message);
        initView();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_system_message))
                .autoCancel(this);
        View header = new View(this);
        header.setMinimumHeight(PixelUtil.dp2px(10));
        View footer = new View(this);
        footer.setMinimumHeight(PixelUtil.dp2px(10));
        mLvMessage.addHeaderView(header);
        mLvMessage.addFooterView(footer);

        SystemNoticeManager.getInstance().getSystemNoticeHistory(new CommonLoadingListener<List<SystemNoticeModel>>() {
            @Override
            public void onResponse(List<SystemNoticeModel> list) {
                SystemMessageListAdapter adapter = new SystemMessageListAdapter(SystemMessageActivity.this, list);
                mLvMessage.setAdapter(adapter);
            }
        });
    }
}
