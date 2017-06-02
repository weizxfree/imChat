package com.itutorgroup.tutorchat.phone.domain.request.v2;

import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;
import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class GetUserSettingRequest extends CommonRequest implements IGetTickListener {
    @Tag(4)
    public long Ticks;

    @Override
    public void loadTicks() {
        this.Ticks = TicksUtil.getUserSettingTick();
    }

    @Override
    public String toString() {
        return "GetUserSettingRequest{" +
                "Ticks=" + Ticks +
                '}';
    }
}
