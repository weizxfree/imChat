package com.itutorgroup.tutorchat.phone.utils.common;

import android.content.Context;
import android.content.Intent;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.event.GlobalActionEvent;
import com.itutorgroup.tutorchat.phone.service.GlobalService;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;

import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.eventbus.Subscribe;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class GlobalActionUtil {

    private static GlobalActionUtil sInstance;

    public static GlobalActionUtil getInstance() {
        if (sInstance == null) {
            synchronized (GlobalActionUtil.class) {
                if (sInstance == null) {
                    sInstance = new GlobalActionUtil();
                }
            }
        }
        return sInstance;
    }

    private EventBus mEventBus;

    public void init() {
        mEventBus = EventBusManager.getInstance();
        mEventBus.register(this);
    }

    @Subscribe
    public void onGlobalActionEvent(GlobalActionEvent event) {
        String action = event.mAction;
        if (GlobalActionEvent.ACTION_AUTO_REFRESH_CURRENT_USER_INFO.equals(action)
                || GlobalActionEvent.ACTION_AUTO_GET_SERVICE_ACCOUNT_LIST.equals(action)) {
            startGlobalServiceWithAction(action);
        }
    }

    private void startGlobalServiceWithAction(String action) {
        Context context = LPApp.getInstance();
        Intent intent = new Intent(context, GlobalService.class);
        intent.setAction(action);
        context.startService(intent);
    }
}
