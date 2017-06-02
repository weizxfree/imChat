package com.itutorgroup.tutorchat.phone.adapter.service;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;

import java.util.List;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class ServiceAccountListAdapter extends MyBaseAdapter<ServiceAccountModel> {

    private String mSearchKey;

    public ServiceAccountListAdapter(Context context, List<ServiceAccountModel> data) {
        super(context, data);
    }

    public void setFilterKey(String key) {
        mSearchKey = key;
    }

    public void setData(List<ServiceAccountModel> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_service_account;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        convertView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(55)));
        bindData(position, convertView, holder);
        return convertView;
    }

    private void bindData(int position, View convertView, ViewHolder holder) {
        AvatarView avatarView = holder.getView(R.id.avatar_view);
        avatarView.setUserHead(getItem(position).ImageUrl, R.drawable.chat_service_number);

        TextView tvName = holder.getView(R.id.tv_name);
        tvName.setText(SearchUtil.formatSearchStr(mSearchKey, getItem(position).Name));

        TextView tvDescription = holder.getView(R.id.tv_description);
        tvDescription.setText(getItem(position).Description);
    }
}
