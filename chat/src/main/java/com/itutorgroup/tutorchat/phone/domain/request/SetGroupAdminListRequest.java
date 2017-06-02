package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.domain.beans.SetGroupAdminInfo;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/12/19.
 */
public class SetGroupAdminListRequest extends CommonRequest {

    @Tag(4)
    public String GroupID;

    @Tag(5)
    public List<SetGroupAdminInfo> GroupAdminList;

    @Override
    public String toString() {
        return "SetGroupAdminListRequest{" +
                "GroupID='" + GroupID + '\'' +
                ", GroupAdminList=" + GroupAdminList +
                '}';
    }
}
