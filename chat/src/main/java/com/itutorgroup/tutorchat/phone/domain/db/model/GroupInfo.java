package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/1.
 */
@DatabaseTable(tableName = "group_info")
public class GroupInfo implements Serializable {

    @DatabaseField(id = true, unique = true)
    @Tag(1)
    public String GroupID;

    @DatabaseField
    @Tag(2)
    public String GroupName;

    @DatabaseField
    @Tag(3)
    public String Description;

    @DatabaseField
    @Tag(4)
    public long CreateTime;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    @Tag(5)
    public ArrayList<GroupUserInfo> GroupUsers;

    @DatabaseField
    @Tag(6)
    public String Creator;

    @DatabaseField
    @Tag(7)
    public int Status; //

    @DatabaseField
    @Tag(8)
    public boolean IsEdit;

    @DatabaseField
    @Tag(9)
    public long LastModifyTime;

    @DatabaseField
    @Tag(10)
    public String AnnouncementID;

    @DatabaseField
    @Tag(11)
    public String AnnouncementText;

    @DatabaseField
    @Tag(12)
    public String currentUserId;

    public GroupInfo() {
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "GroupID='" + GroupID + '\'' +
                ", GroupName='" + GroupName + '\'' +
                ", Description='" + Description + '\'' +
                ", CreateTime=" + CreateTime +
                " GroupUsers.Size = " + (GroupUsers == null ? 0 : GroupUsers.size()) +
                ", GroupUsers=" + GroupUsers +
                ", Creator='" + Creator + '\'' +
                ", Status=" + Status +
                ", IsEdit=" + IsEdit +
                ", LastModifyTime=" + LastModifyTime +
                ", AnnouncementID='" + AnnouncementID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupInfo groupInfo = (GroupInfo) o;

        return GroupID != null ? GroupID.equals(groupInfo.GroupID) : groupInfo.GroupID == null;

    }

    public void updateProfile(String itemField, String itemVal) {
        if ("GroupName".equals(itemField)) {
            this.GroupName = itemVal;
        } else if ("Description".equals(itemField)) {
            this.Description = itemVal;
        } else if ("IsEdit".equals(itemField)) {
            this.IsEdit = Boolean.getBoolean(itemVal);
        } else if ("AnnouncementText".equals(itemField)) {
            this.AnnouncementText = itemVal;
        } else if ("AnnouncementID".equals(itemField)) {
            this.AnnouncementID = itemVal;
        } else if ("Status".equals(itemField)) {
            this.Status = Integer.valueOf(itemVal);
        }
    }
}
