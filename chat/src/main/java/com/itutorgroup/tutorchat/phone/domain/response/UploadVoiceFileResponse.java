package com.itutorgroup.tutorchat.phone.domain.response;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class UploadVoiceFileResponse extends CommonResponse {

    @Tag(3)
    public String VoiceFileID;

}
