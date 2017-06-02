package com.itutorgroup.tutorchat.phone.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ChatDetailActivity;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.beans.UserInfoVo;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.ContactsPinnedSectionItem;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.PinnedSectionItem;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.ui.common.PinnedSectionListView;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.Preconditions;


public class ContactsSortAdapter extends SAFAdapter<PinnedSectionItem<UserInfoVo>> implements SectionIndexer, PinnedSectionListView.PinnedSectionListAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private boolean mIsSelectMode = false;
    private CheckBoxListener mListener;
    private static HashMap<String, Boolean> mSelectMap;
    private List<String> mDisableIdList;
    private String mFilterKey;

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (Preconditions.isNotBlank(mList)) {
            for (int i = 0; i < mList.size(); i++) {
                int type = getItemViewType(i);
                if (type == PinnedSectionItem.ITEM) {
                    if (mList.get(i).data.firstLetter.charAt(0) == sectionIndex) {
                        return i;
                    }
                } else if (type == PinnedSectionItem.SECTION) {
                    ContactsPinnedSectionItem item = (ContactsPinnedSectionItem) mList.get(i);
                    if (item.mSection.charAt(0) == sectionIndex) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (Preconditions.isNotBlank(mList)) {
            int type = getItemViewType(position);
            if (type == PinnedSectionItem.ITEM) {
                return mList.get(position).data.firstLetter.charAt(0);
            } else if (type == PinnedSectionItem.SECTION) {
                ContactsPinnedSectionItem item = (ContactsPinnedSectionItem) mList.get(position);
                return item.mSection.charAt(0);
            }
        }
        return 0;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == PinnedSectionItem.SECTION;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void setCurrentSelected(List<String> list) {
        if (list != null && list.size() > 0) {
            for (String id : list) {
                mSelectMap.put(id, true);
            }
            notifyDataSetChanged();
        }
    }

    public void setDisableList(List<String> list) {
        mDisableIdList = list;
        notifyDataSetChanged();
    }

    public void clearAllChecked() {
        mSelectMap.clear();
        notifyDataSetChanged();
    }

    public void removeSelectedId(String id) {
        if (mSelectMap.remove(id)) {
            notifyDataSetChanged();
        }
    }

    public void setFilterKey(String key) {
        mFilterKey = key;
    }

    @Override
    public void setData(List<PinnedSectionItem<UserInfoVo>> list) {
        this.mList = list;
        this.notifyDataSetChanged();
    }

    public interface CheckBoxListener {
        void onSelectIdAdd(String id);

        void onSelectIdRemove(String id);
    }

    public void setCheckBoxListener(CheckBoxListener listener) {
        mListener = listener;
    }

    public ContactsSortAdapter(Context context, ArrayList<PinnedSectionItem<UserInfoVo>> pinnedVOList, boolean isSelectStatus) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = pinnedVOList;
        mSelectMap = new HashMap<>();
        this.mIsSelectMode = isSelectStatus;
    }

    public ContactsSortAdapter(Context context, ArrayList<PinnedSectionItem<UserInfoVo>> pinnedVOList, List<String> disableList) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = pinnedVOList;
        mSelectMap = new HashMap<>();
        this.mIsSelectMode = true;
        mDisableIdList = disableList;
    }

    @Override
    public PinnedSectionItem<UserInfoVo> getItem(int position) {
        return (PinnedSectionItem<UserInfoVo>) super.getItem(position);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        PinnedSectionItem pinnedSectionItem = getItem(i);
        ViewHolder holder = null;
        final UserInfoVo item = getItem(i).data;
        if (view == null) {
            if (pinnedSectionItem.type == PinnedSectionItem.ITEM) {
                view = mInflater.inflate(R.layout.list_item_my_contacts, null);
                view.setLayoutParams(new PinnedSectionListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(55)));
                holder = new ItemViewHolder(view);
            } else if (pinnedSectionItem.type == PinnedSectionItem.SECTION) {
                view = mInflater.inflate(R.layout.list_item_pinned_section, null);
                holder = new SectionViewHolder(view);
            }
        } else {
            holder = (ViewHolder) view.getTag(R.id.tag_holder);
        }

        if (view != null) {
            view.setTag(R.id.tag_holder, holder);
            view.setTag(R.id.tag_position, i);
            view.setTag(R.id.tag_bean, item);
        }
        if (holder != null) {
            if (pinnedSectionItem.type == PinnedSectionItem.ITEM) {
                holder.setBean(getItem(i).data, mIsSelectMode, i);
            } else if (pinnedSectionItem.type == PinnedSectionItem.SECTION) {
                ContactsPinnedSectionItem contactsPinnedSectionItem = (ContactsPinnedSectionItem) mList.get(i);
                holder.setBean(contactsPinnedSectionItem.mSection, mIsSelectMode, i);
            }
        }

        if (view != null) {
            view.setOnClickListener(mOnContactClickListener);
        }
        return view;
    }

    private View.OnClickListener mOnContactClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.tag_position);
            if (getItemViewType(position) == PinnedSectionItem.ITEM) {
                if (mIsSelectMode) {
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                    checkBox.performClick();
                } else {
                    UserInfoVo item = (UserInfoVo) v.getTag(R.id.tag_bean);
                    UserInfo userInfo = UserInfoDao.getInstance().selectWithId(item.id);
                    mContext.startActivity(new Intent(mContext, ChatDetailActivity.class).putExtra("user_info", userInfo));
                }
            }
        }
    };

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!buttonView.isEnabled()) {
                return;
            }
            UserInfoVo vo = (UserInfoVo) buttonView.getTag(R.id.tag_bean);

            Boolean tmp = mSelectMap.get(vo.id);
            if (tmp == null) {
                tmp = false;
            }
            if (tmp != isChecked) {
                mSelectMap.put(vo.id, isChecked);
                if (mListener != null) {
                    if (isChecked) {
                        mListener.onSelectIdAdd(vo.id);
                    } else {
                        mListener.onSelectIdRemove(vo.id);
                    }
                }
            }
        }
    };

    class ViewHolder<T> {
        public void setBean(T t, boolean isSelectStatus, int position) {

        }
    }

    class ItemViewHolder extends ViewHolder<UserInfoVo> {

        View mContentView;

        @InjectView(id = R.id.imv_header)
        CircleImageView imvHeader;
        @InjectView(id = R.id.tv_english_name)
        TextView tvName;
        @InjectView(id = R.id.tv_english_title)
        TextView tvEnglishTitle;
        @InjectView(id = R.id.tv_department)
        TextView tvDepartment;
        @InjectView(id = R.id.checkBox)
        CheckBox checkBox;
        @InjectView(id = R.id.divider_top)
        View mDividerTopView;
        @InjectView(id = R.id.divider_bottom)
        View mDividerBottomView;

        public ItemViewHolder(View view) {
            Injector.injectInto(this, view);
            mContentView = view;
            imvHeader.setTag(R.id.tag_default, R.drawable.head_personal_blue);
        }

        @Override
        public void setBean(final UserInfoVo item, boolean isSelectStatus, int position) {
            UserInfoHelper.showAvatar(item.img, imvHeader);
            UserInfoManager.getInstance().getUserInfo(item.id, new CommonLoadingListener<UserInfo>() {
                @Override
                public void onResponse(UserInfo userInfo) {
                    if (!TextUtils.isEmpty(userInfo.Image)) {
                        item.img = userInfo.Image;
                        UserInfoHelper.showAvatar(userInfo.Image, imvHeader);
                    } else {
                        imvHeader.setImageResource(R.drawable.head_personal_blue);
                    }
                }
            });
            if (!TextUtils.isEmpty(mFilterKey)) {
                tvName.setText(SearchUtil.formatSearchStr(mFilterKey, item.Name));
            } else {
                tvName.setText(item.Name);
            }
            tvEnglishTitle.setText(item.Title);
            tvDepartment.setText(item.Department);

            if (isSelectStatus) {
                checkBox.setVisibility(View.VISIBLE);
                Boolean isChecked = mSelectMap.get(item.id);
                if (isChecked == null) {
                    isChecked = false;
                }

                checkBox.setTag(R.id.tag_position, position);
                checkBox.setTag(R.id.tag_bean, item);

                checkBox.setChecked(isChecked);
                checkBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

            } else {
                checkBox.setVisibility(View.GONE);
            }

            if (mDisableIdList != null && mDisableIdList.size() != 0) {
                boolean flag = mDisableIdList.contains(item.id);
                mContentView.setEnabled(!flag);
                checkBox.setEnabled(!flag);
                if (flag) {
                    checkBox.setChecked(true);
                }
            }

            if (position > 0) {
                mDividerTopView.setBackgroundResource(mIsSelectMode ? R.drawable.list_divider_margin_left_13 : R.drawable.list_divider_margin_left_13);
                mDividerTopView.setVisibility(getItemViewType(position - 1) == PinnedSectionItem.ITEM ? View.VISIBLE : View.GONE);
                mDividerBottomView.setVisibility(position == getCount() - 1 ? View.VISIBLE : View.GONE);
            }
        }
    }

    class SectionViewHolder extends ViewHolder<String> {

        @InjectView(id = R.id.tv_item_pinned_section)
        TextView tvSection;

        public SectionViewHolder(View view) {
            Injector.injectInto(this, view);
        }

        @Override
        public void setBean(String section, boolean isSelectStatus, int position) {
            tvSection.setText(section);
        }
    }

}