package com.itutorgroup.tutorchat.phone.adapter.group;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.Preconditions;

/**
 *
 */
public class GroupUserInfoAdapter extends SAFAdapter<UserInfo> {


    private LayoutInflater mInflater;
    private ViewHolder holder;
    private Context mContext;
    private List<UserInfo> allList;


    public GroupUserInfoAdapter(Context context, List<UserInfo> list) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        allList = list;
        mList = getGridViewList(list);
    }

    /**
     * 获取gridView前10个数据
     *
     * @param list
     * @return
     */
    private List<UserInfo> getGridViewList(List<UserInfo> list) {

        if (Preconditions.isBlank(list))
            return null;
        UserInfo userInfo = new UserInfo();
        userInfo.Name = "More";
        list.add(list.size(), userInfo);
        return list;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        UserInfo item = (UserInfo) getItem(i);
        if (view == null) {
            view = mInflater.inflate(R.layout.cell_gridview_imghead, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(item.Name);
        if (TextUtils.isEmpty(item.Image)) {
            holder.headImageView.setBorderWidth(0);
            holder.headImageView.setBorderColor(Color.TRANSPARENT);
        }
        UserInfoHelper.showAvatar(item, holder.headImageView);
        if (i == getCount() - 1) {
            view.setOnClickListener(mOnShowMoreMemberClickListener);
            holder.headImageView.setBorderWidth(0);
            holder.headImageView.setImageResource(R.drawable.ic_add_group);
        } else {
            view.setTag(R.id.tag_bean, item);
            view.setOnClickListener(mOnItemClickListener);
        }
        return view;
    }

    private View.OnClickListener mOnShowMoreMemberClickListener;

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInfo info = (UserInfo) v.getTag(R.id.tag_bean);
            mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", info.UserID));
        }
    };

    public void setOnMoreGroupMemberClickListener(View.OnClickListener listener) {
        mOnShowMoreMemberClickListener = listener;
    }

    class ViewHolder {
        @InjectView
        CircleImageView headImageView;
        @InjectView
        TextView name;

        public ViewHolder(View view) {
            Injector.injectInto(this, view);
            headImageView.setTag(R.id.tag_default, R.drawable.head_personal_white);
        }
    }
}
