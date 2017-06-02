package com.itutorgroup.tutorchat.phone.domain.request;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupUserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/1.
 */
public class GetGroupInfoRequest extends CommonRequest implements IGetTickListener {
    @Tag(4)
    public String GroupID;

    @Tag(5)
    public long Ticks;

    @Override
    public void loadTicks() {
        if (GroupUserInfoDao.getInstance().getGroupUserCount(GroupID) != 0) {
            this.Ticks = TicksUtil.getTicks(GroupID);
        }
    }

    @Override
    public String toString() {
        return "GetGroupInfoRequest{" +
                "GroupID='" + GroupID + '\'' +
                ", Ticks=" + Ticks +
                '}';
    }
}
