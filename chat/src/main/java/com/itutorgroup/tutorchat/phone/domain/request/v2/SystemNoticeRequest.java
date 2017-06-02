package com.itutorgroup.tutorchat.phone.domain.request.v2;

import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/24.
 */
public class SystemNoticeRequest extends CommonRequest {
    @Tag(4)
    public long SystemNoticeId;
}
