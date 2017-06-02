package com.itutorgroup.tutorchat.phone.domain.response;

import java.io.Serializable;
import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/11/16.
 */
public class GetSchedulerMsgsResponse extends CommonResponse {


    @Tag(3)
    public SchedulerMsgModel SchedulerMsgs;


    public static class SchedulerMsgModel implements Serializable{

        @Tag(1)
        public long Id ;
        @Tag(2)
        public long LastModifyTime ;
        @Tag(3)
        public int ScheduleType ;
        @Tag(4)
        public long ExecuteTime ;
        @Tag(5)
        public List<Integer>  WeekOfDays;
        @Tag(6)
        public List<Long>  DateOfMonth ;
        @Tag(7)
        public int EveryManyDays ;
        @Tag(8)
        public String Content ;
        @Tag(9)
        public int Type ;
        @Tag(10)
        public String PosterID ;
        @Tag(11)
        public String ReceiverID ;
        @Tag(12)
        public int IsGroup ;
        @Tag(13)
        public int Status ;


        @Override
        public String toString() {
            return "SchedulerMsgModel{" +
                    "Id=" + Id +
                    ", LastModifyTime=" + LastModifyTime +
                    ", ScheduleType=" + ScheduleType +
                    ", ExecuteTime=" + ExecuteTime +
                    ", WeekOfDays=" + WeekOfDays +
                    ", DateOfMonth=" + DateOfMonth +
                    ", EveryManyDays=" + EveryManyDays +
                    ", Content='" + Content + '\'' +
                    ", Type=" + Type +
                    ", PosterID='" + PosterID + '\'' +
                    ", ReceiverID='" + ReceiverID + '\'' +
                    ", IsGroup=" + IsGroup +
                    ", Status=" + Status +
                    '}';
        }
    }

}
