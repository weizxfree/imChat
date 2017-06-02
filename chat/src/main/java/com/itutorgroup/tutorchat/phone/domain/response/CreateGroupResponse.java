package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;

import java.io.Serializable;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 * 创建群组response
 *
 */
public class CreateGroupResponse extends CommonResponse implements Serializable{

    @Tag(3)
    public GroupInfo Group ;

    public CreateGroupResponse() {
    }

    @Override
    public String toString() {
        return "CreateGroupResponse{" +
                "Group=" + Group +
                '}';
    }
}
