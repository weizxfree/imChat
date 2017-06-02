package com.itutorgroup.tutorchat.phone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;

public class GroupAnnouncementListAdapter extends SAFAdapter<MessageModel> {



    private LayoutInflater mInflater;
    private Context mContext;

    private Map<String, UserInfo> userInfoMap;

    public GroupAnnouncementListAdapter(Context context, List<MessageModel> list){
        this.mInflater = LayoutInflater.from(context);
        userInfoMap = new HashMap<>();
        mContext = context;
        mList = list;
    }

    public void addMsgListToTop(List<MessageModel> listMessages) {
        if (Preconditions.isBlank(listMessages))
            return;
        mList.addAll(0, listMessages);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final MessageModel item = (MessageModel) getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_group_annoucement, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(position == 0 ){
            holder.mIndex.setBackgroundResource(R.color.bg_actionbar);
        }else{
            holder.mIndex.setBackgroundResource(R.color.color_list_item_pressed);
        }
        UserInfo userInfo = userInfoMap.get(item.PosterID);
        if (StringUtils.isBlank(userInfo)) {
            final ViewHolder tmpHolder = holder;
            UserInfoManager.getInstance().getUserInfo(item.PosterID, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(final UserInfo userInfoResponse) {
                    if (StringUtils.isNotBlank(userInfoResponse)) {
                        userInfoMap.put(item.PosterID, userInfoResponse);
                    }
                    setUserInfo2Ui(tmpHolder, userInfoResponse);
                }
            });
        } else {
            setUserInfo2Ui(holder, userInfo);
        }
        if(item.GroupAnnouncementIsRead == 1){
            holder.mTvReadStatus.setText(LPApp.getInstance().getString(R.string.msg_status_is_read));
            holder.mTvReadStatus.setTextColor(mContext.getResources().getColor(R.color.text_color_time_group_announcement));
        }else{
            holder.mTvReadStatus.setText(LPApp.getInstance().getString(R.string.msg_status_is_not_read));
            holder.mTvReadStatus.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        holder.mTvContent.setText(item.Content);
        holder.mMessageTime.setText(TimeUtils.getTime(item.CreateTime,TimeUtils.DETAIL_DATE_FORMAT));


        return convertView;
    }

    class ViewHolder {


        @InjectView(id = R.id.tv_index)
        TextView mIndex;
        @InjectView(id = R.id.avatar_view)
        ImageView mAvatar;
        @InjectView(id = R.id.tv_name)
        TextView mTvName;
        @InjectView(id = R.id.tv_department)
        TextView mTvDepartment;
        @InjectView(id = R.id.tv_read_status)
        TextView mTvReadStatus;
        @InjectView(id = R.id.tv_content)
        TextView mTvContent;
        @InjectView(id = R.id.tv_message_time)
        TextView mMessageTime;


        public ViewHolder(View view) {
            Injector.injectInto(this, view);
        }
    }

    private void setUserInfo2Ui(ViewHolder viewHolder,  UserInfo userInfo) {
        viewHolder.mTvName.setText(userInfo.Name);
        viewHolder.mTvDepartment.setText(userInfo.Title);
        viewHolder.mAvatar.setTag(R.id.tag_default, R.drawable.head_personal_square);
        UserInfoHelper.showAvatar(userInfo.Image, viewHolder.mAvatar);
    }
}
