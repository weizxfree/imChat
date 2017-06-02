package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/15.
 */
public class LoginResponse extends CommonResponse implements Serializable {



    @Tag(3)
    public UserInfo User;

    @Tag(4)
    public String Token;

    public LoginResponse() {

    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "ResultCode=" + ResultCode +
                ", ResultMsg='" + ResultMsg + '\'' +
                ", User=" + User.toString() +
                '}';
    }

}


