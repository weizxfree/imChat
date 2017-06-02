package com.itutorgroup.tutorchat.phone.domain.request;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class UploadVoiceFileRequest extends CommonRequest {

    @Tag(4)
    public byte[] FileData;
    @Tag(5)
    public String ExtendName;
    @Tag(6)
    public long PlayTime;


}
