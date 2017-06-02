package com.itutorgroup.tutorchat.phone.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ConversationTopSortActivity;
import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity;
import com.itutorgroup.tutorchat.phone.domain.beans.ConversationItem;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.ui.common.groupimageview.AvatarView;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.CacheManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.TopChatManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.message.ConversationUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ListDialogHelper;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/12 0012.
 */
public class ConversationAdapter extends SAFAdapter<ConversationItem> {

    private LayoutInflater mInflater;
    private Context mContext;
    private boolean mScrollState = false;

    public ConversationAdapter(Context context, List<ConversationItem> list) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mList = list;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ConversationItem item = (ConversationItem) getItem(i);
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_conversation, null);
            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(60)));
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag(R.id.tag_holder);
        }
        view.setTag(R.id.tag_holder, holder);
        view.setTag(R.id.tag_bean, item);
        holder.setBean(item);
        view.setOnClickListener(mOnListItemClickListener);
        view.setOnLongClickListener(mOnItemLongClickListener);
        return view;
    }

    private View.OnLongClickListener mOnItemLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Observable.just(v)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<View, ListDialogHelper>() {
                        @Override
                        public ListDialogHelper call(View view) {
                            ConversationItem item = (ConversationItem) view.getTag(R.id.tag_bean);
                            final int targetType = item.chatInfo instanceof UserInfo ? TopModel.ID_TYPE_USER : TopModel.ID_TYPE_GROUP;
                            final String targetId = item.targetId;
                            String deleteConversation = mContext.getString(R.string.remove_conversation);
                            final boolean isTopChat = TopChatManager.getInstance().isTop(item.targetId);
                            final boolean showSortTopChat = isTopChat && TopChatManager.getInstance().getTopCount() > 1;
                            String topChat = mContext.getString(isTopChat ? R.string.remove_top_chat : R.string.top_chat);
                            boolean canShowSetReadState = ConversationDao.getInstance().canShowReadState(targetId);
                            final boolean unread = ConversationDao.getInstance().getUnreadCount(targetId) > 0;
                            String toggleReadState = mContext.getString(unread ? R.string.conversation_set_read : R.string.conversation_set_unread);
                            ArrayList<String> menuList = new ArrayList<>();
                            final ArrayList<Runnable> actionList = new ArrayList<>();
                            if (canShowSetReadState) {
                                menuList.add(toggleReadState);
                                actionList.add(new Runnable() {
                                    @Override
                                    public void run() {
                                        ConversationManager.getInstance().setConversationReadState(targetId, unread);
                                    }
                                });
                            }
                            menuList.add(deleteConversation);
                            actionList.add(new Runnable() {
                                @Override
                                public void run() {
                                    ConversationUtil.performClearChatHistory(mContext, targetId, true);
                                }
                            });
                            menuList.add(topChat);
                            actionList.add(new Runnable() {
                                @Override
                                public void run() {
                                    ConversationManager.getInstance().setConversationTopChat(targetId, targetType, !isTopChat);
                                }
                            });
                            if (showSortTopChat) {
                                menuList.add(mContext.getString(R.string.title_top_chat_sort));
                                actionList.add(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContext.startActivity(new Intent(mContext, ConversationTopSortActivity.class));
                                    }
                                });
                            }
                            return new ListDialogHelper(mContext, menuList, actionList);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<ListDialogHelper>() {
                        @Override
                        public void call(ListDialogHelper helper) {
                            helper.show();
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
            return true;
        }
    };


    private OnClickListener mOnListItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ConversationItem item = (ConversationItem) v.getTag(R.id.tag_bean);
            if (item.chatInfo instanceof UserInfo) {
                mContext.startActivity(new Intent(mContext, SingleChatActivity.class).putExtra("user_id", ((UserInfo) item.chatInfo).UserID));
            } else if (item.chatInfo instanceof GroupInfo) {
                mContext.startActivity(new Intent(mContext, GroupChatActivity.class).putExtra("GroupId", item.targetId));
            }
        }
    };

    public void setScrollState(boolean state) {
        mScrollState = state;
    }

    class ViewHolder {
        View mView;
        @InjectView(id = R.id.avatar_view)
        AvatarView mAvatar;
        @InjectView(id = R.id.tv_name)
        TextView mTvName;
        @InjectView(id = R.id.tv_department)
        TextView mTvDepartment;
        @InjectView(id = R.id.tv_has_alt_me)
        TextView mTvHasAltMe;
        @InjectView(id = R.id.tv_last_message)
        TextView mTvLastMessage;
        @InjectView(id = R.id.tv_time)
        TextView mTvTime;
        @InjectView(id = R.id.tv_unread_count)
        TextView mTvUnRead;
        @InjectView(id = R.id.imv_no_disturb)
        ImageView mImvNoDisturb;

        public ViewHolder(View view) {
            Injector.injectInto(this, view);
            mView = view;
        }

        public void setBean(final ConversationItem item) {
            mTvLastMessage.setTag(R.id.tag_default, item.targetId);
            mAvatar.showDefault();
            if (TextUtils.isEmpty(item.groupId)) {
                mAvatar.setUserHead(item.imagePath);
            } else {
                mAvatar.setGroupId(item.targetId);
            }
            if (mScrollState) {
//                mAvatar.hide();
                mTvDepartment.setText(item.title);
                mTvTime.setText(TimeUtils.formatTimeString(item.time));
                mTvName.setText(item.name);
                mImvNoDisturb.setVisibility(item.isDisturb ? View.VISIBLE : View.GONE);
                ConversationUtil.setUnreadCountAndBg(mTvUnRead, item.unReadCount, item.isDisturb);
                mView.setBackgroundResource(item.isTop ? R.drawable.bg_top_chat_list_item_selector : R.drawable.bg_default_list_item_selector);
                mTvHasAltMe.setText(item.altMeText);
                mTvHasAltMe.setVisibility(TextUtils.isEmpty(item.altMeText) ? View.GONE : View.VISIBLE);
                MessageManager.getInstance().loadEmojiText(mContext, mTvLastMessage, item.posterName + item.lastMessage);
            } else {
                mTvDepartment.setText(item.title);

                mTvTime.setText(TimeUtils.formatTimeString(item.time));

                loadName(item);
                checkIsDisturb(item);
                checkIsTopChat(item);
                checkAltOrAnnouncement(item);
                loadLastMessage(item);
            }
        }

        private void loadName(final ConversationItem item) {
            if (TextUtils.isEmpty(item.groupId)) {
                mTvName.setText(item.name);
                UserInfoManager.getInstance().getUserInfo(item.targetId, new CommonLoadingListener<UserInfo>() {
                    @Override
                    public void onResponse(UserInfo userInfo) {
                        if (userInfo != null) {
                            if (!userInfo.Name.equals(item.name)) {
                                item.name = userInfo.Name;
                                mTvName.setText(userInfo.Name);
                            }

                            if (!TextUtils.isEmpty(userInfo.Image)) {
                                item.imagePath = userInfo.Image;
                                mAvatar.setUserHead(item.imagePath);
                            }
                        }
                    }
                });
            } else {
                if (TextUtils.isEmpty(((GroupInfo) item.chatInfo).GroupID)) {
                    GroupManager.getInstance().getGroupInfo(item.groupId, new CommonLoadingListener<GroupInfo>() {
                        @Override
                        public void onResponse(GroupInfo groupInfo) {
                            item.chatInfo = groupInfo;
                            GroupManager.getInstance().formatGroupName((GroupInfo) item.chatInfo, new CommonLoadingListener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    item.name = s;
                                    mTvName.setText(s);
                                }
                            });
                        }
                    });
                } else {
                    GroupManager.getInstance().formatGroupName((GroupInfo) item.chatInfo, new CommonLoadingListener<String>() {
                        @Override
                        public void onResponse(String s) {
                            item.name = s;
                            mTvName.setText(s);
                        }
                    });
                }
            }
        }

        private void checkUnreadCount(final ConversationItem item, final Boolean isDisturb) {
            Observable.just(item)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ConversationItem, Integer>() {
                        @Override
                        public Integer call(ConversationItem conversationItem) {
                            return ConversationDao.getInstance().getUnreadCount(item.targetId);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            item.unReadCount = integer;
                            ConversationUtil.setUnreadCountAndBg(mTvUnRead, integer, isDisturb);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }

        private void setLastMessageContent(String tag, String text) {
            Object o = mTvLastMessage.getTag(R.id.tag_default);
            if (o != null && o instanceof String) {
                String tvTag = (String) mTvLastMessage.getTag(R.id.tag_default);
                if (TextUtils.equals(tag, tvTag)) {
                    Observable.just(text)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String s) {
                                    mTvLastMessage.setText(s);
                                }
                            }, CommonUtil.ACTION_EXCEPTION);
                }
            }
        }

        private void loadLastMessage(final ConversationItem item) {
            if (TextUtils.isEmpty(item.posterId) && TextUtils.isEmpty(item.lastMessage)) {
                setLastMessageContent(item.targetId, "");
            }
            Observable.just(item)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ConversationItem, ConversationItem>() {
                        @Override
                        public ConversationItem call(ConversationItem conversationItem) {
                            conversationItem.draft = ConversationDao.getInstance().queryConversationDraft(conversationItem.targetId);
                            return conversationItem;
                        }
                    })
                    .map(new Func1<ConversationItem, String>() {
                        @Override
                        public String call(ConversationItem conversationItem) {
                            if (!TextUtils.isEmpty(conversationItem.draft)) {
                                return conversationItem.draft;
                            } else {
                                if (item.messageModel != null
                                        && (item.messageModel.Type == MessageType.SYSTEM_MESSAGE
                                        || item.messageModel.Type == MessageType.WITH_DRAWAL)) {
                                    String cache = CacheManager.getInst().getString(item.messageModel.Content);
                                    if (!TextUtils.isEmpty(cache)) {
                                        setLastMessageContent(item.targetId, cache);
                                    }
                                    MessageManager.getInstance().getMessageConversationText(item.messageModel, new CommonLoadingListener<String>() {
                                        @Override
                                        public void onResponse(String s) {
                                            item.lastMessage = s;
                                            setLastMessageContent(item.targetId, s);
                                            CacheManager.getInst().save(item.messageModel.Content, s);
                                        }
                                    });
                                    return null;
                                } else if (conversationItem.chatInfo instanceof GroupInfo) {
                                    String posterId = conversationItem.posterId;
                                    UserInfoManager.getInstance().getUserInfo(posterId, new CommonLoadingListener<UserInfo>() {
                                        @Override
                                        public void onResponse(UserInfo userInfo) {
                                            String currentUserId = AccountManager.getInstance().getCurrentUserId();
                                            if (!TextUtils.isEmpty(currentUserId) && !currentUserId.equals(userInfo.UserID)) {
                                                item.posterName = userInfo.Name + ": ";
                                            } else {
                                                item.posterName = "";
                                            }
                                            MessageManager.getInstance().loadEmojiText(mContext, mTvLastMessage, item.posterName + item.lastMessage);
                                        }
                                    });
                                } else if (conversationItem.chatInfo instanceof UserInfo) {
                                    return item.lastMessage;
                                }
                            }
                            return null;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if (TextUtils.isEmpty(s) && item.chatInfo instanceof GroupInfo) {
                                return;
                            }
                            MessageManager.getInstance().loadEmojiText(mContext, mTvLastMessage, s);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }

        private void checkIsTopChat(final ConversationItem item) {
            Observable.just(item)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ConversationItem, Boolean>() {
                        @Override
                        public Boolean call(ConversationItem conversationItem) {
                            return TopChatManager.getInstance().isTop(conversationItem.targetId);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean isTopChat) {
                            item.isTop = isTopChat;
                            mView.setBackgroundResource(isTopChat ? R.drawable.bg_top_chat_list_item_selector : R.drawable.bg_default_list_item_selector);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }

        private void checkIsDisturb(final ConversationItem item) {

            Observable.just(item)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ConversationItem, Boolean>() {
                        @Override
                        public Boolean call(ConversationItem item) {
                            String id = item.targetId;
                            if (!TextUtils.isEmpty(id)) {
                                return UserSettingManager.getInstance().isTargetIsDisturb(id);
                            } else {
                                return false;
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean isDisturb) {
                            item.isDisturb = isDisturb;
                            mImvNoDisturb.setVisibility(isDisturb ? View.VISIBLE : View.GONE);
                            checkUnreadCount(item, isDisturb);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }

        private void checkAltOrAnnouncement(final ConversationItem item) {
            Observable.just(item)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<ConversationItem, Boolean>() {
                        @Override
                        public Boolean call(ConversationItem conversationItem) {
                            return MessageDao.getInstance().hasUnreadAnnoucementMessageWithTargetId(conversationItem.targetId);
                        }
                    })
                    .map(new Func1<Boolean, String>() {
                        @Override
                        public String call(Boolean hasAnnouncement) {
                            if (hasAnnouncement) {
                                return mContext.getString(R.string.tip_conversation_has_announcement);
                            } else {
                                return "";
                            }
                        }
                    })
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String tag) {
                            boolean hasAltMeMsg = MessageDao.getInstance().hasAltMeMessageWithTargetId(item.targetId);
                            if (hasAltMeMsg) {
                                tag += mContext.getString(R.string.tip_conversation_has_alt_me);
                            }
                            return tag;
                        }
                    })
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            String draft = ConversationDao.getInstance().queryConversationDraft(item.targetId);
                            if (!TextUtils.isEmpty(draft)) {
                                s = mContext.getString(R.string.tip_conversation_has_draft);
                            }
                            return s;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String tag) {
                            item.altMeText = tag;
                            mTvHasAltMe.setText(tag);
                            mTvHasAltMe.setVisibility(TextUtils.isEmpty(tag) ? View.GONE : View.VISIBLE);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }
}
