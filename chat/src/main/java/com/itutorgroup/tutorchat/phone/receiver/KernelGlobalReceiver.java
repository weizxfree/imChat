package com.itutorgroup.tutorchat.phone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.itutorgroup.tutorchat.phone.service.DataService;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;

/**
 * Created by joyinzhao on 2016/11/1.
 */
public class KernelGlobalReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Kernel.getInstance().isTcpEnabled()) {
            return;
        }
        String action = intent.getAction();
        Intent serviceIntent = new Intent(context, DataService.class);
        if (action.equals(AccountManager.ACTION_USER_LOGOUT)) {
            serviceIntent.putExtra(DataService.CMD_SHUTDOWN, true);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean flag = AccountManager.getInstance().loadLoginData();
            if (flag && AppUtils.hasNetwork()) {
                serviceIntent.putExtra(DataService.CMD_CLEAR_RETRY_COUNT, true);
            }
        }
        context.startService(serviceIntent);
    }
}
