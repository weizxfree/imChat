package com.itutorgroup.tutorchat.phone.domain.response;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

import java.util.List;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/9/1.
 */
public class CheckIsReadResponse extends CommonResponse {

    @Tag(3)
    public List<ReadModel>  ReadInfoList ;

    public static class ReadModel{

        @Tag(1)
        public String MessageID ;
        @Tag(2)
        public int ReadCount ; //单聊中表示 1 0 、群聊中表示已经读过的人数
        @Tag(3)
        public int UserNum ; //群组总人数

        @Override
        public String toString() {
            return "ReadModel{" +
                    "MessageID='" + MessageID + '\'' +
                    ", ReadCount=" + ReadCount +
                    ", UserNum=" + UserNum +
                    '}';
        }
    }


    public static List<MessageModel>  convertToMessageModel(List<ReadModel> readInfoList,List<MessageModel> list){

        for(MessageModel messageModel : list){
            for(ReadModel readModel :readInfoList){
                if(messageModel.MessageID.equals(readModel.MessageID)){
                    messageModel.IsRead = readModel.ReadCount;
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "CheckIsReadResponse{" +
                "ReadInfoList=" + ReadInfoList +
                '}';
    }
}
