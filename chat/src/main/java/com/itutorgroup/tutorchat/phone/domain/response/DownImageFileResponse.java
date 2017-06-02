package com.itutorgroup.tutorchat.phone.domain.response;

import java.util.Arrays;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/7.
 */
public class DownImageFileResponse extends CommonResponse{

    @Tag(3)
    public  String ImageFileID ;
    @Tag(4)
    public  String ExtendName ;
    @Tag(5)
    public  byte[] FileData ;

    @Override
    public String toString() {
        return "DownImageFileResponse{" +
                "ImageFileID='" + ImageFileID + '\'' +
                ", ExtendName='" + ExtendName + '\'' +
                ", FileData=" + Arrays.toString(FileData) +
                '}';
    }
}
