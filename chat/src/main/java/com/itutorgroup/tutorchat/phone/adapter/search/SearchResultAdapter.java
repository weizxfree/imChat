package com.itutorgroup.tutorchat.phone.adapter.search;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ChatDetailActivity;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupDetailActivity;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.SearchResultGroup;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;

import java.util.List;

/**
 * Created by joyinzhao on 2016/9/6.
 */
public class SearchResultAdapter extends MyBaseAdapter {

    private int mType;
    private String mKey;
    private boolean mCompose;

    public SearchResultAdapter(Context context, int type, List data, String key, boolean compose) {
        super(context, data);
        mType = type;
        mKey = key;
        mCompose = compose;
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_search_local_item;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        SearchResultGroup.ViewHolder searchHolder = new SearchResultGroup.ViewHolder(mContext, convertView, mType, mKey, mData);
        searchHolder.mComposeMode = mCompose;
        searchHolder.setPosition(position);
        convertView.setTag(R.id.tag_holder, holder);
        convertView.setTag(R.id.tag_position, position);
        return convertView;
    }
}
