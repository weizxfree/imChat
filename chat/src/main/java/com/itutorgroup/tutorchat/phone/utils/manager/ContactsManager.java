package com.itutorgroup.tutorchat.phone.utils.manager;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ContactsConstraintDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInContactDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.ContactsConstraint;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.AddContactRequest;
import com.itutorgroup.tutorchat.phone.domain.request.GetGroupInfoRequest;
import com.itutorgroup.tutorchat.phone.domain.request.GetUserRequest;
import com.itutorgroup.tutorchat.phone.domain.request.RemoveContactRequest;
import com.itutorgroup.tutorchat.phone.domain.request.SearchUserByKeyRequest;
import com.itutorgroup.tutorchat.phone.domain.response.AddContactResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetGroupInfoResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserResponse;
import com.itutorgroup.tutorchat.phone.domain.response.RemoveContactResponse;
import com.itutorgroup.tutorchat.phone.domain.response.SearchUserByKeyResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class ContactsManager {
    private static ContactsManager sContactsManager;

    public static final int CONTACT_TYPE_PERSONAL = 1;
    public static final int CONTACT_TYPE_GROUP = 2;

    public static ContactsManager getInstance() {
        if (sContactsManager == null) {
            synchronized (ContactsManager.class) {
                if (sContactsManager == null) {
                    sContactsManager = new ContactsManager();
                }
            }
        }
        return sContactsManager;
    }

    public void searchUserByKey(String key, final RequestHandler.RequestListener<SearchUserByKeyResponse> listener) {
        SearchUserByKeyRequest request = new SearchUserByKeyRequest();
        request.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        request.UserID = AccountManager.getInstance().getCurrentUserId();
        request.Token = AccountManager.getInstance().getToken();
        request.Keys = key;

        new RequestHandler<SearchUserByKeyResponse>()
                .operation(Operation.SEARCH_USER_BY_KEY)
                .request(request)
                .exec(SearchUserByKeyResponse.class, new RequestHandler.RequestListener<SearchUserByKeyResponse>() {
                    @Override
                    public void onResponse(SearchUserByKeyResponse response, Bundle bundle) {
                        Observable.just(response)
                                .observeOn(Schedulers.io())
                                .subscribe(
                                        new Action1<SearchUserByKeyResponse>() {
                                            @Override
                                            public void call(SearchUserByKeyResponse response) {
                                                if (response.UserList != null && response.UserList.size() != 0) {
                                                    UserInfoDao.getInstance().add(response.UserList);
                                                }
                                            }
                                        }
                                );
                        if (listener != null) {
                            listener.onResponse(response, bundle);
                        }
                    }
                });
    }

    public void addContact(Context context, String addId, int addType, RequestHandler.RequestListener<AddContactResponse> listener) {
        AddContactRequest request = new AddContactRequest();
        request.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        request.UserID = AccountManager.getInstance().getCurrentUserId();
        request.Token = AccountManager.getInstance().getToken();
        request.AddID = addId;
        request.AddType = addType;

        new RequestHandler<AddContactResponse>()
                .request(request)
                .dialog(context)
                .operation(Operation.ADD_CONTACT)
                .exec(AddContactResponse.class, listener);
    }


    public void RemoveContact(Context context, String removeId, int removeType, RequestHandler.RequestListener<RemoveContactResponse> listener) {

        RemoveContactRequest removeContactRequest = new RemoveContactRequest();
        removeContactRequest.init();
        removeContactRequest.RemoveID = removeId;
        removeContactRequest.RemoveType = removeType;
        new RequestHandler<RemoveContactResponse>()
                .request(removeContactRequest)
                .operation(Operation.REMOVE_CONTACT)
                .dialog(context)
                .exec(RemoveContactResponse.class, listener);
    }

    public void getUserInfo(String userId, long ticks, final RequestHandler.RequestListener<GetUserResponse> listener) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        GetUserRequest request = new GetUserRequest();
        request.init();
        request.QueryUserID = userId;

        new RequestHandler<GetUserResponse>()
                .operation(Operation.GET_USER)
                .request(request)
                .exec(GetUserResponse.class, new RequestHandler.RequestListener<GetUserResponse>() {
                    @Override
                    public void onResponse(final GetUserResponse response, final Bundle bundle) {
                        if (response.User != null && !TextUtils.isEmpty(response.User.UserID)) {
                            Observable.just(response.User)
                                    .observeOn(Schedulers.io())
                                    .subscribe(new Action1<UserInfo>() {
                                        @Override
                                        public void call(UserInfo userInfo) {
                                            UserInfoDao.getInstance().add(userInfo);
                                            if (listener != null) {
                                                Observable.just(listener)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Action1<RequestHandler.RequestListener<GetUserResponse>>() {
                                                            @Override
                                                            public void call(RequestHandler.RequestListener<GetUserResponse> getUserResponseRequestListener) {
                                                                listener.onResponse(response, bundle);
                                                            }
                                                        }, CommonUtil.ACTION_EXCEPTION);
                                            }
                                        }
                                    }, CommonUtil.ACTION_EXCEPTION);
                        } else if (listener != null) {
                            listener.onResponse(response, bundle);
                        }
                    }
                });
    }

    public void getGroupInfo(String groupId, long ticks, final RequestHandler.RequestListener<GetGroupInfoResponse> listener) {
        GetGroupInfoRequest request = new GetGroupInfoRequest();
        request.init();
        request.GroupID = groupId;

        new RequestHandler()
                .operation(Operation.GET_GROUP_INFO)
                .request(request)
                .exec(GetGroupInfoResponse.class, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                    @Override
                    public void onResponse(final GetGroupInfoResponse response, final Bundle bundle) {
                        if (response.Group != null && !TextUtils.isEmpty(response.Group.GroupID)) {
                            Observable.just(response.Group)
                                    .subscribeOn(Schedulers.io())
                                    .map(new Func1<GroupInfo, Void>() {
                                        @Override
                                        public Void call(GroupInfo groupInfo) {
                                            GroupInfoDao.getInstance().add(response.Group);
                                            EventBusManager.getInstance().post(new GroupInfoEvent(response.Group));
                                            return null;
                                        }
                                    })
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            if (listener != null) {
                                                listener.onResponse(response, bundle);
                                            }
                                        }
                                    }, CommonUtil.ACTION_EXCEPTION);
                        }
                    }
                });
    }

    public void saveContactsToDB(UserInfo user) {
        UserInfoDao.getInstance().add(user);
    }

    public void saveContactsToDB(final List<UserInfo> userList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContactsConstraintDao.getInstance().saveMyContactsConstraint(userList);
                UserInfoDao.getInstance().add(userList);
            }
        }).start();
    }

    public void saveGroupsToDB(final List<GroupInfo> groupList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GroupInContactDao.getInstance().clearMyGroupList();
                GroupInfoDao.getInstance().add(groupList);
            }
        }).start();
    }

    public void getMyContacts(final CommonLoadingListener<List<UserInfo>> listener) {
        Observable.just(listener)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, List<ContactsConstraint>>() {
                    @Override
                    public List<ContactsConstraint> call(Object o) {
                        return ContactsConstraintDao.getInstance().getMyConstraint();
                    }
                })
//                .filter(new Func1<List<ContactsConstraint>, Boolean>() {
//                    @Override
//                    public Boolean call(List<ContactsConstraint> contactsConstraintList) {
//                        return contactsConstraintList != null && contactsConstraintList.size() > 0;
//                    }
//                })
                .map(new Func1<List<ContactsConstraint>, List<String>>() {
                    @Override
                    public List<String> call(List<ContactsConstraint> contactsConstraintList) {
                        List<String> list = new ArrayList<>();
                        if (contactsConstraintList != null && contactsConstraintList.size() > 0) {
                            for (ContactsConstraint cc : contactsConstraintList) {
                                list.add(cc.targetUserId);
                            }
                        }
                        return list;
                    }
                })
//                .filter(new Func1<List<String>, Boolean>() {
//                    @Override
//                    public Boolean call(List<String> list) {
//                        return list != null && list.size() > 0;
//                    }
//                })
                .map(new Func1<List<String>, List<UserInfo>>() {
                    @Override
                    public List<UserInfo> call(List<String> list) {
                        return UserInfoDao.getInstance().findUserByIdList(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<UserInfo>>() {
                    @Override
                    public void call(List<UserInfo> list) {
                        listener.onResponse(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }
}
