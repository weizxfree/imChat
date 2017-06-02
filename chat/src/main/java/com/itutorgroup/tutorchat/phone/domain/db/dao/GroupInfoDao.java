package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by joyinzhao on 2016/9/1.
 */
public class GroupInfoDao implements DBHelper.IDBUpdateListener {
    private DBHelper mHelper;
    private Dao<GroupInfo, Integer> mDao;
    private GroupUserInfoDao mGroupUserInfoDao;

    private static GroupInfoDao sInstance;

    public static GroupInfoDao getInstance() {
        if (sInstance == null) {
            synchronized (GroupInfoDao.class) {
                if (sInstance == null) {
                    sInstance = new GroupInfoDao();
                }
            }
        }
        return sInstance;
    }

    private GroupInfoDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(GroupInfo.class);
            mGroupUserInfoDao = GroupUserInfoDao.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTableUpdate(int oldVersion, int newVersion) {
        try {
            String sqlV4 = "ALTER TABLE `group_info` ADD COLUMN AnnouncementText text;";
            String sqlV16 = "ALTER TABLE `group_info` ADD COLUMN currentUserId STRING;";
            DBHelper.getInstance().updateTable(mDao, oldVersion, 4, sqlV4);
            DBHelper.getInstance().updateTable(mDao, oldVersion, 16, sqlV16);
        } catch (Exception e) {

        }
    }

    public void add(final GroupInfo model) {
        try {
            model.currentUserId = AccountManager.getInstance().getCurrentUserId();
            DBHelper.getInstance().doTransaction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mDao.createOrUpdate(model);
                    mGroupUserInfoDao.add(model.GroupID, model.GroupUsers);
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Collection<GroupInfo> collection) {
        if (collection != null) {
            for (GroupInfo group : collection) {
                GroupInContactDao.getInstance().add(group.GroupID);
                add(group);
            }
        }
    }

    public GroupInfo selectWithId(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        GroupInfo group = null;
        try {
            group = mDao.queryBuilder().where().eq("GroupID", id).queryForFirst();
            if (group != null) {
                group.GroupUsers = new ArrayList<>(mGroupUserInfoDao.queryGroupUserInfoList(id, -1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return group;
    }

    public void queryAll() {
        try {
            List<GroupInfo> list = mDao.queryBuilder().query();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<GroupInfo> queryGroupInfosByIds(List<String> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        List<GroupInfo> groupInfos = null;
        if (Preconditions.isBlank(list))
            return null;
        try {
            groupInfos = mDao.queryBuilder().where().in("GroupID", list).query();
            return groupInfos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateGroupName(String id, String groupName) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(groupName))
            return;
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("GroupName", groupName).where().in("GroupID", id);
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGroupAnnouncement(String id, String announcement) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(announcement))
            return;
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        try {
            updateBuilder.updateColumnValue("AnnouncementText", announcement).where().in("GroupID", id);
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<GroupUserInfo> queryGroupUserInfo(String groupId, long limit) {
        return mGroupUserInfoDao.queryGroupUserInfoList(groupId, limit);
    }

    private RawRowMapper mSearchGroupMapper = new RawRowMapper<SearchManager.SearchGroupBean>() {
        @Override
        public SearchManager.SearchGroupBean mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
            SearchManager.SearchGroupBean bean = new SearchManager.SearchGroupBean();
            bean.id = resultColumns[0];
            bean.name = resultColumns[1];
            bean.count = "" + mGroupUserInfoDao.getGroupUserCount(resultColumns[0]);
            return bean;
        }
    };

    private String getUserLikeSql(String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        return "select distinct group_info.GroupID, group_info.GroupName " +
                " from group_info, group_user_info, group_in_contact, user_info " +
                " where (" +
                " ((group_info.GroupID = group_in_contact.GroupID " +
                " and group_in_contact.CurrentUserId = '" +
                currentUserId + "') or group_info.currentUserId = '" +
                currentUserId + "') " +
                " and (group_info.GroupID = group_user_info.GroupID " +
                " and user_info.UserId = group_user_info.UserId " +
                " and user_info.Name like '%" + key + "%')" +
                ");";
    }

    private String getGroupLikeSql(String key) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        return "select distinct group_info.GroupID, group_info.GroupName " +
                " from group_info, group_in_contact " +
                " where (" +
                " group_info.GroupName like '%" + key + "%' and " +
                " group_info.GroupID = group_in_contact.GroupID " +
                " and group_in_contact.CurrentUserId = '" +
                currentUserId + "'); ";
    }

    public List<SearchManager.SearchGroupBean> searchByKey(String key) {
        List<SearchManager.SearchGroupBean> list = null;
        try {
            GenericRawResults<SearchManager.SearchGroupBean> tmp = mDao.queryRaw(getUserLikeSql(key), mSearchGroupMapper);
            list = tmp.getResults();
            Set set = new HashSet(list);
            tmp = mDao.queryRaw(getGroupLikeSql(key), mSearchGroupMapper);
            list = tmp.getResults();
            set.addAll(list);
            list.clear();
            list.addAll(set);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getUserRightInGroup(String groupId, String userId) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(userId)) {
            return 100;
        }
        int right = 100;
        try {
            String sql = "select group_user_info.IsAdmin from group_user_info, group_info " +
                    "where (group_info.GroupID = group_user_info.GroupID " +
                    "and group_info.GroupID = '" + groupId +
                    "' and group_user_info.UserID = '" + userId + "')";
            GenericRawResults<String[]> tmp = mDao.queryRaw(sql);
            String result = tmp.getFirstResult()[0];
            right = Integer.parseInt(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return right;
    }

}
