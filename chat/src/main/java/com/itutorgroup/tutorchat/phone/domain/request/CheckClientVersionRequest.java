package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/14.
 */
public class CheckClientVersionRequest extends CommonRequest {

    @Tag(4)
    public int clientType;

    @Tag(5)
    public int versionNum;

    @Override
    public String toString() {
        return "CheckClientVersionRequest{" +
                "clientType=" + clientType +
                ", versionNum=" + versionNum +
                '}';
    }
}
