package com.itutorgroup.tutorchat.phone.utils.kernel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.IKernelServiceInterface;
import com.itutorgroup.tutorchat.phone.INotifyCallBack;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.service.DataService;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by joyinzhao on 2016/10/20.
 */
public class Kernel {
    private static Kernel sInstance;

    private IKernelServiceInterface mService;

    private boolean mEnableTcp = true;
    private boolean mEnablePullService = false;

    public static Kernel getInstance() {
        if (sInstance == null) {
            synchronized (Kernel.class) {
                if (sInstance == null) {
                    sInstance = new Kernel();
                }
            }
        }
        return sInstance;
    }

    public boolean isTcpEnabled() {
        return mEnableTcp;
    }

    public boolean isPullEnabled() {
        return mEnablePullService;
    }

    public void startTcpService(Context context) {
        if (!mEnableTcp && !TextUtils.isEmpty(AccountManager.getInstance().getCurrentUserId())) {
            return;
        }
        try {
            mTcpIntent = new Intent(context, DataService.class);
//            LPApp.getInstance().bindService(mTcpIntent, mConnection, Context.BIND_AUTO_CREATE);
            LPApp.getInstance().getApplicationContext().startService(mTcpIntent);
        } catch (Exception e) {
        }
    }

    public void startTcpByAlarm(Context context) {
        if (!mEnableTcp || !TextUtils.isEmpty(AccountManager.getInstance().getCurrentUserId())) {
            return;
        }
        try {
            Intent intent = new Intent(context, DataService.class);
            intent.putExtra(AccountManager.PK_TOKEN, AccountManager.getInstance().getToken());
            intent.putExtra(AccountManager.PK_USER_INFO, AccountManager.getInstance().getCurrentUser());
            PendingIntent pendingIntent = PendingIntent.getService(LPApp.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager) LPApp.getInstance()
                    .getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    pendingIntent);
        } catch (Exception e) {
        }
    }

    public void stopTcpService() {
        try {
////            LPApp.getInstance().unbindService(mConnection);
            if (mTcpIntent != null) {
                LPApp.getInstance().stopService(mTcpIntent);
                mTcpIntent = null;
            }
        } catch (Exception e) {

        }
        LPApp.getInstance().sendBroadcast(new Intent(AccountManager.ACTION_USER_LOGOUT));
    }

    private Intent mTcpIntent;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IKernelServiceInterface.Stub.asInterface(service);
            try {
                mService.registerCallBack(mNotifyCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unRegisterCallBack(mNotifyCallBack);
            } catch (RemoteException e) {
            }
            mService = null;
        }


    };

    private INotifyCallBack mNotifyCallBack = new INotifyCallBack.Stub() {

        @Override
        public void onDataReceived(String stringBase64) throws RemoteException {
            byte[] base64Bytes = Base64.decodeBase64(stringBase64.getBytes());
            LogUtil.d("kernel : " + new String(base64Bytes));
        }
    };

    public void sendMessage(String message) {
        byte[] b = message.getBytes();
        String stringBase64 = new String(Base64.encodeBase64(b));
        try {
            mService.sendMessage(stringBase64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTcpOnline(IKernelServiceInterface iKernel) {
        if (iKernel != null) {
            try {
                return iKernel.isTcpOnline();
            } catch (Exception e) {
                LogUtil.exception(e);
            }
        }
        return false;
    }

    public boolean isTcpConnect(IKernelServiceInterface iKernel) {
        if (iKernel != null) {
            try {
                return iKernel.isTcpConnect();
            } catch (Exception e) {
                LogUtil.exception(e);
            }
        }
        return false;

    }

    public boolean isTcpReceiveOfflineMessage(IKernelServiceInterface iKernel) {
        if (iKernel != null) {
            try {
                boolean connect = iKernel.isTcpConnect();
                boolean online = iKernel.isTcpOnline();
                return connect && !online;
            } catch (Exception e) {
                LogUtil.exception(e);
            }
        }
        return false;
    }

    public void resumeTcpIfDied(IKernelServiceInterface iKernel) {
        if (iKernel != null) {
            try {
                iKernel.resumeTcpIfDied();
            } catch (Exception e) {
                LogUtil.exception(e);
            }
        }
    }
}
