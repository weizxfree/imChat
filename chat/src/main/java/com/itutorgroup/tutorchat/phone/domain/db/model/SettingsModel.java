package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */
@DatabaseTable(tableName = "user_setting_v2")
public class SettingsModel {

    @Tag(1)
    public String Id;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    @Tag(2)
    public ArrayList<String> DisturbUsers;

    @DatabaseField(defaultValue = "0")
    @Tag(3)
    public int IsDisturb;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    @Tag(4)
    public ArrayList<String> DisturbGroups;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    @Tag(5)
    public ArrayList<String> BlackIDs;

    @DatabaseField(defaultValue = "1")
    @Tag(6)
    public int NewsNoticed;

    @Tag(7)
    public ArrayList<TopModel> Tops;

    @Tag(8)
    public long LastModifyTime;

    @DatabaseField(id = true, unique = true)
    @Tag(9)
    public String UserId;

    @Override
    public String toString() {
        return "SettingsModel{" +
                "Id='" + Id + '\'' +
                ", DisturbUsers=" + DisturbUsers +
                ", IsDisturb=" + IsDisturb +
                ", DisturbGroups=" + DisturbGroups +
                ", BlackIDs=" + BlackIDs +
                ", NewsNoticed=" + NewsNoticed +
                ", Tops=" + Tops +
                ", LastModifyTime=" + LastModifyTime +
                ", UserId='" + UserId + '\'' +
                '}';
    }
}
