package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/22.
 */
public class GetAllContractResponse extends CommonResponse implements ISetTickListener {

    @Tag(3)
    public List<UserInfo> Users;
    @Tag(4)
    public List<GroupInfo> Groups;
    @Tag(5)
    public long LastUpdateTime;

    @Override
    public void setTicks() {
        TicksUtil.setContactsListTick(LastUpdateTime);
    }

    @Override
    public String toString() {
        return "GetAllContractResponse{" +
                "uSize=" + (Users == null ? "null" : Users.size()) +
                ", gSize=" + (Groups == null ? "null" : Groups.size()) +
                ", Users=" + Users +
                ", Groups=" + Groups +
                ", LastUpdateTime=" + LastUpdateTime +
                '}';
    }
}
