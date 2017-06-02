package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/1.
 */
public class GetGroupInfoResponse extends CommonResponse implements ISetTickListener {
    @Tag(3)
    public GroupInfo Group;

    @Override
    public void setTicks() {
        if (Group != null) {
            TicksUtil.setTicks(Group.GroupID, Group.LastModifyTime);
        }
    }

    @Override
    public String toString() {
        return "GetGroupInfoResponse{" +
                "Group=" + Group +
                '}';
    }
}
