package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


@DatabaseTable(tableName = "group_in_contact")
public class GroupInContactModel implements Serializable {


    @DatabaseField(id = true, unique = true)
    public String id;

    @DatabaseField()
    public String  GroupID;

    @DatabaseField
    public String CurrentUserId;


}
