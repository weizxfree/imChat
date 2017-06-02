package com.itutorgroup.tutorchat.phone.adapter.chat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.group.GroupAnnouncementActivity;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;

import java.util.List;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class SystemMessageListAdapter extends MyBaseAdapter<SystemNoticeModel> {

    public SystemMessageListAdapter(Context context, List<SystemNoticeModel> data) {
        super(context, data);
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_system_message;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        bindData(convertView, position, holder);
        return convertView;
    }

    private void bindData(View convertView, int position, ViewHolder holder) {
        TextView tvContent = holder.getView(R.id.tv_content);
        tvContent.setText(getItem(position).MsgBody);
        TextView tvTime = holder.getView(R.id.tv_message_time);
        tvTime.setText(TimeUtils.DETAIL_DATE_FORMAT.format(getItem(position).LastModifiedTime));
    }
}
