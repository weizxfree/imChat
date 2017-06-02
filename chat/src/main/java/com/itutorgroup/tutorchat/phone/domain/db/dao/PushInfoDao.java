package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.PushInfoModel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;


public class PushInfoDao {
    private DBHelper mHelper;
    private Dao<PushInfoModel, Integer> mDao;

    private static PushInfoDao sInstance;

    public static PushInfoDao getInstance() {
        if (sInstance == null) {
            synchronized (PushInfoDao.class) {
                if (sInstance == null) {
                    sInstance = new PushInfoDao();
                }
            }
        }
        return sInstance;
    }

    private PushInfoDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(PushInfoModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String GroupID) {
        PushInfoModel pushInfoModel = new PushInfoModel();
        pushInfoModel.TargetId = GroupID;
        pushInfoModel.CurrentUserId = AccountManager.getInstance().getCurrentUserId();
        try {
            mDao.createOrUpdate(pushInfoModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(final Collection<String> collection) {
        if (collection == null || collection.size() == 0) {
            return;
        }
        try {
            for (String id : collection) {
                add(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String targetId) {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        try {
            Where where = deleteBuilder.where().eq("TargetId", targetId).and().eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId());
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getUnreadCount(String targetId) {
        try {
            return mDao.queryBuilder().where().eq("TargetId", targetId).and().eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId()).countOf();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ArrayList<String> getAllPushInfo() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            List<PushInfoModel> list = mDao.queryBuilder().selectColumns("TargetId").distinct().query();
            if (list != null && list.size() > 0) {
                ArrayList<String> result = new ArrayList<>();
                for (PushInfoModel model : list) {
                    result.add(model.TargetId);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUnreadTotalCount() {
        try {
            return (int) mDao.queryBuilder().where().eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId()).countOf();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


}
