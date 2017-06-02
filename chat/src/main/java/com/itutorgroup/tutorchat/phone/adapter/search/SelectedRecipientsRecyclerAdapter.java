package com.itutorgroup.tutorchat.phone.adapter.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.adapter.ContactsSortAdapter;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;

/**
 * Created by joyinzhao on 2016/9/13.
 */
public class SelectedRecipientsRecyclerAdapter extends RecyclerView.Adapter<SelectedRecipientsRecyclerAdapter.ViewHolder> {

    private List<UserInfo> mData;
    private Context mContext;
    private ContactsSortAdapter.CheckBoxListener mListener;

    public SelectedRecipientsRecyclerAdapter(Context context, List<UserInfo> data, ContactsSortAdapter.CheckBoxListener listener) {
        mContext = context;
        mData = data;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_selected_recipients, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.imvHeader.setTag(R.id.tag_bean, mData.get(i));
        viewHolder.imvHeader.setTag(R.id.tag_default, R.drawable.head_personal_blue);
        viewHolder.imvHeader.setOnClickListener(mOnItemClickListener);
        UserInfoHelper.showAvatar(mData.get(i), viewHolder.imvHeader);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInfo userInfo = (UserInfo) v.getTag(R.id.tag_bean);
            mListener.onSelectIdRemove(userInfo.UserID);
//            mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_info", userInfo));
        }
    };

    public void setData(List<UserInfo> list) {
        mData = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imvHeader;

        public ViewHolder(View itemView) {
            super(itemView);
            imvHeader = (CircleImageView) itemView.findViewById(R.id.imv_header);
        }
    }
}
