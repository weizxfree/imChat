package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;

import java.util.List;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class ServiceAccountListEvent {
    public List<ServiceAccountModel> mList;

    public ServiceAccountListEvent(List<ServiceAccountModel> list) {
        mList = list;
    }
}
