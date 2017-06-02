package com.itutorgroup.tutorchat.phone.domain.response;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class UpdateUserPhotoResponse extends CommonResponse {
    @Tag(3)
    public String PhotoUrl;

    public UpdateUserPhotoResponse() {

    }

    @Override
    public String toString() {
        return "UpdateUserPhotoResponse{" +
                "PhotoUrl='" + PhotoUrl + '\'' +
                '}';
    }
}
