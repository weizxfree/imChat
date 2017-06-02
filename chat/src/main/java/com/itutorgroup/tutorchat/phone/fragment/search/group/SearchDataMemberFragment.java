package com.itutorgroup.tutorchat.phone.fragment.search.group;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/13.
 */
public class SearchDataMemberFragment extends SearchMemberFragment {

    private List<UserInfo> mData;

    public SearchDataMemberFragment(Context context) {
        super(context);
        setBackgroundColor(Color.BLUE);
    }

    public SearchDataMemberFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchDataMemberFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(List<UserInfo> data) {
        mData = data;
        parseData(data);
    }

    @Override
    public void doSearch(final String text) {
        Observable.just(mData)
                .subscribeOn(Schedulers.io())
                .filter(new Func1<List<UserInfo>, Boolean>() {
                    @Override
                    public Boolean call(List<UserInfo> userInfos) {
                        return userInfos != null && userInfos.size() != 0;
                    }
                })
                .map(new Func1<List<UserInfo>, List<UserInfo>>() {
                    @Override
                    public List<UserInfo> call(List<UserInfo> userInfos) {
                        List<UserInfo> list = new ArrayList<>();
                        for (UserInfo user : userInfos) {
                            if (TextUtils.isEmpty(text)) {
                                list.add(user);
                            } else {
                                if (user.Name.toLowerCase().contains(text.toLowerCase())
                                        || user.ChineseName.toLowerCase().contains(text.toLowerCase())) {
                                    list.add(user);
                                }
                            }
                        }
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<UserInfo>>() {
                    @Override
                    public void call(List<UserInfo> list) {
                        parseData(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    @Override
    public void showEmptyView() {
        search("");
    }

    public void refresh() {
        setData(mData);
    }
}
