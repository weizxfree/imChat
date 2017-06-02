package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class TopDao {

    private DBHelper mHelper;
    private Dao<TopModel, Integer> mDao;

    private static TopDao sInstance;

    public static TopDao getInstance() {
        if (sInstance == null) {
            synchronized (TopDao.class) {
                if (sInstance == null) {
                    sInstance = new TopDao();
                }
            }
        }
        return sInstance;
    }

    private TopDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(TopModel model) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        model.currentUserId = currentUserId;
        model.id = currentUserId + model.TID;
        try {
            mDao.createOrUpdate(model);
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    public void add(final Collection<TopModel> collection) {
        if (collection != null && collection.size() > 0) {
            DBHelper.getInstance().doTransaction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (TopModel model : collection) {
                        add(model);
                    }
                    return null;
                }
            });
        }
    }

    public void reset(final Collection<TopModel> collection) {
        DBHelper.getInstance().doTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                remove(null);
                if (collection != null && collection.size() > 0) {
                    for (TopModel model : collection) {
                        add(model);
                    }
                }
                return null;
            }
        });
    }

    public List<TopModel> query() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            return mDao.queryBuilder().orderBy("Order", false).where().eq("currentUserId", currentUserId).query();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public int queryTopIndex() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return 0;
        }
        try {
            TopModel model = mDao.queryBuilder().orderBy("Order", false).limit(1).where().eq("currentUserId", currentUserId).queryForFirst();
            if (model != null) {
                return model.Order;
            }
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return 0;
    }

    public void remove(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            DeleteBuilder builder = mDao.deleteBuilder();
            Where where = builder.where();
            where.eq("currentUserId", currentUserId);
            if (!TextUtils.isEmpty(targetId)) {
                where.and().eq("TID", targetId);
            }
            builder.delete();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    public boolean isTop(String targetId) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(targetId)) {
            return false;
        }
        try {
            return mDao.queryBuilder().where().eq("currentUserId", currentUserId).and().eq("TID", targetId).countOf() > 0;
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return false;
    }

    public long getTopCount() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return 0;
        }
        try {
            return mDao.queryBuilder().where().eq("currentUserId", currentUserId).countOf();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return 0;
    }
}
