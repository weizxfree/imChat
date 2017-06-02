package com.itutorgroup.tutorchat.phone.domain.response;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/14.
 */
public class CheckClientVersionResponse extends CommonResponse  {


    @Tag(3)
    public UpdateVersion Version;

    public static class UpdateVersion implements Serializable{
        @Tag(1)
        public String Name ;
        @Tag(2)
        public String Version ;
        @Tag(3)
        public int VersionNum ;
        @Tag(4)
        public String Url;
        @Tag(5)
        public int IsForce ;
        @Tag(6)
        public String Info;

    }

}
