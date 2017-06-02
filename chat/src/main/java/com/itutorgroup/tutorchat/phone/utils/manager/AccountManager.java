package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.igexin.sdk.PushManager;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.UpdateUserPhotoRequest;
import com.itutorgroup.tutorchat.phone.domain.response.UpdateUserPhotoResponse;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import cn.salesuite.saf.utils.StringUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/8/29.
 */
public class AccountManager {

    public static final String ACTION_USER_LOGOUT = BuildConfig.APPLICATION_ID + ".user_logout";

    public static final String PK_TOKEN = "token";
    public static final String PK_USER_INFO = "user_info";
    public static final String PK_AES_KEY = "aes_key";
    public static final String PK_AES_VALUE = "aes_value";

    public static final String PK_CLIENT_ID = "client_id";
    private static AccountManager sAccountManager;

    private UserInfo mCurrentUser;

    private String mToken;

    private List<String> mRightList;

    public static AccountManager getInstance() {
        if (sAccountManager == null) {
            synchronized (AccountManager.class) {
                if (sAccountManager == null) {
                    sAccountManager = new AccountManager();
                }
            }
        }
        return sAccountManager;
    }

    public void loginSuccess(String token, UserInfo user) {
        setCurrentUser(user);
        setToken(token);
    }

    public void loginSuccess(String token, UserInfo user, int aesKey, String aesValue) {
        loginSuccess(token, user);
        AppPrefs prefs = AppPrefs.get(LPApp.getInstance());
        prefs.putInt(PK_AES_KEY, aesKey);
        prefs.putString(PK_AES_VALUE, aesValue);
//        ACache.get(LPApp.getInstance()).put(PK_AES_KEY, "" + aesKey);
//        ACache.get(LPApp.getInstance()).put(PK_AES_VALUE, aesValue);
    }

    public boolean loadLoginData() {
        AppPrefs prefs = AppPrefs.get(LPApp.getInstance());
        String token = prefs.getString(PK_TOKEN, null);
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        Object userObject = prefs.getObject(PK_USER_INFO);
        UserInfo userInfo = (UserInfo) userObject;
        if (userInfo == null) {
            return false;
        }

        mToken = token;
        mCurrentUser = userInfo;
        return true;
    }

    public void logout() {
        Observable.just(this)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<AccountManager>() {
                    @Override
                    public void call(AccountManager accountManager) {
                        clearAccountData();
                        removeNotification();
                        Kernel.getInstance().stopTcpService();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private void removeNotification() {
        NotificationManager nm = (NotificationManager) LPApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    public void clearAccountData() {
        clearPrefs();
        mToken = null;
        mCurrentUser = null;
        ACache.get(LPApp.getInstance()).clear();
        ServiceAccountManager.getInstance().logout();
    }

    private void clearPrefs() {
        AppPrefs prefs = AppPrefs.get(LPApp.getInstance());
        prefs.remove(PK_USER_INFO);
        prefs.remove(PK_TOKEN);
        prefs.remove(PK_AES_KEY);
        prefs.remove(PK_AES_VALUE);
    }

    public void setCurrentUser(UserInfo user) {
        mCurrentUser = user;
        AppPrefs.get(LPApp.getInstance()).putObject(PK_USER_INFO, user);
    }

    public UserInfo getCurrentUser() {
//        if (mCurrentUser == null || TextUtils.isEmpty(mCurrentUser.UserID)) {
//            loadLoginData();
//        }
        return mCurrentUser;
    }

    public String getCurrentUserId() {
//        if (mCurrentUser == null || TextUtils.isEmpty(mCurrentUser.UserID)) {
//            loadLoginData();
//        }
        if (mCurrentUser != null) {
            return mCurrentUser.UserID;
        } else {
            return null;
        }
    }

    public void setToken(String token) {
        mToken = token;
        AppPrefs.get(LPApp.getInstance()).putString(PK_TOKEN, token);
    }

    public String getToken() {
        return mToken;
    }

    public void setClientId(String clientId) {
        AppPrefs.get(LPApp.getInstance()).putString(PK_CLIENT_ID, clientId);
    }

    public int getAESKey() {
        return AppPrefs.get(LPApp.getInstance()).getInt(PK_AES_KEY, 0);
//        String key = ACache.get(LPApp.getInstance()).getAsString(PK_AES_KEY);
//        if (!TextUtils.isEmpty(key)) {
//            try {
//                int n = Integer.valueOf(key);
//                return n;
//            } catch (NumberFormatException e) {
//            }
//        }
//        return 0;
    }

    public String getAESValue() {
        return AppPrefs.get(LPApp.getInstance()).getString(PK_AES_VALUE, Constant.AES_LOGIN_KEY);
//        String value = ACache.get(LPApp.getInstance()).getAsString(PK_AES_VALUE);
//        if (TextUtils.isEmpty(value)) {
//            value = Constant.AES_LOGIN_KEY;
//        }
//        return value;
    }

    public String getClientId() {
        String clientId = AppPrefs.get(LPApp.getInstance()).getString(PK_CLIENT_ID, null);
        if (StringUtils.isEmpty(clientId)) {
            clientId = PushManager.getInstance().getClientid(LPApp.getInstance());
            if (StringUtils.isNotEmpty(clientId)) {
                setClientId(clientId);
            }
        }
        return clientId;
    }


    public void updateAvatar(Context context, byte[] data, final RequestHandler.RequestListener<UpdateUserPhotoResponse> listener) {
        final UpdateUserPhotoRequest request = new UpdateUserPhotoRequest();
        request.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        request.UserID = AccountManager.getInstance().getCurrentUserId();
        request.Token = AccountManager.getInstance().getToken();
        request.PhotoData = data;

        new RequestHandler<UpdateUserPhotoResponse>()
                .operation(Operation.UPDATE_USER_PHOTO)
                .dialog(context)
                .request(request)
                .exec(UpdateUserPhotoResponse.class, new RequestHandler.RequestListener<UpdateUserPhotoResponse>() {
                    @Override
                    public void onResponse(UpdateUserPhotoResponse response, Bundle bundle) {
                        mCurrentUser.Image = response.PhotoUrl;
                        AppPrefs.get(LPApp.getInstance()).putObject(PK_USER_INFO, mCurrentUser);
                        EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(mCurrentUser));
                        if (listener != null) {
                            listener.onResponse(response, bundle);
                        }
                    }
                });
    }

    public void setRightList(List<String> list) {
        mRightList = list;
        EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(mCurrentUser));
    }

    public List<String> getRightList() {
        return mRightList;
    }

    public void setLastAccount(String account) {
        AppPrefs.get(LPApp.getInstance()).putString("last_account", account);
    }

    public String getLastAccount() {
        return AppPrefs.get(LPApp.getInstance()).getString("last_account", "");
    }

    public boolean isSoundEnabled() {
        return AppPrefs.get(LPApp.getInstance()).getBoolean("settings:sound:" + getCurrentUserId(), true);
    }

    public boolean isVibrateEnabled() {
        return AppPrefs.get(LPApp.getInstance()).getBoolean("settings:vibrate:" + getCurrentUserId(), true);
    }

    public void setSoundEnabled(boolean enable) {
        AppPrefs.get(LPApp.getInstance()).putBoolean("settings:sound:" + getCurrentUserId(), enable);
    }

    public void setVibrateEnabled(boolean enable) {
        AppPrefs.get(LPApp.getInstance()).putBoolean("settings:vibrate:" + getCurrentUserId(), enable);
    }

}
