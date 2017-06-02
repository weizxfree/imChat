package com.itutorgroup.tutorchat.phone.adapter.chat;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class MessageRecipientAdapter extends MyBaseAdapter {

    private String mSearchKey;
    private String mGroupId;
    private boolean mShowRight;
    private CommonLoadingListener<UserInfo> mListener;
    private GroupInfoDao mDao;

    public MessageRecipientAdapter(Context context, List data, String key) {
        super(context, data);
        mSearchKey = key;
    }

    public MessageRecipientAdapter(Context context, List<UserInfo> list, String key, String groupId, boolean showRight, CommonLoadingListener<UserInfo> listener) {
        super(context, list);
        mSearchKey = key;
        mGroupId = groupId;
        mShowRight = showRight;
        mListener = listener;
        mDao = GroupInfoDao.getInstance();
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

    private void setData(int position, View convertView, final ViewHolder holder) {
        CircleImageView imvHeader = holder.getView(R.id.imv_header);
        TextView tvName = holder.getView(R.id.tv_name);
        TextView tvChineseName = holder.getView(R.id.tv_chinese_name);
        TextView tvDepartment = holder.getView(R.id.tv_department);

        UserInfo userInfo = (UserInfo) convertView.getTag(R.id.tag_bean);
        imvHeader.setTag(R.id.tag_default, R.drawable.head_personal_blue);
        UserInfoHelper.showAvatar(userInfo, imvHeader);

        SpannableString name = new SpannableString(userInfo.Name);
        SpannableString chineseName = new SpannableString(userInfo.ChineseName);
        if (!TextUtils.isEmpty(mSearchKey)) {
            name = SearchUtil.formatSearchStr(mSearchKey, userInfo.Name);
            chineseName = SearchUtil.formatSearchStr(mSearchKey, userInfo.ChineseName);
        }
        tvName.setText(name);
        tvChineseName.setText(chineseName);
        tvDepartment.setText(mContext.getString(R.string.personal_department_read_status, userInfo.DepartmentGroup,userInfo.Department, userInfo.Title));

        convertView.setOnClickListener(mOnItemClickListener);

        if (mShowRight) {
            Observable.just(userInfo.UserID)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<String, Integer>() {
                        @Override
                        public Integer call(String userId) {
                            return mDao.getUserRightInGroup(mGroupId, userId);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            TextView tvRight = holder.getView(R.id.tv_group_right);
                            tvRight.setVisibility(integer > 1 ? View.GONE : View.VISIBLE);
                            tvRight.setText(integer == 0 ? R.string.group_owner : R.string.group_manager);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    private View.OnClickListener mOnItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInfo userInfo = (UserInfo) v.getTag(R.id.tag_bean);
            if (mListener != null) {
                mListener.onResponse(userInfo);
            }
        }
    };

}
