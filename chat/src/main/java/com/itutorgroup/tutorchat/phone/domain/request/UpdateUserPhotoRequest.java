package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class UpdateUserPhotoRequest extends CommonRequest {
    @Tag(4)
    public byte[] PhotoData;
}
