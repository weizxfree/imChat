package com.itutorgroup.tutorchat.phone.adapter.group;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;




public class GroupMemberSelectAdapter extends MyBaseAdapter {

    private String mSearchKey;

    public static final int RESULT_OK = 0x11;

    public GroupMemberSelectAdapter(Context context, List data, String key) {
        super(context, data);
        mSearchKey = key;
    }


    public void setFilterKey(String key) {
        mSearchKey = key;
    }


    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_message_recipients;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        setData(position, convertView, holder);
        return convertView;
    }

    @Override
    public UserInfo getItem(int position) {
        return (UserInfo)super.getItem(position);
    }


    private void setData(int position, View convertView, ViewHolder holder) {
        CircleImageView imvHeader = holder.getView(R.id.imv_header);
        TextView tvName = holder.getView(R.id.tv_name);
        TextView tvChineseName = holder.getView(R.id.tv_chinese_name);
        TextView tvDepartment = holder.getView(R.id.tv_department);
        imvHeader.setTag(R.id.tag_default, R.drawable.head_personal_blue);
        UserInfo userInfo = (UserInfo) convertView.getTag(R.id.tag_bean);
        UserInfoHelper.showAvatar(userInfo, imvHeader);
        SpannableString name = new SpannableString(userInfo.Name);
        SpannableString chineseName = new SpannableString(userInfo.ChineseName);
        if (!TextUtils.isEmpty(mSearchKey)) {
            name = SearchUtil.formatSearchStr(mSearchKey, userInfo.Name);
            chineseName = SearchUtil.formatSearchStr(mSearchKey, userInfo.ChineseName);
        }
        tvName.setText(name);
        tvChineseName.setText(chineseName);
        tvDepartment.setText(mContext.getString(R.string.personal_department_read_status, userInfo.DepartmentGroup, userInfo.Department, userInfo.Title));
        convertView.setOnClickListener(mOnItemClickListener);
    }


    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                UserInfo userInfo = (UserInfo) v.getTag(R.id.tag_bean);
                Intent intent = new Intent();
                intent.putExtra("user_info", userInfo);
                ((Activity) mContext).setResult(RESULT_OK, intent);
                ((Activity) mContext).finish();
        }
    };
}
