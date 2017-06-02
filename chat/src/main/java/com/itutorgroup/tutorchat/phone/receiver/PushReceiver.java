package com.itutorgroup.tutorchat.phone.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.igexin.sdk.PushConsts;
import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.PushInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.request.ConnectRequest;
import com.itutorgroup.tutorchat.phone.domain.response.ConnectResponse;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PushReceiver extends BroadcastReceiver {
    public static final String TAG = PushReceiver.class.getSimpleName();
    public static final String CMD_MSG_REFRESH_PUSH_NOTIFICATION = BuildConfig.APPLICATION_ID + ".refresh_push_notification";
    private static long mLastTime = 0;
    public static final int isSingleChat = 1;
    public static final int isGroupChat = 2;
    private PowerManager.WakeLock m_wakeLockObj = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppUtils.isRunningForegroundAndScreenOn(context))
            return;
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(PushConsts.CMD_ACTION)) {
            switch (bundle.getInt(PushConsts.CMD_ACTION)) {
                case PushConsts.GET_MSG_DATA:
                   /*
                    byte[] payload = bundle.getByteArray("payload");
                    String taskid = bundle.getString("taskid");
                    String messageid = bundle.getString("messageid");
                    boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
                    try {
                        String data = new String(payload);
                        if (StringUtils.isNotEmpty(data)) {
                            JSONObject myJsonObject = new JSONObject(data);
                            String id = myJsonObject.getString("GroupID");
                            String content = myJsonObject.getString("Content");
                            String title = myJsonObject.getString("Title");
                            saveAndShowNotification(context, content, title, id, 0);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    */
                    break;

                case PushConsts.GET_CLIENTID:
                    String cid = bundle.getString("clientid");
                    AccountManager.getInstance().setClientId(cid);
                    ConnectRequest connectRequest = new ConnectRequest(
                            Constant.MESSAGE_DEVICE_TYPE, AccountManager.getInstance().getCurrentUserId(), AccountManager.getInstance().getToken(), cid, Constant.DEVICE_TYPE);
                    connectRequest.LanguageType = AppManager.getInstance().getCurrentLanguageType();
                    ConnectAsyncTask(connectRequest);
                    break;
            }
        } else {
            parseActionData(context, intent);
        }
    }

    private void parseActionData(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(CMD_MSG_REFRESH_PUSH_NOTIFICATION)) {
//            List<String> idList = PushInfoDao.getInstance().getAllPushInfo();
            List<String> idList = intent.getStringArrayListExtra("target_id_list");
            if (idList != null && idList.size() > 0) {
                Observable.just(idList)
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<List<String>, List<MessageModel>>() {
                            @Override
                            public List<MessageModel> call(List<String> list) {
                                return MessageDao.getInstance().selectChatList(list);
                            }
                        })
                        .filter(new Func1<List<MessageModel>, Boolean>() {
                            @Override
                            public Boolean call(List<MessageModel> modelList) {
                                return modelList != null && modelList.size() > 0;
                            }
                        })
                        .flatMap(new Func1<List<MessageModel>, Observable<MessageModel>>() {
                            @Override
                            public Observable<MessageModel> call(List<MessageModel> modelList) {
                                return Observable.from(modelList);
                            }
                        })
                        .filter(new Func1<MessageModel, Boolean>() {
                            @Override
                            public Boolean call(MessageModel model) {
                                return MessageDao.getInstance().getConversationUnreadMessageCount(model.targetId) > 0 || model.Type == MessageType.SYSTEM_MESSAGE;
                            }
                        })
                        .filter(new Func1<MessageModel, Boolean>() {
                            @Override
                            public Boolean call(MessageModel model) {
                                return !UserSettingManager.getInstance().isTargetIsDisturb(model.targetId) && !UserSettingManager.getInstance().isTargetIsShield(model.targetId);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<MessageModel>() {
                            @Override
                            public void call(final MessageModel model) {
                                if (TextUtils.isEmpty(model.GroupId)) {
                                    UserInfoManager.getInstance().getUserInfo(model.targetId, new CommonLoadingListener<UserInfo>() {
                                        @Override
                                        public void onResponse(final UserInfo userInfo) {
                                            dispatchNotificationMessage(context, model, userInfo.Name);
                                        }
                                    });
                                } else {
                                    GroupManager.getInstance().getGroupInfo(model.GroupId, new CommonLoadingListener<GroupInfo>() {
                                        @Override
                                        public void onResponse(GroupInfo groupInfo) {
                                            GroupManager.getInstance().formatGroupNameOnce(groupInfo, new CommonLoadingListener<String>() {
                                                @Override
                                                public void onResponse(String s) {
                                                    dispatchNotificationMessage(context, model, s);
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        }
    }

    private void dispatchNotificationMessage(final Context context, final MessageModel model, final String name) {
        MessageManager.getInstance().getMessageNotificationText(model, new CommonLoadingListener<String>() {
            @Override
            public void onResponse(String content) {
                String id = model.targetId;
                String title = name;
                int type = -1;
                if (StringUtils.isEmpty(model.GroupId)) {
                    type = isSingleChat;
                } else {
                    type = isGroupChat;
                }
                showNotification(context, content, title, id, type);
            }
        });
    }

    /**
     * @param context
     */
    private void saveAndShowNotification(Context context, String content, String title, String id, int type) {
        if (StringUtils.isEmpty(id))
            return;
        PushInfoDao.getInstance().add(id);
        showNotification(context, content, title, id, 0);
    }

    private void showNotification(Context context, String content, String title, String id, int type) {
        /**
         * 当处于前台时候不需要显示通知栏
         */
        if (AppUtils.isRunningForegroundAndScreenOn(context))
            return;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(LPApp.getInstance());
        mBuilder
                .setContentTitle(title)//设置通知栏标题
                .setContentText(FaceConversionUtil.getInstace().getExpressionZhFromServer(content))
                .setContentIntent(getDefalutIntent(context, id, type)) //设置通知栏点击意图
                .setNumber(MessageDao.getInstance().getConversationUnreadMessageCount(id)) //设置通知集合的数量
                .setTicker(content) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
//                .Notification.DEFAULT_ALL  //Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.drawable.push_small_icon);//设置通知小ICON

        boolean isFast = checkMessageIsFast();
        int flag = 0;
        if (!AppUtils.isRunningForegroundAndScreenOn(LPApp.getInstance()) && AccountManager.getInstance().isSoundEnabled() && !isFast) {
            flag |= Notification.DEFAULT_SOUND;
        }
        if (!AppUtils.isRunningForegroundAndScreenOn(LPApp.getInstance()) && AccountManager.getInstance().isVibrateEnabled() && !isFast) {
            flag |= Notification.DEFAULT_VIBRATE;
        }
        mBuilder.setDefaults(flag);
        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        Notification notify = mBuilder.build();
        /*
        * 如果直接设置setLargeIcon会导致smallIcon显示在右侧，所以可以通过设置contentview来间接实现
        * 后面有可能要重新设置largeIcon尺寸，只需要更换此处图片即可
        * */
        notify.contentView.setImageViewResource(android.R.id.icon, R.drawable.push);
        mNotificationManager.notify(AccountManager.getInstance().getCurrentUserId() + id, 0, notify);
        if (!isScreenOn()) {
            acquireWakeLock(0);
            releaseWakeLock();
        }

    }

    public PendingIntent getDefalutIntent(Context mContext, String id, int type) {
        Intent clickIntent = new Intent(mContext, NotificationClickReceiver.class);
        clickIntent.putExtra("targetId", id);
        clickIntent.putExtra("type", type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(LPApp.getInstance(), id.hashCode(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    private void ConnectAsyncTask(ConnectRequest connectRequest) {
        new RequestHandler<ConnectResponse>()
                .operation(Operation.CONNECT)
                .bundle(new NBundle().addIgnoreToastErrorCode(NetworkError.ERROR_INVALID_USER_ID).build())
                .request(connectRequest)
                .exec(ConnectResponse.class, new RequestHandler.RequestListener<ConnectResponse>() {
                    @Override
                    public void onResponse(ConnectResponse t, Bundle bundle) {
                    }

                    @Override
                    public void onError(int errorCode, ConnectResponse response, Exception e, Bundle bundle) {
                    }

                    @Override
                    public void onNullResponse(Bundle bundle) {
                    }
                });
    }


    private boolean checkMessageIsFast() {
        long now = System.currentTimeMillis();
        if (now - mLastTime < 800) {
            return true;
        } else {
            mLastTime = System.currentTimeMillis();
        }
        return false;
    }

    public void acquireWakeLock(long milltime) {
        if (m_wakeLockObj == null) {
            PowerManager pm = (PowerManager) LPApp.getInstance().getSystemService(Context.POWER_SERVICE);
            m_wakeLockObj = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, TAG);
            m_wakeLockObj.acquire(milltime);
        }
    }


    public void releaseWakeLock() {
        if (m_wakeLockObj != null && m_wakeLockObj.isHeld()) {
            m_wakeLockObj.release();
            m_wakeLockObj = null;
        }
    }

    public boolean isScreenOn() {
        PowerManager pm = (PowerManager) LPApp.getInstance().getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            return true;
        }
        return false;
    }


}
