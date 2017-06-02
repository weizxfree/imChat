package com.itutorgroup.tutorchat.phone.activity.search;

import android.os.Bundle;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.search.SearchResultAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

import java.util.List;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/9/6.
 */
public class SearchResultActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.lv_search_result)
    ListView mLvResult;

    @InjectExtra(key = "type")
    int mType;

    @InjectExtra(key = "data")
    List mData;

    @InjectExtra(key = "key")
    String mKey;

    @InjectExtra(key = "compose", defaultBoolean = true)
    boolean mCompose;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        initViews();
    }

    private void initViews() {
        mHeaderLayout.title(getString(R.string.search_result)).autoCancel(this);

        SearchResultAdapter adapter = new SearchResultAdapter(this, mType, mData, mKey, mCompose);
        mLvResult.setAdapter(adapter);
    }


}
