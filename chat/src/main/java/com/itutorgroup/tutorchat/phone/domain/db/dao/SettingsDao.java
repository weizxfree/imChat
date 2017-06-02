package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

/**
 * Created by joyinzhao on 2016/11/15.
 */
public class SettingsDao implements DBHelper.IDBUpdateListener {
    private static final String TAG = SettingsDao.class.getSimpleName();

    private static SettingsDao sInstance;

    private DBHelper mHelper;
    Dao<SettingsModel, Integer> mDao;

    public static SettingsDao getInstance() {
        if (sInstance == null) {
            synchronized (SettingsDao.class) {
                if (sInstance == null) {
                    sInstance = new SettingsDao();
                }
            }
        }
        return sInstance;
    }

    private SettingsDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(SettingsModel.class);
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    @Override
    public void onTableUpdate(int oldVersion, int newVersion) {

    }

    public void add(SettingsModel model) {
        try {
            mDao.createOrUpdate(model);
        } catch (Exception e) {
            LogUtil.exception(e);
            LogUtil.e(TAG, "create or update error: " + model);
        }
    }

    public void setNewsNoticeDisturb(int flag) {
        setMySettingsItemEnable("NewsNoticed", flag);
    }

    public void setIsDisturb(int flag) {
        setMySettingsItemEnable("IsDisturb", flag);
    }

    private void setMySettingsItemEnable(String columnName, int enable) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        try {
            UpdateBuilder builder = mDao.updateBuilder();
            builder.updateColumnValue(columnName, enable);
            builder.where().eq("UserId", currentUserId);
            builder.update();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }

    public SettingsModel query() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        try {
            return mDao.queryBuilder().where().eq("UserId", currentUserId).queryForFirst();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

}
