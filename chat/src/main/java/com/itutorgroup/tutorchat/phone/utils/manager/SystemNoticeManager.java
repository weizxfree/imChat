package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.SystemNoticeDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.SystemNoticeModel;
import com.itutorgroup.tutorchat.phone.domain.event.SystemNoticeEvent;
import com.itutorgroup.tutorchat.phone.domain.request.v2.SystemNoticeRequest;
import com.itutorgroup.tutorchat.phone.domain.response.v2.SystemNoticeResponse;
import com.itutorgroup.tutorchat.phone.receiver.MainGlobalReceiver;
import com.itutorgroup.tutorchat.phone.service.GlobalService;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/11/24.
 */
public class SystemNoticeManager {
    private static SystemNoticeManager sInstance;

    public static SystemNoticeManager getInstance() {
        if (sInstance == null) {
            synchronized (SystemNoticeManager.class) {
                if (sInstance == null) {
                    sInstance = new SystemNoticeManager();
                }
            }
        }
        return sInstance;
    }

    private SystemNoticeManager() {
    }

    public void loadSystemNotice() {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<SystemNoticeManager, SystemNoticeModel>() {
                    @Override
                    public SystemNoticeModel call(SystemNoticeManager systemNoticeManager) {
                        return SystemNoticeDao.getInstance().getActiveSystemNotice();
                    }
                })
                .subscribe(new Action1<SystemNoticeModel>() {
                    @Override
                    public void call(SystemNoticeModel model) {
                        setExpiredAlarm(model);
                        EventBusManager.getInstance().post(new SystemNoticeEvent(model));
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void setExpiredAlarm(SystemNoticeModel model) {
        if (model != null) {
            long time = model.ExpiredTime;
            Intent intent = new Intent(MainGlobalReceiver.ACTION_SYSTEM_NOTICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(LPApp.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager mgr = (AlarmManager) LPApp.getInstance()
                    .getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, time, pendingIntent);
        }
    }

    public void getSystemNoticeHistory(final CommonLoadingListener<List<SystemNoticeModel>> listener) {
        Observable.just(this)
                .subscribeOn(Schedulers.io())
                .map(new Func1<SystemNoticeManager, List<SystemNoticeModel>>() {
                    @Override
                    public List<SystemNoticeModel> call(SystemNoticeManager systemNoticeManager) {
                        return SystemNoticeDao.getInstance().querySystemNoticeHistory();
                    }
                })
                .filter(new Func1<List<SystemNoticeModel>, Boolean>() {
                    @Override
                    public Boolean call(List<SystemNoticeModel> list) {
                        return list != null && list.size() > 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<SystemNoticeModel>>() {
                    @Override
                    public void call(List<SystemNoticeModel> list) {
                        listener.onResponse(list);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public void dispatchSystemNotice(SystemNoticeModel model) {
        if (model != null) {
            Observable.just(model)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<SystemNoticeModel>() {
                        @Override
                        public void call(SystemNoticeModel model) {
                            if (model.Status == 2) {
                                SystemNoticeDao.getInstance().remove(model);
                            } else {
                                SystemNoticeDao.getInstance().add(model);
                            }
                            LPApp.getInstance().sendBroadcast(new Intent(MainGlobalReceiver.ACTION_SYSTEM_NOTICE));
                        }
                    }, CommonUtil.ACTION_EXCEPTION);
        }
    }

    public void getSystemNotice(long noticeId) {
        SystemNoticeRequest request = new SystemNoticeRequest();
        request.init();
        request.SystemNoticeId = noticeId;
        new RequestHandler()
                .request(request)
                .operation(Operation.SYSTEM_NOTICE)
                .exec(SystemNoticeResponse.class, new RequestHandler.RequestListener<SystemNoticeResponse>() {
                    @Override
                    public void onResponse(SystemNoticeResponse response, Bundle bundle) {
                        if (response.SystemNotice != null) {
                            dispatchSystemNotice(response.SystemNotice);
                        }
                    }
                });
    }

    public void receiveNoticeMessage(MessageModel messageModel) {
        try {
            Intent intent = new Intent(MainGlobalReceiver.ACTION_RECEIVE_NOTICE_MSG);
            String content = messageModel.Content;
            long id = Long.parseLong(content);
            intent.putExtra("id", id);
            LPApp.getInstance().sendBroadcast(intent);
        } catch (Exception e) {

        }
    }
}
