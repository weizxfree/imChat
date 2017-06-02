package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/24.
 */
@DatabaseTable(tableName = "system_notice")
public class SystemNoticeModel {
    @DatabaseField
    @Tag(1)
    public long SystemNoticeId;

    @DatabaseField
    @Tag(2)
    public long ExpiredTime;

    @DatabaseField
    @Tag(3)
    public String MsgBody;

    @DatabaseField
    @Tag(4)
    public int Cate;

    @DatabaseField
    @Tag(5)
    public int Status;

    @DatabaseField
    @Tag(6)
    public String PosterUserId;

    @DatabaseField
    @Tag(7)
    public long LastModifiedTime;

    @DatabaseField
    @Tag(8)
    public String currentUserId;

    @DatabaseField(id = true, unique = true)
    @Tag(9)
    public String id;

    @Override
    public String toString() {
        return "SystemNoticeModel{" +
                "SystemNoticeId=" + SystemNoticeId +
                ", ExpiredTime=" + ExpiredTime +
                ", MsgBody=" + MsgBody +
                ", Cate=" + Cate +
                ", Status=" + Status +
                ", PosterUserId='" + PosterUserId + '\'' +
                ", LastModifiedTime=" + LastModifiedTime +
                '}';
    }
}
