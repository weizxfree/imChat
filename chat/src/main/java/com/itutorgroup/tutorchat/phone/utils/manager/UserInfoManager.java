package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.request.GetUserByIDsRequest;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserByIDsResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserResponse;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.salesuite.saf.utils.Preconditions;

/**
 * Created by tom_zxzhang on 2016/9/6.
 */
public class UserInfoManager {


    private static UserInfoManager userInfoManager;

    public static UserInfoManager getInstance() {
        if (userInfoManager == null) {
            synchronized (UserInfoManager.class) {
                if (userInfoManager == null) {
                    userInfoManager = new UserInfoManager();
                }
            }
        }
        return userInfoManager;
    }


    public void GetUserByIDsRequest(List<String> ids, RequestHandler.RequestListener<GetUserByIDsResponse> listener) {

        if (Preconditions.isBlank(ids))
            return;
        GetUserByIDsRequest request = new GetUserByIDsRequest();
        request.init();
        request.UserIDList = ids;
        new RequestHandler<>()
                .operation(Operation.GET_USER_BY_IDS)
                .request(request)
                .exec(GetUserByIDsResponse.class, listener);
    }

    public void getUserInfo(String userId, final CommonLoadingListener<UserInfo> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String userId = params[0];
                final UserInfo user = UserInfoDao.getInstance().selectWithId(userId);
                if (user != null) {
                    LPApp.getInstance().mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onResponse(user);
                        }
                    });
                } else {
                    ContactsManager.getInstance().getUserInfo(userId, 0, new RequestHandler.RequestListener<GetUserResponse>() {
                        @Override
                        public void onResponse(final GetUserResponse response, Bundle bundle) {
                            if (response.User != null && !TextUtils.isEmpty(response.User.UserID)) {
                                LPApp.getInstance().mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onResponse(response.User);
                                    }
                                });
                            }
                        }
                    });
                }
                return null;
            }
        }.execute(userId);
    }

    public void forceGetUserList(final List<String> ids, final CommonLoadingListener<List<UserInfo>> listener) {
        if (ids == null || ids.size() == 0 || listener == null) {
            throw new IllegalArgumentException();
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (Iterator<String> iterator = ids.iterator(); iterator.hasNext(); ) {
                    String id = iterator.next();
                    if (TextUtils.isEmpty(id)) {
                        iterator.remove();
                        break;
                    }
                }
                final List<UserInfo> list = UserInfoDao.getInstance().findUserByIdList(ids);

                List<String> queryIds = new ArrayList<>();
                if (list == null || list.size() == 0) {
                    queryIds = ids;
                } else if (ids.size() == list.size()) {
                    listener.onResponse(list);
                    return null;
                } else {
                    for (String id : ids) {
                        boolean exists = false;
                        for (UserInfo user : list) {
                            if (id.equals(user.UserID)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            queryIds.add(id);
                        }
                    }
                }
                GetUserByIDsRequest(queryIds, new RequestHandler.RequestListener<GetUserByIDsResponse>() {
                    @Override
                    public void onResponse(GetUserByIDsResponse response, Bundle bundle) {
                        if (response.Users != null) {
                            list.addAll(response.Users);
                            UserInfoDao.getInstance().add(response.Users);
                        }
                        listener.onResponse(list);
                    }
                });
                return null;
            }
        }.execute();
    }

    public Map<String, String> getUserInfoMap(UserInfo user) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = user.getClass().getFields();
        if (fields != null) {
            for (Field field : fields) {
                String name = field.getName();
                try {
                    Object obj = field.get(user);
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
