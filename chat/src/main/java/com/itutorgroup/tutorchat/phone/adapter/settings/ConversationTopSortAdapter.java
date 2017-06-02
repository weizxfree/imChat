package com.itutorgroup.tutorchat.phone.adapter.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.beans.ConversationItem;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.ui.recycler.ItemTouchHelperAdapter;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;

import java.util.Collections;
import java.util.List;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/26.
 */
public class ConversationTopSortAdapter extends RecyclerView.Adapter<ConversationTopSortAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private Context mContext;
    private List<TopModel> mData;
    private View.OnTouchListener mOnDragTouchListener;
    private CommonLoadingListener mSortListener;

    public ConversationTopSortAdapter(Context context, List<TopModel> data, View.OnTouchListener listener) {
        mContext = context;
        mData = data;
        mOnDragTouchListener = listener;
    }

    public List<TopModel> getData() {
        return mData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_conversation_top_sort, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(60)));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setSortCallback(CommonLoadingListener listener) {
        mSortListener = listener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        @InjectView(id = R.id.tv_name)
        TextView mTvName;
        @InjectView(id = R.id.avatar_view)
        AvatarView mAvatar;
        @InjectView(id = R.id.tv_department)
        TextView mTvDepartment;
        @InjectView(id = R.id.view_drag_sort)
        View mDragSortView;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            Injector.injectInto(this, view);
        }

        public void bind(TopModel item, int position) {
            mDragSortView.setTag(R.id.tag_holder, MyViewHolder.this);
            mDragSortView.setOnTouchListener(mOnDragTouchListener);
            if (item.IdType == TopModel.ID_TYPE_USER) {
                loadUserInfo(item);
            } else if (item.IdType == TopModel.ID_TYPE_GROUP) {
                loadGroupInfo(item);
            }
        }

        private void loadGroupInfo(final TopModel item) {
            GroupManager.getInstance().getGroupInfo(item.TID, new CommonLoadingListener<GroupInfo>() {
                @Override
                public void onResponse(final GroupInfo groupInfo) {
                    if (groupInfo != null) {
                        mAvatar.setGroupId(groupInfo.GroupID);
                        if (TextUtils.isEmpty(groupInfo.GroupName) || "群聊".equals(groupInfo.GroupName)) {
                            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + groupInfo.GroupID, "");
                            mTvName.setTag(R.id.tag_bean, groupInfo.GroupID);
                            mTvName.setText(cacheName);
                            GroupManager.getInstance().getDefaultGroupName(groupInfo.GroupID, new CommonLoadingListener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    String id = (String) mTvName.getTag(R.id.tag_bean);
                                    if (!TextUtils.isEmpty(id) && id.equals(groupInfo.GroupID)) {
                                        mTvName.setText(s);
                                    }
                                }
                            });
                        } else {
                            mTvName.setText(groupInfo.GroupName);
                            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + item.TID);
                        }
                    }
                }
            });
        }

        private void loadUserInfo(TopModel item) {
            UserInfoManager.getInstance().getUserInfo(item.TID, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo userInfo) {
                    if (userInfo != null) {
                        mTvName.setText(userInfo.Name);
                        mAvatar.setUserHead(userInfo.Image);
                    }
                }
            });
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mData, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (mSortListener != null) {
            mSortListener.onResponse(null);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

}
