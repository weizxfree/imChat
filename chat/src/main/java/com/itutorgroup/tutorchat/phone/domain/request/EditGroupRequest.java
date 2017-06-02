package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 */
public class EditGroupRequest extends CommonRequest{

    @Tag(4)
    public String GroupID ;
    @Tag(5)
    public String GroupName ;
    @Tag(6)
    public List<String> AddUserIDList ;
    @Tag(7)
    public List<String> RemoveUserIDList ;
    @Tag(8)
    public String Description ;

}
