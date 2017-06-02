package com.itutorgroup.tutorchat.phone.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.request.ReceiveMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.request.VerifyMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.domain.response.ReceiveMessageResponse;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by tom_zxzhang on 2016/8/31.
 */
public class ReceiveService extends IntentService {

    private Context mContext;

    private EventBus eventBus;

    ScheduledExecutorService scheduledThreadPool;


    public ReceiveService() {
        super("ReceiveService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = LPApp.getInstance();
        eventBus = EventBusManager.getInstance();
        eventBus.register(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void setIntentRedelivery(boolean enabled) {
        super.setIntentRedelivery(enabled);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Intent是从Activity发过来的，携带识别参数，根据参数不同执行不同的任务
        initScheduledMessage();
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();

    }


    private synchronized void ReceiveMessageTask() {

        /**
         * 当处于后台时候，不再拉消息，仅仅在前台时候才拉消息
         */
        if(!AppUtils.isRunningForegroundAndScreenOn(LPApp.getInstance())){
                return;
        }
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
        receiveMessageRequest.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        receiveMessageRequest.UserID = AccountManager.getInstance().getCurrentUserId();
        receiveMessageRequest.Token = AccountManager.getInstance().getToken();
        new RequestHandler<ReceiveMessageResponse>()
                .operation(Operation.RECEIVE_MESSAGE)
                .bundle(new NBundle().ignoreResponseLog().build())
                .request(receiveMessageRequest)
                .exec(ReceiveMessageResponse.class, new RequestHandler.RequestListener<ReceiveMessageResponse>() {
                    @Override
                    public void onResponse(ReceiveMessageResponse receiveMessageResponse, Bundle bundle) {
                        if (TextUtils.isEmpty(receiveMessageResponse.ReceiptID) || StringUtils.isBlank(receiveMessageResponse.MessageList)) {
                            return;
                        }
                        VerifyMessageRequest(receiveMessageResponse.ReceiptID, receiveMessageResponse);
                    }
                });
    }


    private synchronized void VerifyMessageRequest(String receiptID, final ReceiveMessageResponse receiveMessageResponse) {
        VerifyMessageRequest requeset = new VerifyMessageRequest();
        requeset.receiptID = receiptID;
        requeset.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        requeset.UserID = AccountManager.getInstance().getCurrentUserId();
        requeset.Token = AccountManager.getInstance().getToken();

        new RequestHandler<CommonResponse>()
                .operation(Operation.VERIFY_MESSAGE)
                .request(requeset)
                .bundle(new NBundle().ignoreResponseLog().build())
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse commonResponse, Bundle bundle) {
                        MessageManager.getInstance().SaveAndPostMessage(receiveMessageResponse.MessageList);
                    }
                });
    }


    private void initScheduledMessage() {
        scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            public void run() {
                ReceiveMessageTask();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    public class MsgBinder extends Binder {
        public ReceiveService getService() {
            return ReceiveService.this;
        }
    }


    public void shutdown() {
        scheduledThreadPool.shutdownNow();
    }



}
