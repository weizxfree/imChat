package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.NotificationManager;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tom_zxzhang on 2016/9/6.
 */
public class PushInfoManager {


    private static PushInfoManager pushInfoManager;

    public static PushInfoManager getInstance() {
        if (pushInfoManager == null) {
            synchronized (PushInfoManager.class) {
                if (pushInfoManager == null) {
                    pushInfoManager = new PushInfoManager();
                }
            }
        }
        return pushInfoManager;
    }


    /**
     * 进入聊天页面时候，通知栏如果有未读数，则全部清空，取消通知栏。
     *
     * @param targetId
     */
    public static void CheckNotificationStatus(final String targetId) {

        rx.Observable.just(targetId).subscribeOn(Schedulers.io())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String id) {
                        List<MessageModel> list = MessageDao.getInstance().getAllUnreadMessagesByTargetID(id);
                        return list == null || list.size() == 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        NotificationManager mNotificationManager = (NotificationManager) LPApp.getInstance().getSystemService(LPApp.getInstance().NOTIFICATION_SERVICE);
                        mNotificationManager.cancel(AccountManager.getInstance().getCurrentUserId() + targetId, 0);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public static void clearTargetNotification(String targetId) {
        rx.Observable.just(targetId).subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String targetId) {
                        NotificationManager mNotificationManager = (NotificationManager) LPApp.getInstance().getSystemService(LPApp.getInstance().NOTIFICATION_SERVICE);
                        mNotificationManager.cancel(AccountManager.getInstance().getCurrentUserId() + targetId, 0);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public void refreshNotificationStatus(List<String> msgIdList) {
        if (msgIdList == null || msgIdList.size() == 0) {
            return;
        }
        Observable.from(msgIdList)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, MessageModel>() {
                    @Override
                    public MessageModel call(String s) {
                        MessageModel model = null;
                        try {
                            model = MessageDao.getInstance().SelectMessageModelByMessageId(s);
                        } catch (Exception e) {
                            LogUtil.exception(e);
                        }
                        return model;
                    }
                })
                .filter(new Func1<MessageModel, Boolean>() {
                    @Override
                    public Boolean call(MessageModel model) {
                        return model != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MessageModel>() {
                    @Override
                    public void call(MessageModel model) {
                        CheckNotificationStatus(model.targetId);
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

}
