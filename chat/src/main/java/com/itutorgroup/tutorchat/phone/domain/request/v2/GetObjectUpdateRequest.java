package com.itutorgroup.tutorchat.phone.domain.request.v2;

import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.ObjectIdTime;
import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;
import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class GetObjectUpdateRequest extends CommonRequest implements IGetTickListener {
    @Tag(4)
    public int tableName;

    @Tag(5)
    public ArrayList<ObjectIdTime> objectIdTime;

    public GetObjectUpdateRequest() {

    }

    @Override
    public void loadTicks() {
        if (objectIdTime != null && objectIdTime.size() != 0) {
            for (ObjectIdTime item : objectIdTime) {
                switch (tableName) {
                    case ObjectUpdateHelper.TABLE_NAME_USER:
                    case ObjectUpdateHelper.TABLE_NAME_GROUP:
                        item.ticks = TicksUtil.getTicks(item.itemId);
                        break;
                    case ObjectUpdateHelper.TABLE_NAME_CONTACTS:
                        item.ticks = TicksUtil.getContactsListTick();
                        break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "GetObjectUpdateRequest{" +
                "tableName=" + tableName +
                ", objectIdTime=" + objectIdTime +
                '}';
    }
}
