package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */

@DatabaseTable(tableName = "top_chat_v2")
public class TopModel {

    public static final int ID_TYPE_USER = 1;
    public static final int ID_TYPE_GROUP = 2;

    @DatabaseField
    @Tag(1)
    public int Order; // 置顶顺序

    @DatabaseField
    @Tag(2)
    public int Cate; // 1:固定置顶。2:常用置顶。

    @DatabaseField
    @Tag(3)
    public int IdType; // ID类型 1:User, 2:Group

    @DatabaseField
    @Tag(4)
    public String TID; // TargetId

    @DatabaseField(id = true, unique = true)
    @Tag(5)
    public String id;

    @DatabaseField
    @Tag(6)
    public String currentUserId;

    @Override
    public String toString() {
        return "TopModel{" +
                "Order=" + Order +
                ", Cate=" + Cate +
                ", IdType=" + IdType +
                ", TID='" + TID + '\'' +
                ", id='" + id + '\'' +
                ", currentUserId='" + currentUserId + '\'' +
                '}';
    }
}