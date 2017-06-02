package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.ConversationModel;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.List;

/**
 * Created by joyinzhao on 2016/9/18.
 */
public class ConversationDao implements DBHelper.IDBUpdateListener {
    private DBHelper mHelper;
    private Dao<ConversationModel, Integer> mDao;
    private static ConversationDao sInstance;

    public static ConversationDao getInstance() {
        if (sInstance == null) {
            synchronized (ConversationDao.class) {
                if (sInstance == null) {
                    sInstance = new ConversationDao();
                }
            }
        }
        return sInstance;
    }

    private ConversationDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(ConversationModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTableUpdate(int oldVersion, int newVersion) {
        try {
            mHelper.updateTable(mDao, oldVersion, 9, "ALTER TABLE `conversation` ADD COLUMN draft STRING;");
            mHelper.updateTable(mDao, oldVersion, 15, "ALTER TABLE `conversation` ADD COLUMN unread int DEFAULT 0;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createConversation(String targetId, String groupId, long time) {
        try {
            ConversationModel model = queryConversation(targetId);
            if (model == null) {
                model = new ConversationModel();
            }
            model.CreateTime = time;
            model.GroupId = groupId;
            model.CurrentUserId = AccountManager.getInstance().getCurrentUserId();
            model.targetID = targetId;
            model.Id = model.CurrentUserId + model.targetID;
            mDao.createOrUpdate(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ConversationModel> queryAll(List<String> idList) {
        List<ConversationModel> list = null;
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            Where where = mDao.queryBuilder()
                    .orderBy("CreateTime", false)
                    .groupBy("targetId")
                    .distinct()
                    .where().eq("CurrentUserId", currentUserId);
            if (idList != null && idList.size() > 0) {
                where.and().in("targetID", idList);
            }
            list = where.query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void removeAll() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            DeleteBuilder deleteBuilder = mDao.deleteBuilder();
            Where where = deleteBuilder.where().eq("CurrentUserId", currentUserId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {

        }
    }

    public void remove(String targetId) {
        try {
            DeleteBuilder deleteBuilder = mDao.deleteBuilder();
            Where where = deleteBuilder.where()
                    .eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId())
                    .and().eq("targetId", targetId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {

        }
    }

    public void setDraft(String targetId, String draft) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
            return;
        }
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("draft", draft).where().eq("CurrentUserId", currentUserId).and().eq("targetID", targetId);
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String queryConversationDraft(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        String draft = null;
        ConversationModel model = queryConversation(targetId);
        if (model != null) {
            draft = model.draft;
        }
        return draft;
    }

    public ConversationModel queryConversation(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            return mDao.queryBuilder().distinct().where().eq("CurrentUserId", currentUserId).and().eq("targetID", targetId).queryForFirst();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public int queryUnreadConversationCount(List<String> exceptList) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return 0;
        }
        try {
            QueryBuilder builder = mDao.queryBuilder();
            Where where = builder.distinct().where()
                    .eq("CurrentUserId", currentUserId).and().eq("unread", 1);
            if (exceptList != null && exceptList.size() > 0) {
                where.and().notIn("targetID", exceptList);
            }
            return (int) where.countOf();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return 0;
    }

    public boolean canShowReadState(String targetId) {
        return MessageDao.getInstance().canShowReadState(targetId);
    }

    public int getUnreadCount(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId)
                || TextUtils.isEmpty(currentUserId)) {
            return 0;
        }
        int unreadMessageCount = MessageDao.getInstance().getConversationUnreadMessageCount(targetId);
        if (unreadMessageCount > 0) {
            return unreadMessageCount;
        }
        ConversationModel model = queryConversation(targetId);
        if (model != null && model.unread == 1) {
            return 1;
        }
        return 0;
    }

    public void setReadState(String targetId, boolean read) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            int unread = read ? 0 : 1;
            UpdateBuilder builder = mDao.updateBuilder();
            builder.updateColumnValue("unread", unread);
            builder.where().eq("CurrentUserId", currentUserId)
                    .and().eq("targetID", targetId)
                    .and().ne("unread", unread);
            builder.update();
        } catch (Exception e) {

        }
    }
}
