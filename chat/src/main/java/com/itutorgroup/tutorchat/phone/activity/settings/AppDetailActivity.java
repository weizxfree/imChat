package com.itutorgroup.tutorchat.phone.activity.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.settings.AppDetailAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/29.
 */
public class AppDetailActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.lv_content)
    ListView mListView;

    public static final String EXTRA_USER = "user";
    public static final String EXTRA_GROUP = "group";
    public static final String EXTRA_TYPE = "type";

    public static final int TYPE_APP_INFO = 0x10;
    public static final int TYPE_USER_INFO = 0x11;
    public static final int TYPE_GROUP_INFO = 0x12;

    @InjectExtra(key = "type")
    int mType;

    @InjectExtra(key = "user")
    UserInfo mUser;

    @InjectExtra(key = "group")
    GroupInfo mGroup;

    private String mLastBugInfo;

    private View.OnClickListener mOnBugClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppDetailActivity.this, R.style.MyAlertDialogStyle);
            builder.setMessage(mLastBugInfo)
                    .setPositiveButton(R.string.confirm, null)
                    .setNegativeButton(R.string.msg_operation_delete, mDeleteBugListener)
                    .setNeutralButton(R.string.msg_operation_copy, mCopyBugListener).show();
        }
    };

    private DialogInterface.OnClickListener mCopyBugListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (!TextUtils.isEmpty(mLastBugInfo)) {
                AppUtils.Paste2Clipboard(mLastBugInfo);
                ToastUtil.show(R.string.copy_clipboard_done);
            }
        }
    };

    private DialogInterface.OnClickListener mDeleteBugListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AppPrefs.get(LPApp.getInstance()).remove("app_crash");
            mHeaderLayout.mLayoutRightContainer.removeAllViews();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        initView();
    }

    private void initView() {
        int title = R.string.title_app_detail;
        if (mType != TYPE_APP_INFO) {
            title = R.string.title_chat_desc;
        }
        mHeaderLayout.title(getString(title)).autoCancel(this);
        loadData();
        if (mType == TYPE_APP_INFO) {
            AppPrefs prefs = AppPrefs.get(LPApp.getInstance());
            mLastBugInfo = prefs.getString("app_crash", null);
            if (!TextUtils.isEmpty(mLastBugInfo)) {
                mHeaderLayout.rightText("Bug", mOnBugClickListener);
            }
        }
    }

    private void loadData() {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<AppDetailActivity, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(AppDetailActivity activity) {
                        switch (mType) {
                            case TYPE_APP_INFO:
                                return AppUtils.getDeviceInfoMap(activity);
                            case TYPE_USER_INFO:
                                return UserInfoManager.getInstance().getUserInfoMap(mUser);
                            case TYPE_GROUP_INFO:
                                return GroupManager.getInstance().getGroupInfoMap(mGroup);
                        }
                        return null;
                    }
                })
                .filter(new Func1<Map<String, String>, Boolean>() {
                    @Override
                    public Boolean call(Map<String, String> map) {
                        return map != null && map.size() > 0;
                    }
                })
                .map(new Func1<Map<String, String>, ArrayList<Map.Entry<String, String>>>() {
                    @Override
                    public ArrayList<Map.Entry<String, String>> call(Map<String, String> map) {
                        return new ArrayList<>(map.entrySet());
                    }
                })
                .map(new Func1<ArrayList<Map.Entry<String, String>>, List<Map.Entry<String, String>>>() {
                    @Override
                    public List<Map.Entry<String, String>> call(ArrayList<Map.Entry<String, String>> entries) {
                        Collections.sort(entries, new Comparator<Map.Entry<String, String>>() {
                            @Override
                            public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
                                return lhs.getKey().compareTo(rhs.getKey());
                            }
                        });
                        return entries;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Map.Entry<String, String>>>() {
                               @Override
                               public void call(List<Map.Entry<String, String>> entries) {
                                   AppDetailAdapter adapter = new AppDetailAdapter(AppDetailActivity.this, entries);
                                   mListView.setAdapter(adapter);
                               }
                           }
                        , CommonUtil.ACTION_EXCEPTION);
    }
}
