package com.itutorgroup.tutorchat.phone.domain.response.v2;

import com.itutorgroup.tutorchat.phone.domain.beans.service.ServiceAccountModel;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2017/1/5.
 */
public class GetServiceAccountListResponse extends CommonResponse {
    @Tag(3)
    public ArrayList<ServiceAccountModel> ServiceAccountList;

    @Override
    public String toString() {
        return "GetServiceAccountListResponse{" +
                "ServiceAccountList=" + ServiceAccountList +
                '}';
    }
}
