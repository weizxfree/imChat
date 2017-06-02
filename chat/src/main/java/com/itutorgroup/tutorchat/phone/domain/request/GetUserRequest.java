package com.itutorgroup.tutorchat.phone.domain.request;

import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class GetUserRequest extends CommonRequest implements IGetTickListener {

    @Tag(4)
    public String QueryUserID;

    @Tag(5)
    public long Ticks;

    @Override
    public void loadTicks() {
        this.Ticks = TicksUtil.getTicks(QueryUserID);
    }
}
