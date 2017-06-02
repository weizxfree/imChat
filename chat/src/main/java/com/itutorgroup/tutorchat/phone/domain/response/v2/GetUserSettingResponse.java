package com.itutorgroup.tutorchat.phone.domain.response.v2;

import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.TicksUtil;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class GetUserSettingResponse extends CommonResponse implements ISetTickListener {

    @Tag(3)
    public SettingsModel Setting;

    @Override
    public void setTicks() {
        if (Setting != null) {
            TicksUtil.setUserSettingTick(Setting.LastModifyTime);
        }
    }

    @Override
    public String toString() {
        return "GetUserSettingResponse{" +
                "Setting=" + Setting +
                '}';
    }
}
