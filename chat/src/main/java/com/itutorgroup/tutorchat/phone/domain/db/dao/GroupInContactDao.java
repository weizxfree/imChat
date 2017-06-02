package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInContactModel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by joyinzhao on 2016/9/6.
 */
public class GroupInContactDao {
    private DBHelper mHelper;
    private Dao<GroupInContactModel, Integer> mDao;

    private static GroupInContactDao sInstance;

    public static GroupInContactDao getInstance() {
        if (sInstance == null) {
            synchronized (GroupInContactDao.class) {
                if (sInstance == null) {
                    sInstance = new GroupInContactDao();
                }
            }
        }
        return sInstance;
    }

    private GroupInContactDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(GroupInContactModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String GroupID) {
        GroupInContactModel groupInContactModel = new GroupInContactModel();
        groupInContactModel.id = GroupID + AccountManager.getInstance().getCurrentUserId();
        groupInContactModel.GroupID = GroupID;
        groupInContactModel.CurrentUserId = AccountManager.getInstance().getCurrentUserId();
        try {
            mDao.createOrUpdate(groupInContactModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String groupId) {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        try {
            Where where = deleteBuilder.where().eq("GroupID", groupId).and().eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId());
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearMyGroupList() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return;
        }
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        try {
            Where where = deleteBuilder.where().eq("CurrentUserId", currentUserId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSaveToContact(String groupId) {

        Where where = mDao.queryBuilder().where();
        try {
            GroupInContactModel groupInContactModel = (GroupInContactModel) where.and(
                    where.eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId()),
                    where.eq("GroupID", groupId)
            ).queryForFirst();
            if (StringUtils.isNotBlank(groupInContactModel))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<String> getGroupinContactIDs() {
        List<String> list = new ArrayList<>();
        try {
            GenericRawResults<String[]> strings = mDao.queryBuilder().distinct().selectColumns("GroupID").where().eq("CurrentUserId", AccountManager.getInstance().getCurrentUserId()).and().isNotNull("GroupID").queryRaw();
            for (String[] resultColumns : strings) {
                String unreadMessageId = resultColumns[0];
                list.add(unreadMessageId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
