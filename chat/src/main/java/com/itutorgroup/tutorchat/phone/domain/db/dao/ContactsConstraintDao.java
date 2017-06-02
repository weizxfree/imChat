package com.itutorgroup.tutorchat.phone.domain.db.dao;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.helper.DBHelper;
import com.itutorgroup.tutorchat.phone.domain.db.model.ContactsConstraint;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ContactsEvent;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/9.
 */
public class ContactsConstraintDao {
    private DBHelper mHelper;
    private Dao<ContactsConstraint, Integer> mDao;

    private static ContactsConstraintDao sInstance;

    public static ContactsConstraintDao getInstance() {
        if (sInstance == null) {
            synchronized (ContactsConstraintDao.class) {
                if (sInstance == null) {
                    sInstance = new ContactsConstraintDao();
                }
            }
        }
        return sInstance;
    }

    private ContactsConstraintDao() {
        mHelper = DBHelper.getInstance();
        try {
            mDao = mHelper.getDao(ContactsConstraint.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCurrentConstraint() {
        try {
            String currentUserId = AccountManager.getInstance().getCurrentUserId();
            if (TextUtils.isEmpty(currentUserId)) {
                return;
            }
            DeleteBuilder deleteBuilder = mDao.deleteBuilder();
            Where where = deleteBuilder.where().eq("currentUserId", currentUserId);
            deleteBuilder.setWhere(where);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void add(String currentUserId, String targetUserId) {
        ContactsConstraint contactsConstraint = new ContactsConstraint();
        contactsConstraint.currentUserId = currentUserId;
        contactsConstraint.targetUserId = targetUserId;
        contactsConstraint.Id = contactsConstraint.currentUserId + targetUserId;
        try {
            mDao.createOrUpdate(contactsConstraint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveMyContactsConstraint(List<UserInfo> userList) {
        if (userList == null || userList.size() == 0) {
            return;
        }
        final String currentUserId = AccountManager.getInstance().getCurrentUserId();
        deleteCurrentConstraint();
        Observable.from(userList)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<UserInfo, String>() {
                    @Override
                    public String call(UserInfo userInfo) {
                        return userInfo.UserID;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String targetUserId) {
                        add(currentUserId, targetUserId);
                    }
                }, CommonUtil.ACTION_EXCEPTION, new Action0() {
                    @Override
                    public void call() {
                        EventBusManager.getInstance().post(ContactsEvent.getInstance());
                    }
                });
    }

    public void saveMyContactsConstraint(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        Observable.just(userInfo)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<UserInfo>() {
                    @Override
                    public void call(UserInfo userInfo) {
                        String currentUserId = AccountManager.getInstance().getCurrentUserId();
                        add(currentUserId, userInfo.UserID);
                        EventBusManager.getInstance().post(ContactsEvent.getInstance());
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public List<ContactsConstraint> getMyConstraint() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        if (TextUtils.isEmpty(currentUserId)) {
            return null;
        }
        List<ContactsConstraint> list = null;
        try {
            list = mDao.queryBuilder().where().eq("currentUserId", currentUserId).query();
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return list;
    }

    public boolean isMyContact(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        boolean flag = false;
        try {
            String currentUserId = AccountManager.getInstance().getCurrentUserId();
            flag = mDao.queryBuilder().where().eq("currentUserId", currentUserId).and().eq("targetUserId", userId).query().size() > 0;
        } catch (Exception e) {
            LogUtil.exception(e);
        }
        return flag;
    }

    public void removeContact(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        try {
            DeleteBuilder builder = mDao.deleteBuilder();
            builder.where().eq("currentUserId", currentUserId).and().eq("targetUserId", userId);
            builder.delete();
            EventBusManager.getInstance().post(ContactsEvent.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
