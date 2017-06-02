package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class GetUserResponse extends CommonResponse implements ISetTickListener {

    @Tag(3)
    public UserInfo User;

    @Override
    public void setTicks() {
        if (User != null) {
            TicksUtil.setTicks(User.UserID, User.LastModifyTime);
        }
    }

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "User=" + User +
                '}';
    }
}
