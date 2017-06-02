package com.itutorgroup.tutorchat.phone.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.settings.AppLocaleSettingAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/12/28.
 */
public abstract class AppLocaleSettingActivity extends BaseActivity {

    public HeaderLayout mHeaderLayout;
    public ListView mListView;
    private TextView mTvMenu;

    private int mDefaultCheckPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locale_settings);
        initView();
    }

    private void initView() {
        mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
        mListView = (ListView) findViewById(R.id.list_view);

        mHeaderLayout.title(getHeaderTitle()).autoCancel(this);
        mTvMenu = mHeaderLayout.addRightText(getString(R.string.done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = mListView.getCheckedItemPosition();
                Observable.just(index)
                        .observeOn(Schedulers.io())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer index) {
                                configAppSettings(index);
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        });
        mTvMenu.setEnabled(false);

        mDefaultCheckPosition = getCheckedItem();
        List<String> localeList = Arrays.asList(getResources().getStringArray(getArrayResId()));
        AppLocaleSettingAdapter adapter = new AppLocaleSettingAdapter(this, localeList);
        mListView.setAdapter(adapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setItemChecked(mDefaultCheckPosition, true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTvMenu.setEnabled(position != mDefaultCheckPosition);
            }
        });
    }

    public abstract String getHeaderTitle();

    public abstract int getCheckedItem();

    public abstract int getArrayResId();

    public abstract void configAppSettings(int index);
}
