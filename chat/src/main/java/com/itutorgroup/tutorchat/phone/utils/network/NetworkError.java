package com.itutorgroup.tutorchat.phone.utils.network;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.receiver.MainGlobalReceiver;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.parser.ErrorCodeParser;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class NetworkError {

    public static SparseArray<String> sErrorArray;

    public static final String KEY_IGNORE_ALL_ERROR = "ignoreAllError";
    public static final String KEY_IGNORE_ERROR_CODE = "ignoreErrorCode";
    public static final String KEY_IGNORE_RESPONSE_LOG = "ignoreResponseLog";

    public static final String KEY_REQUEST_OPERATION = "request_operation";

    public static final String EXTRA_ERROR_CODE = "error_code";

    public static final int ERROR_INEXISTED_USER = 100009; // 用户不存在
    public static final int ERROR_INVALID_USER_ID = 999001; // UserID无效
    public static final int ERROR_TOKEN_FAILED = 999004; // Token 验证失败
    public static final int ERROR_AES_DECRYPT_FAILED = 999007;
    public static final int ERROR_INVALID_UNREAD_MESSAGE_ID = 600006; // 未读消息Id无效
    public static final int LOGIN_CAUSE_ENV_CHANGED = -1000001; // 未读消息Id无效

    static {
        init();
    }

    public static void init() {
        Observable.just("")
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            sErrorArray = ErrorCodeParser.getErrorCodeMessageMap();
                        } catch (Exception e) {
                            LogUtil.exception(e);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public static void dispatchError(int errorCode, String resultMsg, Bundle bundle) {
        if (sErrorArray != null && !interceptAllError(bundle) && !interceptErrorCode(bundle, errorCode)) {
            if (!dispatchGlobalError(errorCode, resultMsg, bundle)) {
                String errorMessage = sErrorArray.get(errorCode);
                if (!TextUtils.isEmpty(errorMessage)) {
                    ToastUtil.show(errorMessage);
                }
            }
        }
    }

    private static boolean dispatchGlobalError(int errorCode, String resultMsg, Bundle bundle) {
        switch (errorCode) {
            case ERROR_TOKEN_FAILED: // Token 验证失败
                LPApp.getInstance().sendBroadcast(new Intent(MainGlobalReceiver.ACTION_KICK_OUT));
                return true;
            case ERROR_AES_DECRYPT_FAILED:
                Intent intent = new Intent(MainGlobalReceiver.ACTION_KICK_OUT);
                intent.putExtra(EXTRA_ERROR_CODE, ERROR_AES_DECRYPT_FAILED);
                LPApp.getInstance().sendBroadcast(intent);
                return true;
            case ERROR_INEXISTED_USER:
                return true;
        }
        return false;
    }

    private static boolean interceptErrorCode(Bundle bundle, int errorCode) {
        if (bundle != null) {
            ArrayList<Integer> ignoreList = bundle.getIntegerArrayList(KEY_IGNORE_ERROR_CODE);
            if (ignoreList != null && !ignoreList.isEmpty()) {
                if (ignoreList.contains(errorCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean interceptAllError(Bundle bundle) {
        if (bundle != null) {
            return bundle.getBoolean(KEY_IGNORE_ALL_ERROR);
        }
        return false;
    }
}
