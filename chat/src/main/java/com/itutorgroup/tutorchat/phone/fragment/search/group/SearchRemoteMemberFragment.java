package com.itutorgroup.tutorchat.phone.fragment.search.group;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;

import com.itutorgroup.tutorchat.phone.domain.response.SearchUserByKeyResponse;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

/**
 * Created by joyinzhao on 2016/9/13.
 */
public class SearchRemoteMemberFragment extends SearchMemberFragment {

    public SearchRemoteMemberFragment(Context context) {
        super(context);
        setBackgroundColor(Color.RED);
    }

    public SearchRemoteMemberFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchRemoteMemberFragment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void doSearch(String text) {
        ContactsManager contactsManager = ContactsManager.getInstance();
        contactsManager.searchUserByKey(text, new RequestHandler.RequestListener<SearchUserByKeyResponse>() {
            @Override
            public void onResponse(SearchUserByKeyResponse response, Bundle bundle) {
                parseData(response.UserList);
            }
        });
    }
}
