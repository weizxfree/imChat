package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;

/**
 * Created by joyinzhao on 2016/9/12.
 */
public class GroupInfoEvent {

    public GroupInfo groupInfo;

    public GroupInfoEvent(GroupInfo group) {
        groupInfo = group;
    }
}
