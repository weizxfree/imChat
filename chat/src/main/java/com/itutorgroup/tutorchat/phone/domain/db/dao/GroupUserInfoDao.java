package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.Collection;
import java.util.List;

/**
 * Created by joyinzhao on 2016/9/6.
 */
public class GroupUserInfoDao {
    private DBHelper mHelper;
    private Dao<GroupUserInfo, Integer> mDao;

    private static GroupUserInfoDao sInstance;

    public static GroupUserInfoDao getInstance() {
        if (sInstance == null) {
            synchronized (GroupUserInfoDao.class) {
                if (sInstance == null) {
                    sInstance = new GroupUserInfoDao();
                }
            }
        }
        return sInstance;
    }

    private GroupUserInfoDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(GroupUserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String groupId, Collection<GroupUserInfo> collection) {
        if (collection != null && collection.size() > 0) {
            for (GroupUserInfo info : collection) {
                info.GroupID = groupId;
                info.Id = groupId + info.UserID;
            }
        }
        try {
            remove(groupId);
            if (collection != null && collection.size() > 0) {
                for (GroupUserInfo info : collection) {
                    mDao.createOrUpdate(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String groupId) {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        try {
            Where where = deleteBuilder.where().eq("GroupID", groupId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<GroupUserInfo> queryGroupUserInfoList(String groupId, long limit) {
        List<GroupUserInfo> list = null;
        try {
            list = mDao.queryBuilder().orderBy("IsAdmin", true).limit(limit).where().eq("GroupID", groupId).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public long getGroupUserCount(String groupId) {
        long count = 0;
        if (!TextUtils.isEmpty(groupId)) {
            try {
                QueryBuilder queryBuilder = mDao.queryBuilder();
                queryBuilder.setCountOf(true);
                queryBuilder.setWhere(queryBuilder.where().eq("GroupID", groupId));
                count = mDao.countOf(queryBuilder.prepare());
            } catch (Exception e) {
            }
        }
        return count;
    }


    public List<GroupUserInfo> queryGroupManagerInfoList(String groupId) {
        List<GroupUserInfo> list = null;
        try {
            list = mDao.queryBuilder().orderBy("IsAdmin", true).where().eq("GroupID", groupId).and().eq("IsAdmin", 1).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<GroupUserInfo> queryGroupAdminList(String groupId) {
        List<GroupUserInfo> list = null;
        try {
            list = mDao.queryBuilder().orderBy("IsAdmin", true).where().eq("GroupID", groupId).and().eq("IsAdmin", 0).query();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return list;
    }

    public void setUserRightWithGroup(String groupId, String userId, int admin) {
        try {
            UpdateBuilder builder = mDao.updateBuilder();
            builder.updateColumnValue("IsAdmin", admin).where().eq("GroupID", groupId).and().eq("UserID", userId);
            builder.update();
        } catch (Exception e) {

        }
    }


/*
    See UserSettingManager.
    */
/*设置群的免打扰*//*

    public void updateGroupSetting(String groupId,boolean isNotDistub){
        UpdateBuilder builder = mDao.updateBuilder();
        try {
            builder.updateColumnValue("IsDisturb", isNotDistub).where().eq("GroupID", groupId).and().eq("UserID", AccountManager.getInstance().getCurrentUserId());
            builder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean queryGroupIsDistubState(String groupId){
        try {
            GroupUserInfo groupUserInfo =  mDao.queryBuilder().where().eq("GroupID", groupId).and().eq("UserID", AccountManager.getInstance().getCurrentUserId()).queryForFirst();
            if(StringUtils.isNotBlank(groupUserInfo)&&groupUserInfo.IsDisturb)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
            return  false;

    }
*/


}
