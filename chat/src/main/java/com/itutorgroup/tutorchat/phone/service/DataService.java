package com.itutorgroup.tutorchat.phone.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.itutorgroup.tutorchat.phone.IKernelServiceInterface;
import com.itutorgroup.tutorchat.phone.INotifyCallBack;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.kernel.DataThread;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.permission.platform.AutoStartUtil;

import org.apache.commons.codec.binary.Base64;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/20.
 */
public class DataService extends Service {
    private static final String TAG = "DataService";

    public static final String CMD_SHUTDOWN = "data-service-shutdown";
    public static final String CMD_CLEAR_RETRY_COUNT = "clear-retry-count";

    private RemoteCallbackList<INotifyCallBack> mCallBacks = new RemoteCallbackList<>();
    private DataThread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Kernel.getInstance().isTcpEnabled()) {
            return;
        }
        startTcp();
    }

    private void startTcp() {
        boolean flag = AccountManager.getInstance().loadLoginData();
        if (flag && AppUtils.hasNetwork()) {
            mThread = new DataThread();
            mThread.start();
            AutoStartUtil.init(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Kernel.getInstance().isTcpEnabled()) {
            Observable.just(intent)
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<Intent>() {
                        @Override
                        public void call(Intent intent) {
                            boolean shutdown = false;
                            boolean clearRetryCount = false;
                            if (intent != null) {
                                shutdown = intent.getBooleanExtra(CMD_SHUTDOWN, false);
                                clearRetryCount = intent.getBooleanExtra(CMD_CLEAR_RETRY_COUNT, false);
                            }
                            if (shutdown) {
                                shutdown();
                                stopSelf();
                            } else if (mThread == null) {
                                startTcp();
                            } else if (clearRetryCount) {
                                mThread.resumeTcpIfDied();
                            } else {
                                mThread.startNetty();
                            }
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void shutdown() {
//        AccountManager.getInstance().clearAccountData();
        if (mThread != null) {
            mThread.preShutDown(false);
        }
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        if (mThread != null) {
//            mThread.preShutDown(false);
//        }
        return super.onUnbind(intent);
    }

    private final IKernelServiceInterface.Stub mBinder = new IKernelServiceInterface.Stub() {

        @Override
        public void registerCallBack(INotifyCallBack cb) throws RemoteException {
            if (cb != null) {
                mCallBacks.register(cb);
            }
        }

        @Override
        public void unRegisterCallBack(INotifyCallBack cb) throws RemoteException {
            if (cb != null) {
                mCallBacks.unregister(cb);
            }
        }

        @Override
        public void sendMessage(String stringBase64) throws RemoteException {
            if (mThread != null) {
                mThread.sendMessage(stringBase64);
            }
        }

        @Override
        public boolean isTcpOnline() throws RemoteException {
            if (mThread != null) {
                return mThread.isTcpOnline();
            }
            return false;
        }

        @Override
        public boolean isTcpConnect() throws RemoteException {
            if (mThread != null) {
                return mThread.isTcpConnect();
            }
            return false;
        }

        @Override
        public void resumeTcpIfDied() throws RemoteException {
            if (mThread != null) {
                mThread.resumeTcpIfDied();
            }
        }
    };

    private void notifyOnDataReceived(byte[] b) {
        String stringBase64 = new String(Base64.encodeBase64(b));
        final int len = mCallBacks.beginBroadcast();
        for (int i = 0; i < len; i++) {
            try {
                mCallBacks.getBroadcastItem(i).onDataReceived(stringBase64);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallBacks.finishBroadcast();
    }

    @Override
    public void onDestroy() {
        mCallBacks.kill();
        super.onDestroy();
    }
}
