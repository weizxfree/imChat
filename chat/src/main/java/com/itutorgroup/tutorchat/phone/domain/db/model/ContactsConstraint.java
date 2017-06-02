package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by joyinzhao on 2016/9/9.
 */
@DatabaseTable(tableName = "contacts_constraint")
public class ContactsConstraint {
    @DatabaseField(id = true, unique = true)
    public String Id;

    @DatabaseField
    public String currentUserId;

    @DatabaseField
    public String targetUserId;

    public ContactsConstraint() {

    }
}
