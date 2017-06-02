package com.itutorgroup.tutorchat.phone.utils.daemon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.permission.platform.AutoStartUtil;

/**
 * Created by joyinzhao on 2016/12/16.
 */
public class Service1 extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AutoStartUtil.init(this);
        Kernel.getInstance().startTcpService(this);
        return Service.START_NOT_STICKY;
    }
}
