package com.itutorgroup.tutorchat.phone.domain.response.v2;

import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.ObjectUpdate;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import java.util.ArrayList;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/17.
 */
public class GetObjectUpdateResponse extends CommonResponse implements ISetTickListener {

    @Tag(3)
    public ArrayList<ObjectUpdate> updateObjects;

    public GetObjectUpdateResponse() {

    }

    @Override
    public void setTicks() {
        if (updateObjects != null && updateObjects.size() > 0) {
            for (ObjectUpdate item : updateObjects) {
                switch (item.tableName) {
                    case ObjectUpdateHelper.TABLE_NAME_USER:
                    case ObjectUpdateHelper.TABLE_NAME_GROUP:
                        TicksUtil.setTicks(item.tableObjId, item.updateTime);
                        break;
                    case ObjectUpdateHelper.TABLE_NAME_CONTACTS:
                        TicksUtil.setContactsListTick(item.updateTime);
                        break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "GetObjectUpdateResponse{" +
                "updateObjects=" + updateObjects +
                '}';
    }
}
