package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.beans.MessageModelByPage;
import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateGroupAnnoucementReadStatusEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateMessageReadStatusEvent;
import com.itutorgroup.tutorchat.phone.domain.inter.ChatType;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.response.CheckIsReadResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.manager.PushInfoManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.salesuite.saf.utils.Preconditions;

/**
 * Created by joyinzhao on 2016/8/30.
 */
public class MessageDao implements DBHelper.IDBUpdateListener {
    private DBHelper mHelper;
    private Dao<MessageModel, Integer> mDao;

    private MessageDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(MessageModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MessageDao sInstance;

    public static MessageDao getInstance() {
        if (sInstance == null) {
            synchronized (MessageDao.class) {
                if (sInstance == null) {
                    sInstance = new MessageDao();
                }
            }
        }
        return sInstance;
    }

    public void add(MessageModel message) {
        try {
            String targetId = ConversationManager.getInstance().getMessageTargetId(message);
            message.targetId = targetId;
            message.IsReceiveAndRead = 0;
            mDao.createOrUpdate(message);
            ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
            event.setRefreshId(targetId);
            EventBusManager.getInstance().post(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateMessageSendStatus(String messageId, int messageSendStatus) {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("MessageSendStatus", messageSendStatus).where().in("MessageID", messageId).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCancelMessage(String messageId, String content, int messageSendStatus) {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            SelectArg arg = new SelectArg(content);
            updateBuilder.updateColumnValue("Content", arg);
            updateBuilder.updateColumnValue("MessageSendStatus", messageSendStatus);
            updateBuilder.updateColumnValue("Type", MessageType.WITH_DRAWAL);
            updateBuilder.where().eq("MessageID", messageId).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addItem(MessageModel message) {
        try {
            String targetId = ConversationManager.getInstance().getMessageTargetId(message);
            message.targetId = targetId;
            message.IsReceiveAndRead = 0;
            mDao.createOrUpdate(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Collection<MessageModel> collection) {
        try {
            if (collection != null) {
                for (MessageModel model : collection) {
                    addItem(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MessageModel> selectChatList() {
        return selectChatList(null);
    }

    public List<MessageModel> selectChatList(List<String> targetIdList) {
        List<MessageModel> list = new ArrayList<>();
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            if (targetIdList == null || targetIdList.size() == 0) {
                targetIdList = new ArrayList<>();
                List<MessageModel> tmp = mDao.queryBuilder()
                        .selectColumns("targetId")
                        .groupBy("targetId").distinct()
                        .orderBy("CreateTime", false)
                        .where().eq("currentUserId", currentUserId)
                        .query();
                if (tmp != null && tmp.size() > 0) {
                    for (MessageModel model : tmp) {
                        if (!TextUtils.equals(model.targetId, currentUserId)) {
                            targetIdList.add(model.targetId);
                        }
                    }
                }
            }
            if (targetIdList.size() > 0) {
                for (String id : targetIdList) {
                    MessageModel model = queryLastMessageOfTarget(id);
                    if (model != null) {
                        list.add(model);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return list;
    }

    public MessageModel queryLastMessageOfTarget(String targetId) {
        MessageModel model = null;
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) && !TextUtils.isEmpty(targetId)) {
            return null;
        }
        try {
            model = mDao.queryBuilder()
                    .orderBy("CreateTime", false).limit(1l)
                    .where().eq("currentUserId", currentUserId)
                    .and().eq("targetId", targetId)
                    .queryForFirst();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return model;
    }

    public boolean hasUnreadAnnoucementMessageWithTargetId(String groupId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(groupId)) {
            return false;
        }
        try {
            QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
            Where<MessageModel, Integer> where = queryBuilder.where();
            where.eq("currentUserId", currentUserId)
                    .and().eq("GroupId", groupId)
                    .and().eq("GroupAnnouncementIsRead", 0)
                    .and().ne("PosterID", currentUserId)
                    .and().eq("Type", MessageType.GROUPANNOUNCEMENT);
            List<MessageModel> list = queryBuilder.query();
            return list != null && list.size() > 0;
        } catch (Exception e) {

        }
        return false;
    }

    public boolean hasAltMeMessageWithTargetId(String targetId) {
        List list = getHasAltMeMessageWithTargetId(targetId);
        return list != null && list.size() > 0;
    }

    public int removeConversation(String targetId) {
        try {
            DeleteBuilder deleteBuilder = mDao.deleteBuilder();
            Where where = deleteBuilder.where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId())
                    .and().eq("targetId", targetId);
            deleteBuilder.setWhere(where);
            return deleteBuilder.delete();
        } catch (Exception e) {

        }
        return -1;
    }

    public int removeAllConversation() {
        try {
            DeleteBuilder deleteBuilder = mDao.deleteBuilder();
            Where where = deleteBuilder.where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            deleteBuilder.setWhere(where);
            return deleteBuilder.delete();
        } catch (Exception e) {

        }
        return -1;
    }

    public List<MessageModel> searchByKey(String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(key)) {
            return null;
        }
        List<MessageModel> list = null;
        try {
            list = mDao.queryBuilder().orderBy("CreateTime", false).groupBy("targetId").where()
                    .eq("currentUserId", currentUserId)
                    .and().like("Content", "%" + key + "%")
                    .and().ne("targetType", ChatType.SERVICE_ACCOUNT)
                    .and().in("Type", MessageType.TEXT, MessageType.GROUPANNOUNCEMENT).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MessageModel> searchTargetMessageByKey(String target, String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(target) || TextUtils.isEmpty(key)) {
            return null;
        }
        List<MessageModel> list = null;
        try {
            QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", false);
            queryBuilder.where()
                    .eq("currentUserId", currentUserId)
                    .and().eq("targetId", target)
                    .and().like("Content", "%" + key + "%")
                    .and().in("Type", MessageType.TEXT, MessageType.GROUPANNOUNCEMENT);
            list = queryBuilder.query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<MessageModel> queryMessageByPage(String targetId, long page) throws Exception {
        if (TextUtils.isEmpty(targetId)) {
            return null;
        }
        List<MessageModel> list = null;
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
        queryBuilder.where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId())
                .and().eq("targetId", targetId);
        long tmp = queryBuilder.countOf() - page * Constant.PAGE_NUMBER;
        if (tmp <= 0) {
            if (queryBuilder.countOf() - (page - 1) * Constant.PAGE_NUMBER > 0) {
                list = queryBuilder.limit(queryBuilder.countOf() - (page - 1) * Constant.PAGE_NUMBER).query();
            }
        } else {
            list = queryBuilder.offset(queryBuilder.countOf() - page * Constant.PAGE_NUMBER).limit(Constant.PAGE_NUMBER).query();
        }
        return list;
    }

    public List<MessageModel> getHasAltMeMessageWithTargetId(String targetId) {
        try {
            String currentUserId = AccountManager.getInstance().getCurrentUserId();
            if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
                return null;
            }
            List<MessageModel> list = mDao.queryBuilder()
                    .orderBy("CreateTime", false)
                    .where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId())
                    .and().ne("PosterID", currentUserId)
                    .and().eq("targetId", targetId)
                    .and().ne("Type", MessageType.GROUPANNOUNCEMENT)
                    .and().eq("IsReceiveAndRead", 0)
                    .query();
            List<MessageModel> resultList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (MessageModel model : list) {
                    if (model.AltReceivers != null && model.AltReceivers.size() > 0 && model.AltReceivers.contains(currentUserId)) {
                        resultList.add(model);
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<MessageModel> queryGroupMessageByPage(String fromId, String groupId, long page) throws Exception {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(groupId)) {
            return null;
        }
        List<MessageModel> list = null;
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
        Where<MessageModel, Integer> where = queryBuilder.where();
        where.eq("currentUserId", fromId);
        where.and();
        where.eq("GroupId", groupId).and().isNotNull("GroupId");
        long tmp = queryBuilder.countOf() - page * Constant.PAGE_NUMBER;
        if (tmp <= 0) {
            list = queryBuilder.limit(queryBuilder.countOf() - (page - 1) * Constant.PAGE_NUMBER).query();
        } else {
            list = queryBuilder.offset(queryBuilder.countOf() - page * Constant.PAGE_NUMBER).limit(Constant.PAGE_NUMBER).query();
        }
        return list;
    }


    public List<MessageModel> queryGroupAnnoucementByPage(String fromId, String groupId, long page) throws Exception {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(groupId)) {
            return null;
        }
        List<MessageModel> list = null;
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
        Where<MessageModel, Integer> where = queryBuilder.where();
        where.eq("currentUserId", fromId);
        where.and();
        where.eq("GroupId", groupId).and().isNotNull("GroupId").and().eq("Type", MessageType.GROUPANNOUNCEMENT);
        long tmp = queryBuilder.countOf() - page * Constant.PAGE_NUMBER;
        if (tmp <= 0) {
            list = queryBuilder.limit(queryBuilder.countOf() - (page - 1) * Constant.PAGE_NUMBER).query();
        } else {
            list = queryBuilder.offset(queryBuilder.countOf() - page * Constant.PAGE_NUMBER).limit(Constant.PAGE_NUMBER).query();
        }
        return list;
    }


    public List<MessageModel> queryGroupAnnoucementListByPage(String fromId, String groupId, boolean isRead) throws Exception {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(groupId)) {
            return null;
        }
        List<MessageModel> list = null;
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", false);
        Where<MessageModel, Integer> where = queryBuilder.where();
        where.eq("currentUserId", fromId);
        where.and();
        where.eq("GroupId", groupId).and().isNotNull("GroupId").and().eq("Type", MessageType.GROUPANNOUNCEMENT);
        if (isRead) {
            where.and().eq("GroupAnnouncementIsRead", 1);
        } else {
            where.and().eq("GroupAnnouncementIsRead", 0);
        }
        return queryBuilder.query();
    }


    /*查询未读公告数量*/
    public long queryAnnoucementUnReadCount(String fromId, String groupId) throws Exception {
        if (TextUtils.isEmpty(fromId) || TextUtils.isEmpty(groupId)) {
            return -1;
        }
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
        Where<MessageModel, Integer> where = queryBuilder.where();
        where.eq("currentUserId", fromId)
                .and().eq("GroupId", groupId)
                .and().isNotNull("GroupId")
                .and().eq("Type", MessageType.GROUPANNOUNCEMENT)
                .and().eq("IsSelf", 0)
                .and().eq("GroupAnnouncementIsRead", 0);
        return queryBuilder.countOf();
    }

    public List<String> queryUnReadMessageIDS(String fromId, String receiverId) throws Exception {

        Where where = mDao.queryBuilder().distinct().selectColumns("MessageID").orderBy("CreateTime", true).where();
        GenericRawResults<String[]> strings = where.and(
                where.and(
                        where.eq("PosterID", fromId),
                        where.eq("ReceiverID", receiverId)
                ),
                where.or(
                        where.eq("GroupId", ""),
                        where.isNull("GroupId")
                ),
                where.eq("isRead", 0)
        ).queryRaw();

        List<String> unreadMessageIds = new ArrayList<>();
        for (String[] resultColumns : strings) {
            String unreadMessageId = resultColumns[0];
            unreadMessageIds.add(unreadMessageId);
        }
        return unreadMessageIds;
    }


    /**
     * 查询messages的receiverMessageIdS,可以包含 voice type
     *
     * @param fromId
     * @param receiverId
     * @param IsRecipetType
     * @return
     * @throws Exception
     */
    public List<String> queryReceiveMessageIDS(String fromId, String receiverId, int IsRecipetType) throws Exception {

        Where where = mDao.queryBuilder().distinct().selectColumns("ReceiverMessageID").orderBy("CreateTime", true).where();
        GenericRawResults<String[]> strings = where.and(
                where.eq("targetId", receiverId)
                        .and().ne("PosterID", fromId)
                        .and().eq("currentUserId", fromId)
                        .and().ne("Type", MessageType.WITH_DRAWAL)
                        .and().ne("Type", MessageType.SYSTEM_MESSAGE)
                        .and().ne("Type", MessageType.SYSTEM_NOTICE)
                        .and().ne("Type", MessageType.GROUPANNOUNCEMENT)
                        .and().eq("IsReceiveAndRead", 0),
                where.eq("IsReceipt", IsRecipetType)
        ).queryRaw();

        List<String> unreadMessageIds = new ArrayList<>();
        for (String[] resultColumns : strings) {
            String unreadMessageId = resultColumns[0];
            unreadMessageIds.add(unreadMessageId);
        }
        return unreadMessageIds;
    }

    /**
     * 群组中我发送的消息的状态
     *
     * @param fromId
     * @param GroupId
     * @return
     * @throws Exception
     */
    public List<String> queryUnReadMessageIDSFromGroup(String fromId, String GroupId, String columnName, boolean isfilterGroupAnnoucement) throws Exception {
        Where where = mDao.queryBuilder().distinct().selectColumns(columnName).orderBy("CreateTime", true).where();
        GenericRawResults<String[]> strings = null;
        if (isfilterGroupAnnoucement) {
            strings =
                    where.and(
                            where.eq("currentUserId", fromId),
                            where.eq("GroupId", GroupId),
                            where.gt("GroupUnReadNumbers", 0),// 未读数目>0 的集合
                            where.isNotNull("GroupId"),
                            where.ne("Type", MessageType.GROUPANNOUNCEMENT) //筛选出公告的消息
                    ).queryRaw();
        } else {
            strings =
                    where.and(
                            where.eq("currentUserId", fromId),
                            where.eq("GroupId", GroupId),
                            where.gt("GroupUnReadNumbers", 0),// 未读数目>0 的集合
                            where.isNotNull("GroupId")
                    ).queryRaw();
        }
        List<String> unreadMessageIds = new ArrayList<>();
        for (String[] resultColumns : strings) {
            String unreadMessageId = resultColumns[0];
            unreadMessageIds.add(unreadMessageId);
        }
        return unreadMessageIds;
    }

    /**
     * 查询发给我的消息中我未读的receiverMessageIDs,不需要过滤语音
     */
    public List<String> queryUnReadReceiverMessageIDSFromGroup(String fromId, String GroupId, int type) throws Exception {
        Where where = mDao.queryBuilder().distinct().selectColumns("ReceiverMessageID").orderBy("CreateTime", true).where();
        GenericRawResults<String[]> strings = null;
        strings =
                where.and(
                        where.eq("currentUserId", fromId),
                        where.eq("GroupId", GroupId),
                        where.gt("IsReceiveAndRead", 0),
                        where.eq("IsSelf", 2),
                        where.eq("IsReceipt", type),
                        where.isNotNull("GroupId")
                ).queryRaw();

        List<String> unreadMessageIds = new ArrayList<>();
        for (String[] resultColumns : strings) {
            String unreadMessageId = resultColumns[0];
            unreadMessageIds.add(unreadMessageId);
        }
        return unreadMessageIds;
    }


    public List<String> queryUnReadReceiverMessageIDSFromGroup(String fromId, String GroupId) throws Exception {
        Where where = mDao.queryBuilder().distinct().selectColumns("ReceiverMessageID").orderBy("CreateTime", true).where();
        GenericRawResults<String[]> strings = null;
        strings =
                where.and(
                        where.eq("currentUserId", fromId),
                        where.eq("GroupId", GroupId),
                        where.gt("IsRead", 0),
                        where.eq("IsSelf", 2),
                        where.isNotNull("GroupId")
                ).queryRaw();

        List<String> unreadMessageIds = new ArrayList<>();
        for (String[] resultColumns : strings) {
            String unreadMessageId = resultColumns[0];
            unreadMessageIds.add(unreadMessageId);
        }
        return unreadMessageIds;
    }


    public void updateReceiverGroupReadMessage(List<String> ReceiverMessageID) throws Exception {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        updateBuilder.updateColumnValue("IsRead", 1).where().in("ReceiverMessageID", ReceiverMessageID);
        updateBuilder.update();
    }


    public List<MessageModel> queryMessagesWithId(ArrayList<String> idList) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || idList == null || idList.isEmpty()) {
            return null;
        }
        List<MessageModel> list = null;
        try {
            list = mDao.queryBuilder().where().eq("currentUserId", currentUserId).and().in("MessageID", idList).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public void updateReadMessage(List<String> hasReadMessageIDS) throws Exception {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        updateBuilder.updateColumnValue("IsRead", 1).where().in("MessageID", hasReadMessageIDS);
        updateBuilder.update();
        EventBusManager.getInstance().post(new UpdateMessageReadStatusEvent(hasReadMessageIDS, true));
    }

    public void updateGroupReadMessage(List<CheckIsReadResponse.ReadModel> readModels) throws Exception {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        /**
         * 查询readmoels对应的数据库群消息未读数目有没有发生变化，如果没有变化，直接return
         */
        if (GroupReadMessageIsChange(readModels)) {
            for (CheckIsReadResponse.ReadModel readModel : readModels) {
                updateBuilder.updateColumnValue("GroupUnReadNumbers", readModel.UserNum - readModel.ReadCount).where().in("MessageID", readModel.MessageID);
                updateBuilder.update();
            }
            EventBusManager.getInstance().post(new UpdateMessageReadStatusEvent(readModels));
        }
    }

    public void updateGroupAnnouncementReadMessage(MessageModel messageModel) throws Exception {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        updateBuilder.updateColumnValue("GroupAnnouncementIsRead", 1).where().eq("MessageID", messageModel.MessageID);
        updateBuilder.update();
        EventBusManager.getInstance().post(UpdateGroupAnnoucementReadStatusEvent.getInstance());
        EventBusManager.getInstance().post(UpdateMessageReadStatusEvent.getInstance());
    }

    public boolean CheckGroupAnnouncementIsReadMessage(MessageModel messageModel) throws Exception {

        List<MessageModel> messageModelList = mDao.queryBuilder().where().eq("MessageID", messageModel.MessageID).query();
        if (Preconditions.isNotBlank(messageModelList)) {
            if (messageModelList.get(0).GroupAnnouncementIsRead == 1) {
                return true;
            }
        }
        return false;
    }

    public void setMessageRead(List<String> list) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (list == null || list.size() == 0 || TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            UpdateBuilder updateBuilder = mDao.updateBuilder();
            updateBuilder.updateColumnValue("IsReceiveAndRead", 1);
            updateBuilder.updateColumnValue("GroupAnnouncementIsRead", 1);
            updateBuilder.where().eq("currentUserId", currentUserId).and().ne("PosterID", currentUserId).and().in("MessageID", list);
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.exception(e);
        }
        return;
    }

    /**
     * 对方发过来的消息，当前用户已经查看过。
     */
    public void setConversationRead(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            UpdateBuilder updateBuilder = mDao.updateBuilder();
            updateBuilder.updateColumnValue("IsReceiveAndRead", 1)
                    .where().eq("targetId", targetId)
                    .and().eq("IsReceiveAndRead", 0)
                    .and().eq("currentUserId", currentUserId);
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getConversationUnreadMessageCount(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return 0;
        }
        int count = 0;
        try {
            Where where = mDao.queryBuilder().where();
            where.and(
                    where.eq("targetId", targetId)
                            .and().ne("PosterID", currentUserId)
                            .and().eq("currentUserId", currentUserId)
                            .and().ne("Type", MessageType.WITH_DRAWAL)
                            .and().ne("Type", MessageType.SYSTEM_MESSAGE)
                            .and().ne("Type", MessageType.SYSTEM_NOTICE),
                    where.or(
                            where.and(
                                    where.eq("IsReceiveAndRead", 0),
                                    where.ne("Type", MessageType.GROUPANNOUNCEMENT)
                            ),
                            where.and(
                                    where.eq("GroupAnnouncementIsRead", 0),
                                    where.eq("Type", MessageType.GROUPANNOUNCEMENT)
                            )
                    )
            );
            count = where.query().size();
//            count = mDao.queryBuilder().where().eq("targetId", targetId).and().ne("PosterID", currentUserId).and().eq("IsReceiveAndRead", 0).and().eq("currentUserId", currentUserId).query().size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    public boolean canShowReadState(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId)
                || TextUtils.isEmpty(currentUserId)
                || hasUnreadAnnoucementMessageWithTargetId(targetId)
                || hasAltMeMessageWithTargetId(targetId)) {
            return false;
        }
        return true;
    }

    public List<MessageModel> getAllUnreadMessage(List<String> disturbList) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            Where where = mDao.queryBuilder().where();
            where.and(
                    where.ne("PosterID", currentUserId)
                            .and().ne("PosterID", currentUserId)
                            .and().eq("currentUserId", currentUserId)
                            .and().ne("Type", MessageType.WITH_DRAWAL)
                            .and().ne("Type", MessageType.SYSTEM_MESSAGE)
                            .and().ne("Type", MessageType.SYSTEM_NOTICE),
                    where.or(
                            where.and(
                                    where.eq("IsReceiveAndRead", 0),
                                    where.ne("Type", MessageType.GROUPANNOUNCEMENT)
                            ),
                            where.and(
                                    where.eq("GroupAnnouncementIsRead", 0),
                                    where.eq("Type", MessageType.GROUPANNOUNCEMENT)
                            )
                    )
            );
            if (disturbList != null && disturbList.size() > 0) {
                where.and().notIn("targetId", disturbList);
            }
            return where.query();
//            count = mDao.queryBuilder().where().ne("PosterID", currentUserId).and().eq("IsReceiveAndRead", 0).and().eq("currentUserId", currentUserId).query().size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public MessageModelByPage getMessagesByMessageId(String targetId, String messageId) throws Exception {
        QueryBuilder queryBuilder = mDao.queryBuilder().orderBy("CreateTime", true);
        queryBuilder.where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId()).and().eq("targetId", targetId);
        int totalCount = (int) queryBuilder.countOf();
        int totalPage = totalCount / Constant.PAGE_NUMBER;
        if (totalCount % Constant.PAGE_NUMBER > 0) {
            totalPage++;
        }
        List<MessageModel> messageModelList = new ArrayList<>();
        for (int page = 1; page <= totalPage; page++) {
            List<MessageModel> messageDaoList = queryMessageByPage(targetId, page);
            messageModelList.addAll(0, messageDaoList);
            for (int i = 0; i < messageDaoList.size(); i++) {
                if (messageDaoList.get(i).MessageID.equals(messageId)) {
                    MessageModelByPage messageModelByPage = new MessageModelByPage();
                    messageModelByPage.page = page;
                    messageModelByPage.list = messageModelList;
                    messageModelByPage.index = i + 1;
                    return messageModelByPage;
                }
            }
        }
        return null;
    }


    public MessageModel SelectMessageModelByMessageId(String MessageID) throws Exception {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(MessageID)) {
            return null;
        }
        return mDao.queryBuilder().distinct().where().eq("MessageID", MessageID).and().eq("currentUserId", currentUserId).queryForFirst();
    }

    private boolean GroupReadMessageIsChange(List<CheckIsReadResponse.ReadModel> readModels) throws Exception {

        if (Preconditions.isBlank(readModels))
            return false;
        for (CheckIsReadResponse.ReadModel readModel : readModels) {
            MessageModel messageModel = SelectMessageModelByMessageId(readModel.MessageID);
            if (messageModel != null && messageModel.GroupUnReadNumbers != (readModel.UserNum - readModel.ReadCount)) {
                return true;
            }
        }
        return false;

    }


    /**
     * message fail -> success
     *
     * @param oldMessageId
     */
    public void RemoveMessageById(String oldMessageId) {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        Where where = null;
        try {
            where = deleteBuilder.where().eq("currentUserId", AccountManager.getInstance().getCurrentUserId())
                    .and().eq("MessageId", oldMessageId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void UpdateMessageId(String oldMessageId, String messageId, int messageSendStatus, int IsHavePermissionAccessReadStatus,long createTime) {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("MessageID", messageId);
            updateBuilder.updateColumnValue("MessageSendStatus", messageSendStatus);
            updateBuilder.updateColumnValue("IsHavePermissionAccessReadStatus", IsHavePermissionAccessReadStatus);
            updateBuilder.updateColumnValue("CreateTime", createTime);
            updateBuilder.where().eq("MessageID", oldMessageId).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void UpdateVoiceTime(int time, String messageId) {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("VoiceTime", (float) time).where().eq("MessageID", messageId).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateVoiceReadStatus(String messageId) {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("GroupAnnouncementIsRead", 1).where().eq("MessageID", messageId).and().eq("GroupAnnouncementIsRead", 0).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMessageType(List<String> messageList) {
        if (Preconditions.isBlank(messageList))
            return;
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("Type", MessageType.WITH_DRAWAL);
            updateBuilder.updateColumnValue("Content", "");
            updateBuilder.where().in("MessageID", messageList).and().eq("currentUserId", AccountManager.getInstance().getCurrentUserId());
            updateBuilder.update();
            PushInfoManager.getInstance().refreshNotificationStatus(messageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<MessageModel> getAllUnreadMessagesByTargetID(String targetID) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(targetID)) {
            return null;
        }
        List<MessageModel> messageModels = null;
        try {
            Where where = mDao.queryBuilder().where();
            where.and(
                    where.eq("targetId", targetID)
                            .and().ne("PosterID", currentUserId)
                            .and().eq("currentUserId", currentUserId)
                            .and().ne("Type", MessageType.WITH_DRAWAL)
                            .and().ne("Type", MessageType.SYSTEM_MESSAGE)
                            .and().ne("Type", MessageType.SYSTEM_NOTICE),
                    where.or(
                            where.and(
                                    where.eq("IsReceiveAndRead", 0),
                                    where.ne("Type", MessageType.GROUPANNOUNCEMENT)
                            ),
                            where.and(
                                    where.eq("GroupAnnouncementIsRead", 0),
                                    where.eq("Type", MessageType.GROUPANNOUNCEMENT)
                            )
                    )
            );
            messageModels = where.query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageModels;
    }


    @Override
    public void onTableUpdate(int oldVersion, int newVersion) {
        try {
            String sqlV6 = "ALTER TABLE `single_messages` ADD COLUMN MessageSendStatus int DEFAULT 0;";
            String sqlV7 = "ALTER TABLE `single_messages` ADD COLUMN LocalId STRING;";
            String sqlV8 = "ALTER TABLE `single_messages` ADD COLUMN VoiceTime float DEFAULT 0;";
            String sqlV13 = "ALTER TABLE `single_messages` ADD COLUMN IsReceipt int DEFAULT 0;";
            String sqlV13_1 = "ALTER TABLE `single_messages` ADD COLUMN IsHavePermissionAccessReadStatus int DEFAULT 0;";
            String sqlV17 = "ALTER TABLE `single_messages` ADD COLUMN targetType int DEFAULT 0;";
            DBHelper.getInstance().updateTable(mDao, oldVersion, 6, sqlV6);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 7, sqlV7);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 8, sqlV8);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 13, sqlV13);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 13, sqlV13_1);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 17, sqlV17);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
