package com.itutorgroup.tutorchat.phone.utils.common;


import android.content.Intent;
import android.os.Bundle;

import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.ObjectIdTime;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.ObjectItem;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.ObjectUpdate;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ContactsConstraintDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInContactDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.ContactsEvent;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.v2.GetObjectUpdateRequest;
import com.itutorgroup.tutorchat.phone.domain.response.v2.GetObjectUpdateResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class ObjectUpdateHelper {
    public static final int INST_TYPE_CURRENT_USER_SETTING = 1;
    public static final int INST_TYPE_MESSAGE_READ_STATE = 2;
    public static final int INST_TYPE_USER_INFO = 3;
    public static final int INST_TYPE_CURRENT_USER_CONTACTS = 4;
    public static final int INST_TYPE_GROUP = 5;

    public static final int TABLE_NAME_USER = 1;
    public static final int TABLE_NAME_GROUP = 2;
    public static final int TABLE_NAME_CONTACTS = 3;

    public static void autoUpdateContacts(boolean force) {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        String tickKey = "getAllContract:" + currentUserId;
        if (force) {
            update(TABLE_NAME_CONTACTS, currentUserId);
        } else {
            autoUpdate(TABLE_NAME_CONTACTS, currentUserId, tickKey);
        }
    }

    public static void autoUpdate(int instType, String id) {
        autoUpdate(instType, id, id);
    }

    private static void autoUpdate(int instType, String id, String tickKey) {
        long tick = TicksUtil.getTicks(tickKey);
        if (tick != 0) {
            long time = System.currentTimeMillis();
            if (time - tick <= 2 * 60 * 60 * 1000) {
                return;
            }
        }
        update(instType, id);
    }

    private static void update(int instType, String id) {
        GetObjectUpdateRequest request = new GetObjectUpdateRequest();
        request.init();
        request.tableName = instType;
        request.objectIdTime = new ArrayList<>();
        request.objectIdTime.add(new ObjectIdTime(id));
        new RequestHandler<>()
                .operation(Operation.GET_OBJECT_UPDATE)
                .request(request)
                .exec(GetObjectUpdateResponse.class, new RequestHandler.RequestListener<GetObjectUpdateResponse>() {
                    @Override
                    public void onResponse(GetObjectUpdateResponse response, Bundle bundle) {
                        if (response.updateObjects != null) {
                            Observable.from(response.updateObjects)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .subscribe(new Action1<ObjectUpdate>() {
                                        @Override
                                        public void call(ObjectUpdate objectUpdate) {
                                            updateObject(objectUpdate);
                                        }
                                    }, CommonUtil.ACTION_EXCEPTION);
                        }
                    }
                });
    }

    private static void updateObject(ObjectUpdate objectUpdate) {
        int tableName = objectUpdate.tableName;
        String objectId = objectUpdate.tableObjId;
        switch (tableName) {
            case TABLE_NAME_GROUP:
                updateGroupInfo(objectId, objectUpdate.items);
                break;
            case TABLE_NAME_USER:
                updateUserInfo(objectId, objectUpdate.items);
                break;
            case TABLE_NAME_CONTACTS:
                updateContacts(objectId, objectUpdate.items);
                break;
        }
    }

    private static void updateContacts(String objectId, ArrayList<ObjectItem> items) {
        if (items != null && items.size() > 0) {
            for (ObjectItem item : items) {
                int op = item.Operate;
                String itemField = item.Field;
                String itemVal = item.Val;
                switch (op) {
                    case Operation.OBJECT_UPDATE_CONTACTS_ADD_PEOPLE:
                        addContactPeople(itemVal);
                        break;
                    case Operation.OBJECT_UPDATE_CONTACTS_REMOVE_PEOPLE:
                        removeContactPeople(itemVal);
                        break;
                    case Operation.OBJECT_UPDATE_CONTACTS_ADD_GROUP:
                        addContactGroup(itemVal);
                        break;
                    case Operation.OBJECT_UPDATE_CONTACTS_REMOVE_GROUP:
                        removeContactGroup(itemVal);
                        break;
                }
            }
        }
    }

    private static void removeContactPeople(String userId) {
        ContactsConstraintDao.getInstance().removeContact(userId);
    }

    private static void removeContactGroup(String groupId) {
        GroupInContactDao.getInstance().remove(groupId);
        EventBusManager.getInstance().post(ContactsEvent.getInstance());
    }

    private static void addContactGroup(final String groupId) {
        GroupManager.getInstance().getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
            @Override
            public void onResponse(GroupInfo groupInfo) {
                GroupInContactDao.getInstance().add(groupId);
                EventBusManager.getInstance().post(ContactsEvent.getInstance());
            }
        });
    }

    private static void addContactPeople(String userId) {
        UserInfoManager.getInstance().getUserInfo(userId, new CommonLoadingListener<UserInfo>() {
            @Override
            public void onResponse(UserInfo userInfo) {
                ContactsConstraintDao.getInstance().saveMyContactsConstraint(userInfo);
            }
        });
    }

    private static void updateUserInfo(String objectId, ArrayList<ObjectItem> items) {
        UserInfo user = UserInfoDao.getInstance().selectWithId(objectId);
        if (user == null) {
            return;
        }
        if (items != null && items.size() > 0) {
            for (ObjectItem item : items) {
                String itemField = item.Field;
                String itemVal = item.Val;
                try {
                    Field field = user.getClass().getField(itemField);
                    String type = field.getType().toString();
                    if (type.endsWith("Long") || type.equals("long")) {
                        field.set(user, Long.parseLong(itemVal));
                    } else if (type.endsWith("Boolean") || type.equals("boolean")) {
                        field.set(user, Boolean.parseBoolean(itemVal));
                    } else if (type.endsWith("Integer") || type.equals("int")) {
                        field.set(user, Integer.valueOf(itemVal));
                    } else {
                        field.set(user, itemVal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            UserInfoDao.getInstance().add(user);
            ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
            event.setRefreshId(objectId);
            EventBusManager.getInstance().post(event);
            EventBusManager.getInstance().post(ContactsEvent.getInstance());
        }
    }

    private static void updateGroupInfo(String objectId, ArrayList<ObjectItem> items) {
        GroupInfo group = GroupInfoDao.getInstance().selectWithId(objectId);
        if (group == null) {
            return;
        }
        if (items != null && items.size() > 0) {
            for (ObjectItem item : items) {
                int op = item.Operate;
                String itemField = item.Field;
                String itemVal = item.Val;
                switch (op) {
                    case Operation.OBJECT_UPDATE_GROUP_PROFILE:
                        group.updateProfile(itemField, itemVal);
                        break;
                    case Operation.OBJECT_UPDATE_GROUP_MEMBER_NORMAL:
                        groupMemberChanged(group, itemVal, 2);
                        break;
                    case Operation.OBJECT_UPDATE_GROUP_MEMBER_MASTER:
                        groupMemberChanged(group, itemVal, 0);
                        break;
                    case Operation.OBJECT_UPDATE_GROUP_MEMBER_ADMIN:
                        groupMemberChanged(group, itemVal, 1);
                        break;
                    case Operation.OBJECT_UPDATE_GROUP_MEMBER_LEAVE:
                        groupMemberRemoved(group, itemVal);
                        break;
                }
            }
            GroupInfoDao.getInstance().add(group);
            EventBusManager.getInstance().post(new GroupInfoEvent(group));
        }
    }

    private static void groupMemberRemoved(GroupInfo group, String itemVal) {
        ArrayList<GroupUserInfo> userList = group.GroupUsers;
        if (userList != null && !userList.isEmpty()) {
            for (Iterator<GroupUserInfo> iterator = userList.iterator(); iterator.hasNext(); ) {
                GroupUserInfo info = iterator.next();
                if (itemVal.equals(info.UserID)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static void groupMemberChanged(GroupInfo group, String itemVal, int isAdmin) {
        ArrayList<GroupUserInfo> userList = group.GroupUsers;
        if (userList == null || userList.isEmpty()) {
            userList = new ArrayList<>();
            GroupUserInfo info = new GroupUserInfo();
            info.UserID = itemVal;
            info.IsAdmin = isAdmin;
            userList.add(info);
        } else {
            boolean flag = false;
            for (GroupUserInfo item : userList) {
                if (item.UserID.equals(itemVal)) {
                    item.IsAdmin = isAdmin;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                GroupUserInfo info = new GroupUserInfo();
                info.UserID = itemVal;
                info.IsAdmin = isAdmin;
                userList.add(info);
            }
        }
    }

    public static void dispatchCmd(Intent intent) {
        if (intent != null) {
            int type = intent.getIntExtra("type", 0);
            long time = intent.getLongExtra("timeSpan", 0);
            String objectId = intent.getStringExtra("objectId");

            switch (type) {
                case INST_TYPE_CURRENT_USER_SETTING:
                    UserSettingManager.getInstance().loadUserSettings();
                    break;
                case INST_TYPE_USER_INFO:
                    update(TABLE_NAME_USER, AccountManager.getInstance().getCurrentUserId());
                    break;
                case INST_TYPE_CURRENT_USER_CONTACTS:
                    autoUpdateContacts(true);
                    break;
                case INST_TYPE_GROUP:
                    update(TABLE_NAME_GROUP, objectId);
                    break;
            }
        }
    }
}
