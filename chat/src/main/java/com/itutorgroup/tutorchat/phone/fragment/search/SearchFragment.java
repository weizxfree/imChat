package com.itutorgroup.tutorchat.phone.fragment.search;

import com.itutorgroup.tutorchat.phone.app.BaseFragment;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public abstract class SearchFragment extends BaseFragment {

    public String mSearchKey;

    public void setSearchType(int type) {

    }

    public void setSearchMode(int mode) {

    }

    public abstract void initView();

    public abstract void search(String text);

    public abstract void showEmptyView();

}
