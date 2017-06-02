package com.itutorgroup.tutorchat.phone.domain.response.v2;

import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/24.
 */
public class SystemNoticeResponse extends CommonResponse {
    @Tag(3)
    public SystemNoticeModel SystemNotice;

    @Override
    public String toString() {
        return "SystemNoticeResponse{" +
                "SystemNotice=" + SystemNotice +
                '}';
    }
}
