package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/17.
 */
public class AddContactRequest extends CommonRequest {
    @Tag(4)
    public String AddID;

    @Tag(5)
    public int AddType;

}
