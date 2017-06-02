package com.itutorgroup.tutorchat.phone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.OnlineEvent;
import com.itutorgroup.tutorchat.phone.domain.event.TcpStateChangeEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SystemNoticeManager;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;

/**
 * Created by joyinzhao on 2016/9/23.
 */
public class MainGlobalReceiver extends BroadcastReceiver {

    public static String ACTION_REQUEST_NETWORK_STATE = BuildConfig.APPLICATION_ID + ".request_net_state";
    public static String ACTION_MESSAGE_RECEIVED = BuildConfig.APPLICATION_ID + ".message_received";
    public static String ACTION_REFRESH_CONVERSATION_LIST = BuildConfig.APPLICATION_ID + ".refresh_conversation_list";
    public static String ACTION_SYSTEM_NOTICE = BuildConfig.APPLICATION_ID + ".system_notice";
    public static String ACTION_RECEIVE_NOTICE_MSG = BuildConfig.APPLICATION_ID + ".receive_notice_msg";
    public static String ACTION_CMD_OBJECT_UPDATE = BuildConfig.APPLICATION_ID + ".object_update_patches";
    public static String ACTION_TCP_ONLINE_STATE = BuildConfig.APPLICATION_ID + ".tcp_online";
    public static String ACTION_KICK_OUT = BuildConfig.APPLICATION_ID + ".kick_out";

    public static String EXTRA_MESSAGE_ID_LIST = "message_id_list";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
                || action.equals(ACTION_REQUEST_NETWORK_STATE)) {
            requestNetworkState(context);
        } else if (action.equals(ACTION_MESSAGE_RECEIVED)) {
            MessageManager.getInstance().notifyReceivedMessage(intent.getStringArrayListExtra(EXTRA_MESSAGE_ID_LIST));
        } else if (action.equals(ACTION_REFRESH_CONVERSATION_LIST)) {
            EventBusManager.getInstance().post(ConversationEvent.getInstance());
        } else if (action.equals(ACTION_SYSTEM_NOTICE)) {
            SystemNoticeManager.getInstance().loadSystemNotice();
        } else if (action.equals(ACTION_RECEIVE_NOTICE_MSG)) {
            long id = intent.getLongExtra("id", 0);
            if (id != 0) {
                SystemNoticeManager.getInstance().getSystemNotice(id);
            }
        } else if (action.equals(ACTION_CMD_OBJECT_UPDATE)) {
            ObjectUpdateHelper.dispatchCmd(intent);
        } else if (action.equals(ACTION_TCP_ONLINE_STATE)) {
            EventBusManager.getInstance().post(TcpStateChangeEvent.getInstance());
            EventBusManager.getInstance().post(OnlineEvent.getInstance());
        } else if (action.equals(ACTION_KICK_OUT)) {
            AccountManager.getInstance().logout();
            int errorCode = intent.getIntExtra(NetworkError.EXTRA_ERROR_CODE, NetworkError.ERROR_TOKEN_FAILED);
            EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(errorCode));
        } else if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
            NetworkError.init();
        }
    }

    private void requestNetworkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        AppManager.getInstance().setNetworkAvailable(activeInfo != null);
    }
}  