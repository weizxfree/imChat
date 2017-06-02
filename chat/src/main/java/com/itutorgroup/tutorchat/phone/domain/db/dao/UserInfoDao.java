package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by joyinzhao on 2016/8/30.
 */
public class UserInfoDao implements DBHelper.IDBUpdateListener {
    private DBHelper mHelper;
    Dao<UserInfo, Integer> mDao;

    private static UserInfoDao sInstance;

    public static UserInfoDao getInstance() {
        if (sInstance == null) {
            synchronized (UserInfoDao.class) {
                if (sInstance == null) {
                    sInstance = new UserInfoDao();
                }
            }
        }
        return sInstance;
    }

    private UserInfoDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(UserInfo model) {
        try {
            model.currentUserId = AccountManager.getInstance().getCurrentUserId();
            mDao.createOrUpdate(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Collection<UserInfo> collection) {
        if (collection != null) {
            for (UserInfo user : collection) {
                add(user);
            }
        }
    }

    private RawRowMapper<UserInfo> mUserInfoRawRowMapper = new RawRowMapper<UserInfo>() {
        @Override
        public UserInfo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
            int len = columnNames.length;
            UserInfo userInfo = new UserInfo();
            Class clazz = userInfo.getClass();
            for (int i = 0; i < len; i++) {
                try {
                    Field field = clazz.getField(columnNames[i]);
                    String type = field.getType().toString();
                    if ("Long".endsWith(type) || "long".equals(type)) {
                        field.set(userInfo, Long.parseLong(resultColumns[i]));
                    } else if ("Boolean".endsWith(type) || "boolean".equals(type)) {
                        field.set(userInfo, Boolean.parseBoolean(resultColumns[i]));
                    } else {
                        field.set(userInfo, resultColumns[i]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return userInfo;
        }
    };

    public List<UserInfo> searchByKey(String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        List<UserInfo> list = null;
        try {
//            list = mDao.queryBuilder().where().like("Name", "%" + key + "%").or().like("ChineseName", "%" + key + "%").query();
//            String sql = "select distinct user_info.* from user_info, contacts_constraint " +
//                    " where (" +
//                    " (user_info.Name like '%" + key + "%' " +
//                    " or user_info.ChineseName like '%" + key + "%') " +
//                    " and (( user_info.UserID = contacts_constraint.targetUserId " +
//                    " and contacts_constraint.currentUserId = '" +
//                    currentUserId +
//                    "') or (user_info.currentUserId = '" + currentUserId + "'))" +
//                    ");";
            String sql = "select distinct user_info.* from user_info, contacts_constraint " +
                    " where (" +
                    " (user_info.Name like '%" + key + "%' " +
                    " or user_info.ChineseName like '%" + key + "%') " +
                    " and ( user_info.UserID = contacts_constraint.targetUserId " +
                    " and contacts_constraint.currentUserId = '" +
                    currentUserId +
                    "')" +
                    ");";
            GenericRawResults<UserInfo> results = mDao.queryRaw(sql, mUserInfoRawRowMapper);
            list = results.getResults();
            sql = "select distinct user_info.* from user_info " +
                    " where (" +
                    " (user_info.Name like '%" + key + "%' " +
                    " or user_info.ChineseName like '%" + key + "%') " +
                    " and (user_info.currentUserId = '" + currentUserId + "')" +
                    ");";
            results = mDao.queryRaw(sql, mUserInfoRawRowMapper);
            List<UserInfo> tmp = results.getResults();
            if (list == null) {
                list = new ArrayList<>();
            }
            for (UserInfo info : tmp) {
                if (info != null && !TextUtils.isEmpty(info.UserID) && !list.contains(info)) {
                    list.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public UserInfo selectWithId(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        UserInfo userInfo = null;
        try {
            userInfo = mDao.queryBuilder().where().eq("UserID", id).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    public List<UserInfo> findUserByIdList(List<String> idList) {
        if (idList == null || idList.size() == 0) {
            return null;
        }
        List<UserInfo> userInfo = null;
        try {
            userInfo = mDao.queryBuilder().where().in("UserID", idList).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    @Override
    public void onTableUpdate(int oldVersion, int newVersion) {
        try {
            String sqlV13 = "ALTER TABLE `single_messages` ADD COLUMN currentUserId STRING;";
            DBHelper.getInstance().updateTable(mDao, oldVersion, 13, sqlV13);
        } catch (Exception e) {
            LogUtil.exception(e);
        }
    }
}
