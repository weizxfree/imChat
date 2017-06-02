package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.util.List;

/**
 * Created by joyinzhao on 2016/11/25.
 */
public class SystemNoticeDao {
    private static SystemNoticeDao sInstance;
    private DBHelper mHelper;
    Dao<SystemNoticeModel, Integer> mDao;

    public static SystemNoticeDao getInstance() {
        if (sInstance == null) {
            synchronized (SystemNoticeDao.class) {
                if (sInstance == null) {
                    sInstance = new SystemNoticeDao();
                }
            }
        }
        return sInstance;
    }

    private SystemNoticeDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(SystemNoticeModel.class);
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    public void add(SystemNoticeModel model) {
        try {
            String currentUserId = AccountManager.getInstance().getCurrentUserId();
            if (model != null && !TextUtils.isEmpty(currentUserId)) {
                model.currentUserId = currentUserId;
                model.id = currentUserId + model.SystemNoticeId;
                mDao.createOrUpdate(model);
            }
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    public SystemNoticeModel getActiveSystemNotice() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            long time = System.currentTimeMillis();
            SystemNoticeModel model = mDao.queryBuilder().orderBy("LastModifiedTime", false).limit(1l)
                    .where().eq("currentUserId", currentUserId)
                    .and().gt("ExpiredTime", time)
                    .and().eq("Cate", 1)
                    .queryForFirst();
            return model;
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public List<SystemNoticeModel> querySystemNoticeHistory() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            return mDao.queryBuilder().orderBy("LastModifiedTime", false)
                    .where().eq("currentUserId", currentUserId)
                    .query();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public void remove(SystemNoticeModel model) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || model == null) {
            return;
        }
        try {
            DeleteBuilder builder = mDao.deleteBuilder();
            builder.where().eq("currentUserId", currentUserId).and().eq("SystemNoticeId", model.SystemNoticeId);
            builder.delete();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }
}
