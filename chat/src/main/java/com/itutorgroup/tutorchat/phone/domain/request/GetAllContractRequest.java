package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by tom_zxzhang on 2016/8/22.
 */
public class GetAllContractRequest extends CommonRequest implements IGetTickListener {

    @Tag(4)
    public long Ticks;


    public GetAllContractRequest() {

    }

    public GetAllContractRequest(int messageDeviceType, String userID, String token) {
        MessageDeviceType = messageDeviceType;
        UserID = userID;
        Token = token;
    }

    @Override
    public void loadTicks() {
        this.Ticks = TicksUtil.getContactsListTick();
    }

    @Override
    public String toString() {
        return "GetAllContractRequest{" +
                "Ticks=" + Ticks +
                '}';
    }
}
