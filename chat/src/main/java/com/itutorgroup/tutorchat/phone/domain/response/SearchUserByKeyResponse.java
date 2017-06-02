package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.io.Serializable;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class SearchUserByKeyResponse extends CommonResponse implements Serializable {
    @Tag(3)
    public List<UserInfo> UserList;
}
