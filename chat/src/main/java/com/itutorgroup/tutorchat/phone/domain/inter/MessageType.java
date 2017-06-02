package com.itutorgroup.tutorchat.phone.domain.inter;

/**
 * Created by tom_zxzhang on 2017/2/7.
 */
public interface MessageType {
    int TEXT  = 1 ;
    int PIC = 2 ;
    int VOICE = 3 ;
    int FILE = 4 ;
    int GROUPANNOUNCEMENT = 5 ;
    int SYSTEM_MESSAGE = 6;
    int WITH_DRAWAL = 7 ; //撤回
    int SYSTEM_NOTICE = 8;
}



