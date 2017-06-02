package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/1.
 */
@DatabaseTable(tableName = "group_user_info")
public class GroupUserInfo implements Serializable {

    @DatabaseField
    @Tag(1)
    public String UserID;

    @DatabaseField
    @Tag(2)
    public int IsAdmin; // 0:为创始人  1:为管理员 2:为群成员

    @DatabaseField
    @Tag(3)
    public boolean IsDisturb;

    @DatabaseField(id = true, unique = true)
    @Tag(4)
    public String Id;

    @DatabaseField
    @Tag(5)
    public String GroupID;

    public GroupUserInfo() {
    }

    @Override
    public String toString() {
        return "GroupUserInfo{" +
                "UserID='" + UserID + '\'' +
                ", IsAdmin=" + IsAdmin +
                ", IsDisturb=" + IsDisturb +
                ", Id='" + Id + '\'' +
                ", GroupID='" + GroupID + '\'' +
                '}';
    }
}
