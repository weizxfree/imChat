package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by joyinzhao on 2016/9/18.
 */
@DatabaseTable(tableName = "conversation")
public class ConversationModel {

    @DatabaseField(id = true, unique = true)
    public String Id;

    @DatabaseField
    public String CurrentUserId;

    @DatabaseField
    public String targetID;

    @DatabaseField
    public String GroupId;

    @DatabaseField
    public long CreateTime;

    @DatabaseField
    public String draft;

    @DatabaseField(defaultValue = "0")
    public int unread;

    @Override
    public String toString() {
        return "ConversationModel{" +
                "Id='" + Id + '\'' +
                ", CurrentUserId='" + CurrentUserId + '\'' +
                ", targetID='" + targetID + '\'' +
                ", GroupId='" + GroupId + '\'' +
                ", CreateTime=" + CreateTime +
                ", draft='" + draft + '\'' +
                ", unread=" + unread +
                '}';
    }
}
