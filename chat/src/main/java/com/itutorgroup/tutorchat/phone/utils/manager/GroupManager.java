package com.itutorgroup.tutorchat.phone.utils.manager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.beans.SetGroupAdminInfo;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInContactDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupUserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupUserInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.AttornGroupMasterRequest;
import com.itutorgroup.tutorchat.phone.domain.request.LeaveGroupRequest;
import com.itutorgroup.tutorchat.phone.domain.request.SetGroupAdminListRequest;
import com.itutorgroup.tutorchat.phone.domain.request.SetGroupAdminRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetGroupInfoResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserByIDsResponse;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GroupManager {
    private static GroupManager sInstance;

    public static GroupManager getInstance() {
        if (sInstance == null) {
            synchronized (GroupManager.class) {
                if (sInstance == null) {
                    sInstance = new GroupManager();
                }
            }
        }
        return sInstance;
    }

    public static final int GROUP_RIGHT_CREATOR = 0;
    public static final int GROUP_RIGHT_ADMIN = 1;
    public static final int GROUP_RIGHT_MEMBER = 2;

    public void LeaveGroup(String groupId, RequestHandler.RequestListener<CommonResponse> listener) {
        LeaveGroupRequest request = new LeaveGroupRequest();
        request.init();
        request.GroupID = groupId;
        new RequestHandler<>()
                .operation(Operation.LEAVE_GROUP)
                .request(request)
                .exec(CommonResponse.class, listener);
    }


    public void getGroupListInContactByGroupIds(final CommonLoadingListener<List<GroupInfo>> listener) {
        new AsyncTask<Void, Void, List<GroupInfo>>() {
            @Override
            protected List<GroupInfo> doInBackground(Void... params) {
                List<String> list = GroupInContactDao.getInstance().getGroupinContactIDs();
                return GroupInfoDao.getInstance().queryGroupInfosByIds(list);
            }

            @Override
            protected void onPostExecute(List<GroupInfo> groupInfos) {
                super.onPostExecute(groupInfos);
                if (listener != null)
                    listener.onResponse(groupInfos);

            }
        }.execute();


    }


    public void getGroupUserInfoList(String groupId, final long limit, final CommonLoadingListener<List<GroupUserInfo>> listener) {
        new AsyncTask<String, Void, List<GroupUserInfo>>() {
            @Override
            protected List<GroupUserInfo> doInBackground(String... params) {
                final String id = params[0];
                long start = System.currentTimeMillis();
                List<GroupUserInfo> groupUserInfoList = GroupInfoDao.getInstance().queryGroupUserInfo(id, limit);
                if (Preconditions.isBlank(groupUserInfoList)) {
                    ContactsManager.getInstance().getGroupInfo(id, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                        @Override
                        public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                            if (response.Group != null) {
                                if (listener != null) {
                                    listener.onResponse(GroupInfoDao.getInstance().queryGroupUserInfo(id, limit));
                                }
                            }
                        }
                    });
                } else {
                    if (listener != null) {
                        listener.onResponse(groupUserInfoList);
                    }
                }
                return null;
            }

        }.execute(groupId);
    }

    public void getUserInfoListById(String groupId, long limit, final CommonLoadingListener<List<UserInfo>> listener) {
        final List<UserInfo> userInfoListFromDbOrNet = new ArrayList<UserInfo>();
        GroupManager.getInstance().getGroupUserInfoList(groupId, limit, new CommonLoadingListener<List<GroupUserInfo>>() {
            @Override
            public void onResponse(final List<GroupUserInfo> groupUserInfos) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> userIDsForNetwork = new ArrayList<String>();
                        for (GroupUserInfo groupUserInfo : groupUserInfos) {
                            UserInfo userInfo = UserInfoDao.getInstance().selectWithId(groupUserInfo.UserID);

                            /**
                             * 本地没有收藏加为好友的
                             */
                            if (StringUtils.isBlank(userInfo)) {
                                userIDsForNetwork.add(groupUserInfo.UserID);
                            } else {
                                userInfoListFromDbOrNet.add(userInfo);
                            }
                        }

                        if (Preconditions.isBlank(userIDsForNetwork)) {
                            if (listener != null) {
                                listener.onResponse(userInfoListFromDbOrNet);
                            }
                        } else {
                            UserInfoManager.getInstance().GetUserByIDsRequest(userIDsForNetwork, new RequestHandler.RequestListener<GetUserByIDsResponse>() {
                                @Override
                                public void onResponse(final GetUserByIDsResponse response, Bundle bundle) {
                                    userInfoListFromDbOrNet.addAll(response.Users);
                                    if (listener != null) {
                                        listener.onResponse(userInfoListFromDbOrNet);
                                    }

                                    Observable.just("")
                                            .subscribeOn(Schedulers.io())
                                            .map(new Func1<Object, Void>() {
                                                @Override
                                                public Void call(Object o) {
                                                    UserInfoDao.getInstance().add(response.Users);
                                                    return null;
                                                }
                                            })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action1<Void>() {
                                                @Override
                                                public void call(Void aVoid) {
                                                }
                                            }, CommonUtil.ACTION_EXCEPTION);


//                                    new UserInfoDao().add(response.Users); //网络请求好之后缓存到本地
                                }

                                @Override
                                public void onError(int errorCode, GetUserByIDsResponse response, Exception e, Bundle bundle) {
                                    super.onError(errorCode, response, e, bundle);
                                    Observable.just("")
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Action1<String>() {
                                                @Override
                                                public void call(String s) {
                                                    if (listener != null) {
                                                        listener.onResponse(userInfoListFromDbOrNet);
                                                    }
                                                }
                                            }, CommonUtil.ACTION_EXCEPTION);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    public void getGroupInfo(final String groupId, final CommonLoadingListener<GroupInfo> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        Observable.just(groupId).subscribeOn(Schedulers.io())
                .map(new Func1<String, GroupInfo>() {
                    @Override
                    public GroupInfo call(String id) {
                        return GroupInfoDao.getInstance().selectWithId(id);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GroupInfo>() {
                    @Override
                    public void call(GroupInfo groupInfo) {
                        if (groupInfo != null) {
                            listener.onResponse(groupInfo);
                        } else {
                            TicksUtil.setTicks(groupId, 0);
                            ContactsManager.getInstance().getGroupInfo(groupId, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                                @Override
                                public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                                    listener.onResponse(response.Group);
                                }
                            });
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public void getGroupRightByUserId(final String groupId, final String userId, final CommonLoadingListener<Integer> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        Observable.just(groupId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        return GroupInfoDao.getInstance().getUserRightInGroup(groupId, userId);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        listener.onResponse(integer);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public String getDefaultGroupName(List<UserInfo> userInfoList) {
        if (Preconditions.isBlank(userInfoList))
            return "";
        StringBuilder groupName = new StringBuilder();
        for (UserInfo s : userInfoList) {
            groupName.append(s.Name + "、");
        }
        return groupName.deleteCharAt(groupName.length() - 1).toString();
    }

    public void formatGroupName(GroupInfo groupInfo, final CommonLoadingListener<String> listener) {
        if (TextUtils.isEmpty(groupInfo.GroupName) || "群聊".equals(groupInfo.GroupName)) {
            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + groupInfo.GroupID, "");
            if (listener != null) {
                listener.onResponse(cacheName);
            }
            GroupManager.getInstance().getDefaultGroupName(groupInfo.GroupID, new CommonLoadingListener<String>() {
                @Override
                public void onResponse(String s) {
                    if (listener != null) {
                        listener.onResponse(s);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onResponse(groupInfo.GroupName);
            }
            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + groupInfo.GroupID);
        }
    }


    public void formatGroupNameOnce(GroupInfo groupInfo, final CommonLoadingListener<String> listener) {
        if (TextUtils.isEmpty(groupInfo.GroupName) || "群聊".equals(groupInfo.GroupName)) {
            GroupManager.getInstance().getDefaultGroupName(groupInfo.GroupID, new CommonLoadingListener<String>() {
                @Override
                public void onResponse(String s) {
                    if (listener != null) {
                        listener.onResponse(s);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onResponse(groupInfo.GroupName);
            }
            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + groupInfo.GroupID);
        }
    }

    public void getDefaultGroupName(final String groupId, final CommonLoadingListener<String> listener) {
        if (!TextUtils.isEmpty(groupId)) {
            GroupManager.getInstance().getUserInfoListById(groupId, 3, new CommonLoadingListener<List<UserInfo>>() {
                @Override
                public void onResponse(List<UserInfo> list) {
                    if (list != null && list.size() > 0) {
                        Observable.just(list)
                                .subscribeOn(Schedulers.io())
                                .map(new Func1<List<UserInfo>, String>() {
                                    @Override
                                    public String call(List<UserInfo> list) {
                                        String tmpName = getDefaultGroupName(list);
                                        if (TextUtils.isEmpty(tmpName)) {
                                            tmpName = LPApp.getInstance().getString(R.string.group_chat);
                                        } else {
                                            AppPrefs.get(LPApp.getInstance()).putString("cache_group_name_" + groupId, tmpName);
                                        }
                                        return tmpName;
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<String>() {
                                    @Override
                                    public void call(String s) {
                                        if (listener != null) {
                                            listener.onResponse(s);
                                        }
                                    }
                                }, CommonUtil.ACTION_EXCEPTION);
                    }
                }
            });
        }
    }

    public void attornGroupMaster(Context context, final String groupId, final String newUserId, final CommonLoadingListener<Void> listener) {
        AttornGroupMasterRequest request = new AttornGroupMasterRequest();
        request.init();
        request.GroupID = groupId;
        request.NewUserID = newUserId;
        new RequestHandler()
                .operation(Operation.ATTORN_GROUP_MASTER)
                .request(request)
                .dialog(context)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        ToastUtil.show(R.string.common_successful_operation);
                        GroupUserInfoDao dao = GroupUserInfoDao.getInstance();
                        dao.setUserRightWithGroup(groupId, newUserId, 0);
                        dao.setUserRightWithGroup(groupId, AccountManager.getInstance().getCurrentUserId(), 2);
                        getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
                            @Override
                            public void onResponse(GroupInfo groupInfo) {
                                EventBusManager.getInstance().post(new GroupInfoEvent(groupInfo));
                            }
                        });
                        if (listener != null) {
                            listener.onResponse(null);
                        }
                    }
                });
    }

    public void setGroupAdmin(Context context, final String groupId, final String changeUserId, final boolean add, final CommonLoadingListener<Void> listener) {
        SetGroupAdminRequest request = new SetGroupAdminRequest();
        request.init();
        request.GroupID = groupId;
        request.ChangeUserID = changeUserId;
        request.AddOrRemove = add;

        new RequestHandler()
                .operation(Operation.SET_GROUP_ADMIN)
                .request(request)
                .dialog(context)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        ToastUtil.show(R.string.common_successful_operation);
                        GroupUserInfoDao.getInstance().setUserRightWithGroup(groupId, changeUserId, add ? 1 : 2);
                        getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
                            @Override
                            public void onResponse(GroupInfo groupInfo) {
                                EventBusManager.getInstance().post(new GroupInfoEvent(groupInfo));
                            }
                        });
                        if (listener != null) {
                            listener.onResponse(null);
                        }
                    }
                });
    }

    public void setGroupAdminList(Context context, final String groupId, List<String> idList, final CommonLoadingListener<Boolean> listener) {
        List<String> managerList = new ArrayList<>();
        List<GroupUserInfo> manager = GroupUserInfoDao.getInstance().queryGroupManagerInfoList(groupId);
        if (manager == null) {
            manager = new ArrayList<>();
        }
        for (GroupUserInfo gui : manager) {
            managerList.add(gui.UserID);
        }

        if (managerList.size() == idList.size()) {
            boolean flag = false;
            for (String id : managerList) {
                if (!idList.contains(id)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                if (listener != null) {
                    listener.onResponse(false);
                }
                return;
            }
        }

        List<SetGroupAdminInfo> list = new ArrayList<>();

        if (managerList.size() > 0) {
            for (String id : managerList) {
                if (!idList.contains(id)) {
                    SetGroupAdminInfo info = new SetGroupAdminInfo();
                    info.UserID = id;
                    info.AddOrRemove = false;
                    list.add(info);
                }
            }
        }

        if (idList != null && idList.size() > 0) {
            for (String id : idList) {
                if (!managerList.contains(id)) {
                    SetGroupAdminInfo info = new SetGroupAdminInfo();
                    info.UserID = id;
                    info.AddOrRemove = true;
                    list.add(info);
                }
            }
        }

        SetGroupAdminListRequest request = new SetGroupAdminListRequest();
        request.init();
        request.GroupID = groupId;
        request.GroupAdminList = list;

        new RequestHandler()
                .operation(Operation.SET_GROUP_ADMIN_LIST)
                .request(request)
                .dialog(context)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        ContactsManager.getInstance().getGroupInfo(groupId, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                            @Override
                            public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                                if (listener != null) {
                                    listener.onResponse(true);
                                }
                            }
                        });
                    }
                });
    }

    public void onGroupAnnouncementUpdate(final String groupId, final String announcement) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(announcement)) {
            return;
        }
        Observable.just(groupId)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        GroupInfoDao.getInstance().updateGroupAnnouncement(groupId, announcement);
                        getGroupInfo(groupId, new CommonLoadingListener<GroupInfo>() {
                            @Override
                            public void onResponse(GroupInfo groupInfo) {
                                EventBusManager.getInstance().post(new GroupInfoEvent(groupInfo));
                            }
                        });
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public Map<String, String> getGroupInfoMap(GroupInfo group) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = group.getClass().getFields();
        if (fields != null) {
            for (Field field : fields) {
                String name = field.getName();
                try {
                    Object obj = field.get(group);
                    if (obj != null) {
                        map.put(name, obj.toString());
                    }
                } catch (IllegalAccessException e) {
                }
            }
        }
        return map;
    }

}
