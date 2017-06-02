package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;

/**
 * Created by joyinzhao on 2016/11/24.
 */
public class SystemNoticeEvent {

    public SystemNoticeModel model;

    public SystemNoticeEvent(SystemNoticeModel model) {
        this.model = model;
    }

}
