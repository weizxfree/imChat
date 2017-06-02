package com.itutorgroup.tutorchat.phone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity;
import com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity;
import com.itutorgroup.tutorchat.phone.domain.db.dao.PushInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;

import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by tom_zxzhang on 2016/9/26.
 */
public class NotificationClickReceiver extends BroadcastReceiver {

    public static String MAIN_ACTIVITY_NAME = "com.itutorgroup.tutorchat.phone.activity.MainActivity";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (StringUtils.isNotBlank(intent) && intent.hasExtra("type")) {
            int type = intent.getIntExtra("type", PushReceiver.isSingleChat);
            String targetId = null;
            if (StringUtils.isNotBlank(intent) && intent.hasExtra("targetId")) {
                targetId = intent.getStringExtra("targetId");
            }
            if (StringUtils.isNotEmpty(targetId)) {
                if (type == PushReceiver.isGroupChat) {
                    context.startActivity(new Intent(context, GroupChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("GroupId", targetId));
                } else {
                    UserInfoManager.getInstance().getUserInfo(targetId, new CommonLoadingListener<UserInfo>() {
                        @Override
                        public void onResponse(UserInfo userInfo) {
                            context.startActivity(new Intent(context, SingleChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("user_id", userInfo.UserID));
                        }
                    });
                }
            }


        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNotBlank(intent)) {
                    String targetId = intent.getStringExtra("targetId");
                    PushInfoDao.getInstance().remove(targetId);
                }
            }
        }).start();
    }
}
