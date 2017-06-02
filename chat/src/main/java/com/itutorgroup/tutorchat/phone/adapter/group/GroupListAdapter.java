package com.itutorgroup.tutorchat.phone.adapter.group;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;

import java.util.List;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by Administrator on 2016/5/12 0012.
 */
public class GroupListAdapter extends SAFAdapter<GroupInfo> {

    private LayoutInflater mInflater;
    private ViewHolder holder;
    private Context mContext;
    private String mSearchKey;

    public GroupListAdapter(Context context, List<GroupInfo> list) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = list;
    }

    @Override
    public void setData(List<GroupInfo> list) {
        this.mList = list;
        this.notifyDataSetChanged();
    }

    public void setFilterKey(String key) {
        mSearchKey = key;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final GroupInfo item = (GroupInfo) getItem(i);
        if (view == null) {
            view = mInflater.inflate(R.layout.cell_group_list, null);
            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(55)));
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (TextUtils.isEmpty(item.GroupName) || "群聊".equals(item.GroupName)) {
            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + item.GroupID, "");
            holder.mTvName.setText(SearchUtil.formatSearchStr(mSearchKey, cacheName));
            holder.mTvName.setTag(R.id.tag_bean, true);
            GroupManager.getInstance().getDefaultGroupName(item.GroupID, new CommonLoadingListener<String>() {
                @Override
                public void onResponse(String s) {
                    boolean flag = (boolean) holder.mTvName.getTag(R.id.tag_bean);
                    if (flag) {
                        holder.mTvName.setText(SearchUtil.formatSearchStr(mSearchKey, s));
                    }
                }
            });
        } else {
            holder.mTvName.setTag(R.id.tag_bean, false);
            holder.mTvName.setText(SearchUtil.formatSearchStr(mSearchKey, item.GroupName));
            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + item.GroupID);
        }
        holder.mAvatar.setGroupId(item.GroupID);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, GroupChatActivity.class).putExtra("GroupId", item.GroupID));
            }
        });

        return view;
    }

    class ViewHolder {
        @InjectView(id = R.id.avatar_view)
        AvatarView mAvatar;
        @InjectView(id = R.id.tv_name)
        TextView mTvName;

        public ViewHolder(View view) {
            Injector.injectInto(this, view);
        }
    }
}
