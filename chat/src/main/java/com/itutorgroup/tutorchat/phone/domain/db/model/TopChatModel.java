package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * @deprecated
 */
@DatabaseTable(tableName = "top_chat")
public class TopChatModel implements Serializable {

    @DatabaseField(id = true, unique = true)
    public String Id;

    @DatabaseField
    public String CurrentUserId;

    @DatabaseField
    public String targetID;

    @DatabaseField
    public long CreateTime;

    @Override
    public String toString() {
        return "TopChatModel{" +
                "CurrentUserId='" + CurrentUserId + '\'' +
                ", targetID='" + targetID + '\'' +
                ", CreateTime='" + CreateTime + '\'' +
                '}';
    }

    public TopChatModel() {
    }
}
