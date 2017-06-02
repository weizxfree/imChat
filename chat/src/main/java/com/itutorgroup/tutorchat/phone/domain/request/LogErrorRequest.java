package com.itutorgroup.tutorchat.phone.domain.request;

import android.os.Build;

import com.itutorgroup.tutorchat.phone.config.Constant;

import io.protostuff.Tag;

/**
 * Created by joyinzhao on 2016/9/26.
 */
public class LogErrorRequest extends CommonRequest {

    @Tag(4)
    public String Msg;

    @Tag(5)
    public int DeviceType;

    @Tag(6)
    public String SystemType;

    @Tag(7)
    public String Tag;

    public LogErrorRequest() {
        init();
        DeviceType = Constant.MESSAGE_DEVICE_TYPE;
        SystemType = Build.BRAND + " " + Build.MODEL;
    }

    @Override
    public String toString() {
        return "LogErrorRequest{" +
                "Msg='" + Msg + '\'' +
                ", Tag='" + Tag + '\'' +
                '}';
    }
}
