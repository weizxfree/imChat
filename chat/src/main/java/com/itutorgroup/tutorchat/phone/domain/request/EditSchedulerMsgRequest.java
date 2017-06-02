package com.itutorgroup.tutorchat.phone.domain.request;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/16.
 */
public class EditSchedulerMsgRequest extends CommonRequest {

    @Tag(4)
    public int ScheduleType ;

    @Tag(5)
    public long ExecuteTime ;

    @Tag(6)
    public List<Integer>  WeekOfDays;

    @Tag(7)
    public List<Long>  DateOfMonth;

    @Tag(8)
    public int EveryManyDays ;

    @Tag(9)
    public String Content;

    @Tag(10)
    public int Type  ;

    @Tag(11)
    public String ReceiverID ;

    @Tag(12)
    public int IsGroup ;

    @Tag(13)
    public int SchedulerMsgStatus ;

    @Tag(14)
    public long SchedulerMsgId ;

    @Override
    public String toString() {
        return "EditSchedulerMsgRequest{" +
                "ScheduleType=" + ScheduleType +
                ", ExecuteTime=" + ExecuteTime +
                ", WeekOfDays=" + WeekOfDays +
                ", DateOfMonth=" + DateOfMonth +
                ", EveryManyDays=" + EveryManyDays +
                ", Content='" + Content + '\'' +
                ", Type=" + Type +
                ", ReceiverID='" + ReceiverID + '\'' +
                ", IsGroup=" + IsGroup +
                ", SchedulerMsgStatus=" + SchedulerMsgStatus +
                ", SchedulerMsgId=" + SchedulerMsgId +
                '}';
    }
}
