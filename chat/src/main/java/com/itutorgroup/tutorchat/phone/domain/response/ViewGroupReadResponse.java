package com.itutorgroup.tutorchat.phone.domain.response;

import java.io.Serializable;
import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/8.
 */
public class ViewGroupReadResponse extends CommonResponse {

    @Tag(3)
    public GroupReadStatusModel GroupReadStatusInfo;

    public ViewGroupReadResponse() {

    }

    public static class GroupReadModel implements Serializable {
        @Tag(1)
        public String UserID;

        @Tag(2)
        public int IsRead;

        public GroupReadModel() {

        }
    }

    public static class GroupReadStatusModel implements Serializable {
        @Tag(1)
        public ArrayList<GroupReadModel> ReadStatusList;

        @Tag(2)
        public int ReadCount;

        @Tag(3)
        public int UnReadCount;

        @Tag(4)
        public String MessageID;

        @Tag(5)
        public String GroupID;

        public GroupReadStatusModel() {

        }
    }
}
