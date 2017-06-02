package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


@DatabaseTable(tableName = "push_info")
public class PushInfoModel implements Serializable {


    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField()
    public String  TargetId;

    @DatabaseField
    public String CurrentUserId;


}
