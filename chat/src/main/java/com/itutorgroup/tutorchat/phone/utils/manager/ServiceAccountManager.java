package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;
import com.itutorgroup.tutorchat.phone.domain.event.ServiceAccountListEvent;
import com.itutorgroup.tutorchat.phone.domain.request.v2.GetServiceAccountListRequest;
import com.itutorgroup.tutorchat.phone.domain.response.v2.GetServiceAccountListResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class ServiceAccountManager {
    private static ServiceAccountManager sInstance;
    private List<ServiceAccountModel> mServiceAccountList;
    private long mLastRefreshTime = 0;

    public static ServiceAccountManager getInstance() {
        if (sInstance == null) {
            synchronized (ServiceAccountManager.class) {
                if (sInstance == null) {
                    sInstance = new ServiceAccountManager();
                }
            }
        }
        return sInstance;
    }

    private ServiceAccountManager() {
    }

    public void logout() {
        mServiceAccountList = null;
        mLastRefreshTime = 0;
    }

    public List<ServiceAccountModel> getServiceAccountList() {
        return mServiceAccountList;
    }

    public ServiceAccountModel getServiceAccount(String id) {
        if (mServiceAccountList != null && mServiceAccountList.size() > 0) {
            for (ServiceAccountModel model : mServiceAccountList) {
                if (TextUtils.equals(model.ServiceAccountId, id)) {
                    return model;
                }
            }
        }
        return null;
    }

    public void autoLoadServiceAccountList() {
        if (System.currentTimeMillis() - mLastRefreshTime >= 60 * 60 * 1000) {
            requestServiceAccountList();
        }
    }

    public void requestServiceAccountList() {
        GetServiceAccountListRequest request = new GetServiceAccountListRequest();
        request.init();
        new RequestHandler<>()
                .operation(Operation.GET_SERVICE_ACCOUNT_LIST)
                .request(request)
                .exec(GetServiceAccountListResponse.class, new RequestHandler.RequestListener<GetServiceAccountListResponse>() {
                    @Override
                    public void onResponse(GetServiceAccountListResponse response, Bundle bundle) {
                        mLastRefreshTime = System.currentTimeMillis();
                        mServiceAccountList = response.ServiceAccountList;
                        EventBusManager.getInstance().post(new ServiceAccountListEvent(mServiceAccountList));
                    }
                });
    }
}
