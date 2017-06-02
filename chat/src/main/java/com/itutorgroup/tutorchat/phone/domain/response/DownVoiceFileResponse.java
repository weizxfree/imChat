package com.itutorgroup.tutorchat.phone.domain.response;

import java.util.Arrays;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class DownVoiceFileResponse extends CommonResponse{

    @Tag(3)
    public  String VoiceFileID ;
    @Tag(4)
    public  String ExtendName ;
    @Tag(5)
    public  byte[] FileData ;
    @Tag(6)
    public  long PlayTime ;


    @Override
    public String toString() {
        return "DownVoiceFileResponse{" +
                ", PlayTime=" + PlayTime +
                "VoiceFileID='" + VoiceFileID + '\'' +
                ", ExtendName='" + ExtendName + '\'' +
                ", FileData=" + Arrays.toString(FileData) +
                '}';
    }
}
