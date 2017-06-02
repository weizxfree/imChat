package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.beans.ConversationItem;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.ConversationModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.response.GetGroupInfoResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class ConversationManager {
    private static ConversationManager sInstance;

    public static ConversationManager getInstance() {
        if (sInstance == null) {
            synchronized (ConversationManager.class) {
                if (sInstance == null) {
                    sInstance = new ConversationManager();
                }
            }
        }
        return sInstance;
    }

    public String getMessageTargetId(MessageModel message) {
        String targetId = message.targetId;
        if (TextUtils.isEmpty(targetId)) {
            String groupId = message.GroupId;
            if (!TextUtils.isEmpty(groupId)) {
                targetId = groupId;
            } else {
                targetId = message.PosterID;
                String receiverId = message.ReceiverID;

                String currentId = AccountManager.getInstance().getCurrentUserId();
                if (TextUtils.equals(currentId, targetId)) {
                    targetId = receiverId;
                }
            }
        }
        return targetId;
    }

    public String getConversationTargetId(ConversationItem item) {
        String targetId = null;
        if (item.chatInfo instanceof UserInfo) {
            targetId = ((UserInfo) item.chatInfo).UserID;
        } else if (item.chatInfo instanceof GroupInfo) {
            targetId = ((GroupInfo) item.chatInfo).GroupID;
        }
        return targetId;
    }

    public List<ConversationItem> getConversationList(List<String> idList) {
        MessageDao messageDao = MessageDao.getInstance();
        List<MessageModel> messageModel = messageDao.selectChatList(idList);
        if (messageModel == null) {
            return null;
        }

        List<ConversationItem> list = new ArrayList<>();
        for (MessageModel message : messageModel) {
            ConversationItem conversation = getConversationItemByMessageModel(message);
            if (!TextUtils.isEmpty(conversation.targetId)) {
                list.add(conversation);
            }
        }

        List<ConversationItem> emptyConversationList = getConversationListEmpty(idList);
        if (emptyConversationList != null && emptyConversationList.size() > 0) {
            for (ConversationItem item : emptyConversationList) {
                if (!list.contains(item) && !TextUtils.isEmpty(item.targetId)) {
                    list.add(item);
                }
            }
        }

        return sortConversationList(list);
    }

    public ConversationItem getConversationItemByMessageModel(MessageModel message) {
        String targetId = getMessageTargetId(message);
        ConversationItem conversation = new ConversationItem();
        conversation.messageModel = message;
        conversation.lastMessage = MessageManager.getInstance().getMessageConversationText(message, null);
        conversation.posterId = message.PosterID;
        conversation.time = message.CreateTime;
        conversation.groupId = message.GroupId;
        conversation.unReadCount = MessageDao.getInstance().getConversationUnreadMessageCount(targetId);
        String groupId = message.GroupId;

        if (!TextUtils.isEmpty(groupId)) {
            conversation = getConversationByGroupId(conversation, groupId);
        } else {
            conversation = getConversationByUserId(conversation, targetId);
        }
        return conversation;
    }

    public List<ConversationItem> sortConversationList(List<ConversationItem> list) {
        Collections.sort(list, new Comparator<ConversationItem>() {
            @Override
            public int compare(ConversationItem lhs, ConversationItem rhs) {
                return (int) (rhs.time - lhs.time);
            }
        });
        list = sortTopChat(list);
        createConversationIfNotExists(list);
        return list;
    }

    private void createConversationIfNotExists(List<ConversationItem> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        Observable.from(list)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<ConversationItem>() {
                    @Override
                    public void call(ConversationItem conversationItem) {
                        String groupId = null;
                        if (conversationItem.chatInfo instanceof GroupInfo) {
                            groupId = conversationItem.targetId;
                        }
                        ConversationDao.getInstance().createConversation(conversationItem.targetId, groupId, conversationItem.time);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private List<ConversationItem> getConversationListEmpty(List<String> idList) {
        List<ConversationModel> conversationModelList = ConversationDao.getInstance().queryAll(idList);
        List<ConversationItem> list = null;
        if (conversationModelList != null && conversationModelList.size() != 0) {
            list = new ArrayList<>();
            for (ConversationModel model : conversationModelList) {
                ConversationItem item = new ConversationItem();
                item.lastMessage = "";
                item.time = model.CreateTime;
                String groupId = model.GroupId;
                if (!TextUtils.isEmpty(groupId)) {
                    item = getConversationByGroupId(item, groupId);
                } else {
                    item = getConversationByUserId(item, model.targetID);
                }
                list.add(item);
            }
        }
        return list;
    }

    private List<ConversationItem> sortTopChat(List<ConversationItem> list) {
        final List<TopModel> topList = TopChatManager.getInstance().getTopModelList();
        if (topList == null || topList.size() == 0
                || list == null || list.size() == 0) {
            return list;
        }

        Collections.sort(list, new Comparator<ConversationItem>() {
            @Override
            public int compare(ConversationItem lhs, ConversationItem rhs) {
                String lid = ConversationManager.getInstance().getConversationTargetId(lhs);
                String rid = ConversationManager.getInstance().getConversationTargetId(rhs);
                int rOrder = getSortNum(rid, topList);
                int lOrder = getSortNum(lid, topList);
                return rOrder == lOrder ? 0 : lOrder < rOrder ? 1 : -1;
            }
        });
        return list;
    }

    private int getSortNum(String targetId, List<TopModel> list) {
        if (list != null && list.size() > 0) {
            for (TopModel model : list) {
                if (TextUtils.equals(targetId, model.TID)) {
                    return model.Order;
                }
            }
        }
        return 0;
    }

    private ConversationItem getConversationByUserId(ConversationItem conversation, String targetId) {
        UserInfoDao dao = UserInfoDao.getInstance();
        UserInfo user = dao.selectWithId(targetId);
        if (user == null) {
            ContactsManager.getInstance().getUserInfo(targetId, 0, new RequestHandler.RequestListener<GetUserResponse>() {
                @Override
                public void onResponse(GetUserResponse response, Bundle bundle) {
                    if (response.User != null && !TextUtils.isEmpty(response.User.UserID)) {
                        ContactsManager.getInstance().saveContactsToDB(response.User);
                        EventBusManager.getInstance().post(ConversationEvent.getInstance());
                    }
                }
            });
            user = new UserInfo();
        }
        conversation.title = user.Title;
        conversation.imagePath = user.Image;
        conversation.name = user.Name;
        conversation.chatInfo = user;
        conversation.targetId = user.UserID;
        return conversation;
    }

    private ConversationItem getConversationByGroupId(ConversationItem conversation, String groupId) {
        GroupInfoDao dao = GroupInfoDao.getInstance();
        GroupInfo group = dao.selectWithId(groupId);
        if (group == null) {
            ContactsManager.getInstance().getGroupInfo(groupId, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                @Override
                public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                    if (response.Group != null) {
                        EventBusManager.getInstance().post(ConversationEvent.getInstance());
                    }
                }
            });
            group = new GroupInfo();
        }
        conversation.groupId = groupId;
        conversation.title = "";
        conversation.imagePath = "";
        conversation.name = group.GroupName;
        conversation.chatInfo = group;
        conversation.targetId = groupId;
        return conversation;
    }

    public void setConversationTopChat(String targetId, int targetType, boolean isTop) {
        if (isTop) {
            TopChatManager.getInstance().addTop(targetId, targetType);
        } else {
            TopChatManager.getInstance().removeTop(targetId);
        }
        ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
        event.setRefreshId(targetId);
        EventBusManager.getInstance().post(event);
        TopChatManager.getInstance().requestSetChatOrder(null, TopChatManager.getInstance().getTopModelList(), null);
    }

    public void deleteConversationByGroupId(String targetId) {
        MessageDao.getInstance().removeConversation(targetId);
        EventBusManager.getInstance().post(ConversationEvent.getInstance());
    }

    public void removeAllChatHistory(final CommonLoadingListener<Integer> listener) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return MessageDao.getInstance().removeAllConversation();
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (listener != null) {
                    listener.onResponse(integer);
                }
                EventBusManager.getInstance().post(ConversationEvent.getInstance());
            }
        }.execute();
    }

    public void removeConversation(final String targetId, final CommonLoadingListener<Integer> listener) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return MessageDao.getInstance().removeConversation(targetId);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (listener != null) {
                    listener.onResponse(integer);
                }
                EventBusManager.getInstance().post(ConversationEvent.getInstance());
            }
        }.execute();
    }

    public void setConversationRead(String targetId) {
        if (!TextUtils.isEmpty(targetId)) {
            Observable.just(targetId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String targetId) {
                            MessageDao.getInstance().setConversationRead(targetId);
                            ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
                            event.setRefreshId(targetId);
                            EventBusManager.getInstance().post(event);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    private void setConversationUnread(String targetId) {
        if (!TextUtils.isEmpty(targetId)) {
            Observable.just(targetId).observeOn(Schedulers.io())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String targetId) {
//                            MessageDao.getInstance().setConversationUnread(targetId);
                            ConversationDao.getInstance().setReadState(targetId, false);
                            ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
                            event.setRefreshId(targetId);
                            EventBusManager.getInstance().post(event);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public void setConversationReadState(String targetId, boolean unread) {
        if (unread) {
            MessageManager.getInstance().setSingleMessageReaded(targetId);
            MessageManager.getInstance().setGroupMessageReaded(targetId);
            modifyConversationRead(targetId);
            setConversationRead(targetId);
        } else {
            setConversationUnread(targetId);
        }
    }

    public void modifyConversationRead(String targetId) {
        if (!TextUtils.isEmpty(targetId)) {
            Observable.just(targetId)
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String targetId) {
                            ConversationDao.getInstance().setReadState(targetId, true);
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public void loadAllUnreadCount(final CommonLoadingListener<Integer> listener) {
        Observable.just(0)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        List<String> disturbList = new ArrayList<>();
                        SettingsModel settings = UserSettingManager.getInstance().getMySettings();
                        if (settings != null) {
                            if (settings.DisturbUsers != null && settings.DisturbUsers.size() > 0) {
                                for (String userId : settings.DisturbUsers) {
                                    disturbList.add(userId);
                                }
                            }
                            if (settings.DisturbGroups != null && settings.DisturbGroups.size() > 0) {
                                for (String groupId : settings.DisturbGroups) {
                                    disturbList.add(groupId);
                                }
                            }
                        }
                        List<MessageModel> list = MessageDao.getInstance().getAllUnreadMessage(disturbList);
                        List<String> idList = new ArrayList<>();
                        int len = 0;
                        if (list != null && list.size() > 0) {
                            len = list.size();
                            for (MessageModel model : list) {
                                if (!idList.contains(model.targetId)) {
                                    idList.add(model.targetId);
                                }
                            }
                        }
                        if (disturbList.size() > 0) {
                            idList.addAll(disturbList);
                        }
                        int unreadConversation = ConversationDao.getInstance().queryUnreadConversationCount(idList);
                        return len + unreadConversation;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        listener.onResponse(integer);
                    }
                }, CommonUtil.ACTION_EXCEPTION);

    }

    public void updateMessageConversation(final String targetId, final String groupId, List<MessageModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        Observable.from(list).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .last()
                .map(new Func1<MessageModel, Long>() {
                    @Override
                    public Long call(MessageModel messageModel) {
                        return messageModel.CreateTime;
                    }
                })
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        ConversationDao.getInstance().createConversation(targetId, groupId, aLong);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public void setDraft(String targetId, String draft) {
        ConversationDao.getInstance().setDraft(targetId, draft);
    }

    public void loadDraft(final String targetId, final CommonLoadingListener<String> listener) {
        if (!TextUtils.isEmpty(targetId)) {
            Observable.just(targetId)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            return ConversationDao.getInstance().queryConversationDraft(targetId);
                        }
                    })
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String draft) {
                            return !TextUtils.isEmpty(draft);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String draft) {
                            if (listener != null) {
                                listener.onResponse(draft);
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }
}
