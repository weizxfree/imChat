package com.itutorgroup.tutorchat.phone.adapter.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ChatDetailActivity;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ContactsConstraintDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.response.AddContactResponse;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class SearchOnlineContactsAdapter extends MyBaseAdapter {

    private String mSearchKey;

    public SearchOnlineContactsAdapter(Context context, String key, List data) {
        super(context, data);
        mSearchKey = key;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_search_result_online_contacts;
    }

    public void setFilterKey(String key) {
        mSearchKey = key;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        TextView tvSendMessage = holder.getView(R.id.tv_send_message);
        tvSendMessage.setTag(R.id.tag_position, position);
        tvSendMessage.setOnClickListener(mOnSendMessageClickListener);

        TextView tvCollection = holder.getView(R.id.tv_collection);
        tvCollection.setTag(R.id.tag_position, position);
        tvCollection.setOnClickListener(mOnCollectionClickListener);

        convertView.setOnClickListener(mOnListItemClickListener);

        initData(position, convertView, holder);
        return convertView;
    }

    private void initData(int position, View convertView, ViewHolder holder) {
        if (mData.get(position) instanceof UserInfo) {
            UserInfo info = (UserInfo) mData.get(position);

            TextView tvEnglishName = holder.getView(R.id.tv_english_name);
            tvEnglishName.setText(SearchUtil.formatSearchStr(mSearchKey, info.Name));

            TextView tvChineseName = holder.getView(R.id.tv_chinese_name);
            tvChineseName.setText(SearchUtil.formatSearchStr(mSearchKey, info.ChineseName));

            TextView tvDepartmentGroup = holder.getView(R.id.tv_department);
            tvDepartmentGroup.setText(mContext.getString(R.string.personal_department_position, info.DepartmentGroup, info.Title));

            ImageView imageView = holder.getView(R.id.imv_header);
            imageView.setTag(R.id.tag_default, R.drawable.head_personal_blue);
            UserInfoHelper.showAvatar(info.Image, imageView);

            final TextView tvCollection = holder.getView(R.id.tv_collection);
            Observable.just(info)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<UserInfo, Boolean>() {
                        @Override
                        public Boolean call(UserInfo info) {
                            return ContactsConstraintDao.getInstance().isMyContact(info.UserID);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean isContact) {
                            if (isContact) {
                                tvCollection.setBackgroundColor(Color.TRANSPARENT);
                                tvCollection.setText(mContext.getString(R.string.already_collect));
                                tvCollection.setTextColor(mContext.getResources().getColor(R.color.text_color_btn_already_collect));
                            } else {
                                tvCollection.setBackgroundResource(R.drawable.bg_btn_contacts_collection);
                                tvCollection.setText(mContext.getString(R.string.collection));
                                tvCollection.setTextColor(mContext.getResources().getColor(R.color.white));
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    private View.OnClickListener mOnListItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UserInfo user = (UserInfo) v.getTag(R.id.tag_bean);
            mContext.startActivity(new Intent(mContext, ChatDetailActivity.class).putExtra("user_info", user));
        }
    };

    private View.OnClickListener mOnSendMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.tag_position);
            UserInfo info = (UserInfo) mData.get(position);
            mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", info.UserID));
        }
    };

    private View.OnClickListener mOnCollectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            int position = (int) v.getTag(R.id.tag_position);
            final UserInfo info = (UserInfo) mData.get(position);
            if (ContactsConstraintDao.getInstance().isMyContact(info.UserID)) {
                TextView tvCollection = (TextView) v;
                tvCollection.setBackgroundColor(Color.TRANSPARENT);
                tvCollection.setText(mContext.getString(R.string.already_collect));
                tvCollection.setTextColor(mContext.getResources().getColor(R.color.text_color_btn_already_collect));
                return;
            }
            ContactsManager contactsManager = ContactsManager.getInstance();
            contactsManager.addContact(mContext, info.UserID, ContactsManager.CONTACT_TYPE_PERSONAL,
                    new RequestHandler.RequestListener<AddContactResponse>() {
                        @Override
                        public void onResponse(AddContactResponse response, Bundle bundle) {
                            ContactsConstraintDao.getInstance().saveMyContactsConstraint(info);
                            TextView tv = (TextView) v;
                            tv.setBackgroundColor(Color.TRANSPARENT);
                            tv.setText(mContext.getString(R.string.already_collect));
                            tv.setTextColor(mContext.getResources().getColor(R.color.text_color_btn_already_collect));
                        }
                    });
        }
    };
}
