package com.itutorgroup.tutorchat.phone.domain.db.model;

import java.io.Serializable;
import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public class TcpMessageModel implements Serializable {

    @Tag(1)
    public String Content;

    @Tag(2)
    public int Type; //消息类型 1 文本 2 图片3 语音 4 文件 5 群公告

    @Tag(3)
    public String PosterID;

    @Tag(4)
    public String GroupId;

    @Tag(5)
    public int Priority;

    @Tag(6)
    public long CreateTime;

    @Tag(7)
    public String ReceiverMessageID;

    @Tag(8)
    public String ReceiverID;

    @Tag(9)
    public int IsRead;

    @Tag(10)
    public int IsSelf; // 1 自己发的 2 别人发的

    @Tag(11)
    public long MessageID;

    @Tag(12)
    public ArrayList<String> AltReceivers;

    @Tag(13)
    public int IsReceipt;

    @Override
    public String toString() {
        return "TcpMessageModel{" +
                "Content='" + Content + '\'' +
                ", Type=" + Type +
                ", PosterID='" + PosterID + '\'' +
                ", GroupId='" + GroupId + '\'' +
                ", Priority=" + Priority +
                ", CreateTime=" + CreateTime +
                ", ReceiverMessageID='" + ReceiverMessageID + '\'' +
                ", ReceiverID='" + ReceiverID + '\'' +
                ", IsRead=" + IsRead +
                ", IsSelf=" + IsSelf +
                ", MessageID=" + MessageID +
                ", AltReceivers=" + AltReceivers +
                ", IsReceipt=" + IsReceipt +
                '}';
    }
}
