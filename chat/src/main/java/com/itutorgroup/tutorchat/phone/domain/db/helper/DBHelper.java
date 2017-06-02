package com.itutorgroup.tutorchat.phone.domain.db.helper;

import android.content.Context;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.ContactsConstraint;
import com.itutorgroup.tutorchat.phone.domain.db.model.ConversationModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInContactModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.PushInfoModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopChatModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserSetting;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/30.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "TutorChat.db";

    // increase the database version
    private static final int DATABASE_VERSION = 17;

    private Map<String, Dao> mDaoMap = new HashMap<>();

    private static DBHelper sInstance;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, Constant.DB_KEY);
    }

    public static DBHelper getInstance() {
        if (sInstance == null) {
            synchronized (DBHelper.class) {
                if (sInstance == null) {
                    removeOldData();
                    SQLiteDatabase.loadLibs(LPApp.getInstance());
                    sInstance = new DBHelper(LPApp.getInstance());
                }
            }
        }
        return sInstance;
    }

    private static void removeOldData() {
        Observable.just("dianchechengjin.db", "dianchechengjin.db-journal")
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, File>() {
                    @Override
                    public File call(String s) {
                        return LPApp.getInstance().getDatabasePath(s);
                    }
                })
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return file.exists();
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        ACache.get(LPApp.getInstance()).clear();
                        file.delete();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void createTableIfNotExists() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, MessageModel.class);
            TableUtils.createTableIfNotExists(connectionSource, UserInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, GroupInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, TopChatModel.class);
            TableUtils.createTableIfNotExists(connectionSource, GroupUserInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, GroupInContactModel.class);
            TableUtils.createTableIfNotExists(connectionSource, ContactsConstraint.class);
            TableUtils.createTableIfNotExists(connectionSource, UserSetting.class);
            TableUtils.createTableIfNotExists(connectionSource, ConversationModel.class);
            TableUtils.createTableIfNotExists(connectionSource, PushInfoModel.class);
            TableUtils.createTableIfNotExists(connectionSource, TopModel.class);
            TableUtils.createTableIfNotExists(connectionSource, SettingsModel.class);
            TableUtils.createTableIfNotExists(connectionSource, SystemNoticeModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(net.sqlcipher.database.SQLiteDatabase database, ConnectionSource connectionSource) {
        createTableIfNotExists();
    }

    @Override
    public void onUpgrade(net.sqlcipher.database.SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        createTableIfNotExists();
        try {
            ConversationDao.getInstance().onTableUpdate(oldVersion, newVersion);
            UserInfoDao.getInstance().onTableUpdate(oldVersion, newVersion);
            GroupInfoDao.getInstance().onTableUpdate(oldVersion, newVersion);
            MessageDao.getInstance().onTableUpdate(oldVersion, newVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        for (String key : mDaoMap.keySet()) {
            Dao dao = mDaoMap.get(key);
            dao = null;
        }
    }

    public Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();
        if (mDaoMap.containsKey(className)) {
            dao = mDaoMap.get(className);
        } else {
            dao = super.getDao(clazz);
            mDaoMap.put(className, dao);
        }
        return dao;
    }

    public <T> T doTransaction(Callable<T> callable) {
        try {
            return TransactionManager.callInTransaction(getConnectionSource(), callable);
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return null;
    }

    public void updateTable(Dao<?, Integer> dao, int oldVersion, int minVersion, String sql) throws Exception {
        if (oldVersion < minVersion) {
            dao.executeRaw(sql);
        }
    }

    public interface IDBUpdateListener {
        void onTableUpdate(int oldVersion, int newVersion);
    }
}
