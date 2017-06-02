package com.itutorgroup.tutorchat.phone.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.event.GlobalActionEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.response.GetUserResponse;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ServiceAccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class GlobalService extends IntentService {
    public static final String ACTION_APP_CRASH_HAPPENED = "action_app_crash_happened";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GlobalService(String name) {
        super(name);
    }

    public GlobalService() {
        super("GlobalService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (GlobalActionEvent.ACTION_AUTO_REFRESH_CURRENT_USER_INFO.equals(action)) {
            refreshCurrentUserInfo();
        } else if (GlobalActionEvent.ACTION_AUTO_GET_SERVICE_ACCOUNT_LIST.equals(action)) {
            ServiceAccountManager.getInstance().autoLoadServiceAccountList();
        } else if (ACTION_APP_CRASH_HAPPENED.equals(action)) {
            collectCrashInfo(intent);
        }
    }

    private void collectCrashInfo(Intent intent) {
        String cacheName = intent.getStringExtra("crash_cache_name");
        String log = AppPrefs.get(LPApp.getInstance()).getString(cacheName, null);
        if (!TextUtils.isEmpty(log)) {
            AppPrefs.get(LPApp.getInstance()).remove(cacheName);
            AppManager.getInstance().uploadCrashLog(log);
        }
    }

    private void refreshCurrentUserInfo() {
        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        ContactsManager.getInstance().getUserInfo(currentUserId, 0, new RequestHandler.RequestListener<GetUserResponse>() {
            @Override
            public void onResponse(GetUserResponse response, Bundle bundle) {
                if (response.User != null && !TextUtils.isEmpty(response.User.UserID)) {
                    AccountManager.getInstance().setCurrentUser(response.User);
                    EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(response.User));
                }
            }
        });
    }
}
