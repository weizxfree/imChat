package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class DownImageFileRequest extends CommonRequest {

    @Tag(4)
    public String ImageFileID ;

    @Tag(5)
    public boolean IsOriginalImage ;

}
