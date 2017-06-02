package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * @deprecated
 * Created by tom_zxzhang on 2016/8/16.
 */
@DatabaseTable(tableName = "user_setting")
public class UserSetting implements Serializable {

    @DatabaseField(id = true, unique = true)
    public String id;

    @DatabaseField
    public String CurrentUserId;

    @DatabaseField
    public String targetId; // useid，grouid，null

    @DatabaseField(defaultValue = "1")
    public int isSound;

    @DatabaseField(defaultValue = "1")
    public int isVibrate;

    @DatabaseField(defaultValue = "1")
    public int newMessageWarn;//新消息提醒

    @DatabaseField(defaultValue = "false")
    public boolean IsDisturb; // 消息免打扰

    @DatabaseField(defaultValue = "false")
    public boolean IsShield; // 屏蔽
}
