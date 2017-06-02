package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/16.
 */
@DatabaseTable(tableName = "user_info")
public class UserInfo implements Serializable {

    @DatabaseField
    @Tag(1)
    public String CompanyID;

    @DatabaseField
    @Tag(2)
    public String CompanyName;

    @DatabaseField
    @Tag(3)
    public String Title;

    @DatabaseField
    @Tag(4)
    public String EnglishTitle;

    @DatabaseField
    @Tag(5)
    public String TitlePosition;

    @DatabaseField
    @Tag(6)
    public String DepartmentGroup;

    @DatabaseField
    @Tag(7)
    public String Department;

    @DatabaseField
    @Tag(8)
    public String ChineseName;

    @DatabaseField
    @Tag(9)
    public String Sex;

    @DatabaseField
    @Tag(10)
    public String StaffSn;

    @DatabaseField
    @Tag(11)
    public String CardNo;

    @DatabaseField
    @Tag(12)
    public String Name;

    @DatabaseField(unique = true, id = true)
    @Tag(13)
    public String UserID;

    @DatabaseField
    @Tag(14)
    public String DeptSn;

    @DatabaseField
    @Tag(15)
    public String CompanyEmail;

    @DatabaseField
    @Tag(16)
    public String Birthday;

    @DatabaseField
    @Tag(17)
    public String Ext;

    @DatabaseField
    @Tag(18)
    public String PositionLevel;

    @DatabaseField
    @Tag(19)
    public String Area;

    @DatabaseField
    @Tag(20)
    public String Indate;

    @DatabaseField
    @Tag(21)
    public String ADAccount;

    @DatabaseField
    @Tag(22)
    public String Image;

    @DatabaseField
    @Tag(23)
    public long LastModifyTime;

    @DatabaseField
    @Tag(24)
    public String UserMessage;

    @DatabaseField
    @Tag(25)
    public boolean IsDisturb; // 消息免打扰

    @DatabaseField
    @Tag(26)
    public boolean IsShield; // 我有没有屏蔽他

    @Tag(27)
    public int AppId;

    @DatabaseField
    @Tag(28)
    public String currentUserId;

    public UserInfo() {

    }

    public UserInfo(String companyID, String companyName, String title, String englishTitle, String titlePosition, String departmentGroup, String department, String chineseName, String sex, String staffSn, String cardNo, String name, String userID, String deptSn, String companyEmail, String birthday, String ext, String positionLevel, String area, String indate, String ADAccount, String image, long lastModifyTime, String userMessage, boolean isDisturb, boolean isShield) {
        CompanyID = companyID;
        CompanyName = companyName;
        Title = title;
        EnglishTitle = englishTitle;
        TitlePosition = titlePosition;
        DepartmentGroup = departmentGroup;
        Department = department;
        ChineseName = chineseName;
        Sex = sex;
        StaffSn = staffSn;
        CardNo = cardNo;
        Name = name;
        UserID = userID;
        DeptSn = deptSn;
        CompanyEmail = companyEmail;
        Birthday = birthday;
        Ext = ext;
        PositionLevel = positionLevel;
        Area = area;
        Indate = indate;
        this.ADAccount = ADAccount;
        Image = image;
        LastModifyTime = lastModifyTime;
        UserMessage = userMessage;
        IsDisturb = isDisturb;
        IsShield = isShield;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "CompanyID='" + CompanyID + '\'' +
                ", CompanyName='" + CompanyName + '\'' +
                ", Title='" + Title + '\'' +
                ", EnglishTitle='" + EnglishTitle + '\'' +
                ", TitlePosition='" + TitlePosition + '\'' +
                ", DepartmentGroup='" + DepartmentGroup + '\'' +
                ", Department='" + Department + '\'' +
                ", ChineseName='" + ChineseName + '\'' +
                ", Sex='" + Sex + '\'' +
                ", StaffSn='" + StaffSn + '\'' +
                ", CardNo='" + CardNo + '\'' +
                ", Name='" + Name + '\'' +
                ", UserID='" + UserID + '\'' +
                ", DeptSn='" + DeptSn + '\'' +
                ", CompanyEmail='" + CompanyEmail + '\'' +
                ", Birthday='" + Birthday + '\'' +
                ", Ext='" + Ext + '\'' +
                ", PositionLevel='" + PositionLevel + '\'' +
                ", Area='" + Area + '\'' +
                ", Indate='" + Indate + '\'' +
                ", ADAccount='" + ADAccount + '\'' +
                ", Image='" + Image + '\'' +
                ", LastModifyTime=" + LastModifyTime +
                ", UserMessage='" + UserMessage + '\'' +
                ", IsDisturb=" + IsDisturb +
                ", IsShield=" + IsShield +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        return UserID != null ? UserID.equals(userInfo.UserID) : false;

    }
}
