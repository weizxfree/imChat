package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/6.
 */
public class GetUserByIDsResponse extends CommonResponse{

    @Tag(3)
    public List<UserInfo> Users ;
}
