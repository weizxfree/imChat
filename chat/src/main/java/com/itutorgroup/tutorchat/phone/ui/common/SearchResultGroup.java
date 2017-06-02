package com.itutorgroup.tutorchat.phone.ui.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity;
import com.itutorgroup.tutorchat.phone.activity.search.SearchResultActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.message.SearchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/9/5.
 */
public class SearchResultGroup extends LinearLayout {
    private View mContentView;
    private TextView mTvTitle;
    private TextView mTvSeeMore;
    private RelativeLayout mGroupSeeMore;

    public static final int TYPE_CONTACT = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_MESSAGE = 2;

    private int mType;
    private String mKey;
    private ArrayList mData;

    LinearLayout mLLResult;

    public SearchResultGroup(Context context) {
        super(context);
    }

    public SearchResultGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SearchResultGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.group_search_result, null);
        addView(mContentView);
        initViews();
        loadAttr(attrs);
    }

    private void loadAttr(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SearchResultGroup);
        mType = ta.getInt(R.styleable.SearchResultGroup_srg_type, -1);
        ta.recycle();

        initData();
    }

    private void initData() {
        String title = "";
        switch (mType) {
            case TYPE_CONTACT:
                title = getContext().getString(R.string.contacts);
                break;
            case TYPE_GROUP:
                title = getContext().getString(R.string.group_chat);
                break;
            case TYPE_MESSAGE:
                title = getContext().getString(R.string.search_chat_history);
                break;
        }
        String footer = getContext().getString(R.string.footer_search_more, title);
        mTvTitle.setText(title);
        mTvSeeMore.setText(footer);
    }

    private void initViews() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvSeeMore = (TextView) findViewById(R.id.tv_see_more);
        mGroupSeeMore = (RelativeLayout) findViewById(R.id.rl_group_more);
        mLLResult = (LinearLayout) findViewById(R.id.ll_search_result);

        mGroupSeeMore.setOnClickListener(mOnSeeMoreClickListener);
    }

    private OnClickListener mOnSeeMoreClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mData == null || mData.size() == 0) {
                return;
            }
            Intent intent = new Intent(getContext(), SearchResultActivity.class);
            intent.putExtra("type", mType);
            intent.putExtra("key", mKey);
            intent.putExtra("data", mData);
            getContext().startActivity(intent);
        }
    };

    public void setData(String key, List list) {
        clear();
        mKey = key;
        mData = (ArrayList) list;
        if (list == null || list.size() == 0) {
            return;
        }
        mGroupSeeMore.setVisibility(list.size() > 3 ? VISIBLE : GONE);

        switch (mType) {
            case TYPE_CONTACT:
                dispatchContacts(key, list);
                break;
            case TYPE_GROUP:
                dispatchGroupInfo(key, list);
                break;
            case TYPE_MESSAGE:
                dispatchMessageModel(key, list);
                break;
        }
    }

    private void dispatchMessageModel(String key, List<MessageModel> list) {
        int size = Math.min(3, list.size());
        for (int i = 0; i < size; i++) {
            addResultItem(i, key);
        }
    }

    private void dispatchGroupInfo(String key, List<SearchManager.SearchGroupBean> list) {
        int size = Math.min(3, list.size());
        for (int i = 0; i < size; i++) {
            addResultItem(i, key);
        }
    }

    private void dispatchContacts(String key, List<UserInfo> list) {
        int size = Math.min(3, list.size());
        for (int i = 0; i < size; i++) {
            addResultItem(i, key);
        }
    }

    private void addResultItem(int position, String key) {
        View view = inflate(getContext(), R.layout.list_item_search_local_item, null);

        ViewHolder holder = new ViewHolder(getContext(), view, mType, key, mData);
        holder.setPosition(position);
        view.setTag(R.id.tag_holder, holder);

        view.setTag(R.id.tag_position, position);

        mLLResult.addView(view);
    }

    public static class ViewHolder {

        public boolean mComposeMode = true;

        public Context mContext;
        public View mView;
        public AvatarView avatarView;
        public TextView tvName;
        public TextView tvDepartment;
        public TextView tvLastMessage;

        public List mData;
        public String mKey;
        public int mType;

        public List mResultData;

        public ViewHolder(Context context, View view, int type, String key, List data) {
            mContext = context;
            mView = view;
            view.setOnClickListener(mOnItemClickListener);
            avatarView = (AvatarView) view.findViewById(R.id.avatar_view);
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvDepartment = (TextView) view.findViewById(R.id.tv_department);
            tvLastMessage = (TextView) view.findViewById(R.id.tv_last_message);
            mType = type;
            mKey = key;
            mData = data;
        }

        public void setPosition(int position) {
            switch (mType) {
                case TYPE_CONTACT:
                    UserInfo user = (UserInfo) mData.get(position);
                    SearchUtil.loadUser(this, position, user);
                    break;
                case TYPE_GROUP:
                    SearchManager.SearchGroupBean group = (SearchManager.SearchGroupBean) mData.get(position);
                    SearchUtil.loadGroup(this, position, group);
                    break;
                case TYPE_MESSAGE:
                    MessageModel message = (MessageModel) mData.get(position);
                    SearchUtil.loadMessage(this, position, message);
                    break;
            }
        }


        private OnClickListener mOnItemClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag(R.id.tag_position);
                switch (mType) {
                    case TYPE_CONTACT:
                        UserInfo user = (UserInfo) mData.get(position);
                        mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", user.UserID));
                        break;
                    case TYPE_GROUP:
                        SearchManager.SearchGroupBean group = (SearchManager.SearchGroupBean) mData.get(position);
                        mContext.startActivity(new Intent(mContext, GroupChatActivity.class).putExtra("GroupId", group.id));
                        break;
                    case TYPE_MESSAGE:
                        final MessageModel message = (MessageModel) mData.get(position);
                        if (mResultData != null && mResultData.size() > 1) {
                            Intent intent = new Intent(mContext, SearchResultActivity.class);
                            intent.putExtra("type", mType);
                            intent.putExtra("key", mKey);
                            intent.putExtra("compose", false);
                            intent.putExtra("data", (ArrayList) mResultData);
                            mContext.startActivity(intent);
                        } else {
                            String groupId = message.GroupId;
                            if (!TextUtils.isEmpty(groupId)) {
                                mContext.startActivity(new Intent(mContext, GroupChatActivity.class).putExtra("GroupId", groupId).putExtra("message_id", message.MessageID));
                            } else {
                                String targetId = ConversationManager.getInstance().getMessageTargetId(message);
                                UserInfoManager.getInstance().getUserInfo(targetId, new CommonLoadingListener<UserInfo>() {
                                    @Override
                                    public void onResponse(UserInfo userInfo) {
                                        mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", userInfo.UserID).putExtra("message_id", message.MessageID));
                                    }
                                });
                            }
                        }
                        break;
                }
            }
        };
    }

    public void clear() {
        mLLResult.removeAllViews();
    }
}
