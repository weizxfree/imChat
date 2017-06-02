package com.itutorgroup.tutorchat.phone.utils.manager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.domain.event.NetworkEvent;
import com.itutorgroup.tutorchat.phone.domain.request.LogErrorRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.common.ACache;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.permission.platform.AutoStartUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/26.
 */
public class AppManager {

    private static final String TAG = AppManager.class.getSimpleName();

    public static final String PK_APP_LOCALE = "app_locale";
    public static final String PK_APP_FONT_SCALE = "app_font_scale";

    private static AppManager sInstance;

    private boolean mNetworkAvaliable = false;

    public static AppManager getInstance() {
        if (sInstance == null) {
            synchronized (AppManager.class) {
                if (sInstance == null) {
                    sInstance = new AppManager();
                }
            }
        }
        return sInstance;
    }

    public void uploadCrashLog(String log) {
        uploadLog("Crash", log);
    }

    public void uploadLog(final String tag, String log) {
        LogErrorRequest request = new LogErrorRequest();
        request.Msg = log;
        request.Tag = tag;

        new RequestHandler()
                .request(request)
                .operation(Operation.LogError)
                .exec(CommonResponse.class, new RequestHandler.RequestListener() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        LogUtil.d("log upload done. tag = " + tag);
                    }
                });
    }

    /**
     * 获得app的打包时间
     *
     * @return
     */
    public String getAppBuildTime() {
        String result = "";
        try {
            ApplicationInfo ai = LPApp.getInstance().getPackageManager().getApplicationInfo(LPApp.getInstance().getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
            long time = ze.getTime();
            SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getInstance();
            formatter.applyPattern("yyyy/MM/dd HH:mm:ss");
            result = formatter.format(new java.util.Date(time));
            zf.close();
        } catch (Exception e) {
        }

        return result;
    }

    public static void initAutoStartPermission(final Context context) {
        boolean isPermissionSet = AppPrefs.get(context).getBoolean("tutor_chat_auto_start_permission", false);
        if (!isPermissionSet) {
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setTitle(R.string.tip_title_set_auto_start_permission)
                    .setMessage(R.string.tip_message_set_auto_start_permission)
                    .setPositiveButton(R.string.dialog_message_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AutoStartUtil.startAutoStartSettingsActivity(LPApp.getInstance());
                            AppPrefs.get(LPApp.getInstance()).putBoolean("tutor_chat_auto_start_permission", true);
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public void saveNewVersionClick(Context context) {
        AppPrefs.get(context).putBoolean("app_new_version_click", true);
    }

    public boolean getNewVersionHasClick(Context context) {
        return AppPrefs.get(context).getBoolean("app_new_version_click", false);
    }

    public void setNetworkAvailable(boolean hasNetwork) {
        mNetworkAvaliable = hasNetwork;
        EventBusManager.getInstance().post(new NetworkEvent(hasNetwork));
    }

    public boolean hasNetwork() {
        return mNetworkAvaliable;
    }

    public boolean modifyAppEnv(String env) {
        ACache cache = ACache.get(LPApp.getInstance(), ACache.APP_ENV);
        if (cache != null) {
            cache.put(ACache.APP_ENV, env);
            return true;
        } else {
            return false;
        }
    }

    public String loadCustomEnv() {
        ACache cache = ACache.get(LPApp.getInstance(), ACache.APP_ENV);
        if (cache != null) {
            return cache.getAsString(ACache.APP_ENV);
        }
        return null;
    }

    public void showServerEnv(final Activity activity) {
        Observable.just(activity)
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Activity, List<String>>() {
                    @Override
                    public List<String> call(Activity activity) {
                        String http = CommonUtil.getIPofUrl(APIConstant.URL_HOST);
                        String file = CommonUtil.getIPofUrl(APIConstant.URL_FILE);
                        String tcp = CommonUtil.getIPofUrl(APIConstant.TCP_SERVER_HOST);
                        List<String> list = new ArrayList<>();
                        list.add("API Server: " + http);
                        list.add("File Server: " + file);
                        list.add("TCP : " + tcp + ":" + APIConstant.TCP_SERVER_PORT);
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> list) {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                        builder.setTitle(activity.getString(R.string.tip_current_app_env, APIConstant.API_ENV))
                                .setItems(list.toArray(new String[]{}), null)
                                .show();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public int getIndexFromArray(String str, int resId, int def) {
        if (TextUtils.isEmpty(str)) {
            return def;
        }
        String[] array = LPApp.getInstance().getResources().getStringArray(resId);
        if (array != null && array.length > 0) {
            int len = array.length;
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(str, array[i])) {
                    return i;
                }
            }
        }
        return def;
    }

    public Locale getLocaleWithCode(String code) {
        Locale locale = null;
        switch (code) {
            case "en":
                locale = Locale.ENGLISH;
                break;
            case "zh_CN":
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
        }
        return locale;
    }

    public String getCurrentLocale() {
        String localeCode = AppPrefs.get(LPApp.getInstance()).getString(PK_APP_LOCALE, null);
        String[] array = LPApp.getInstance().getResources().getStringArray(R.array.locale_list_value);
        int index = 0;
        if (!TextUtils.isEmpty(localeCode) && array != null && array.length > 0) {
            int len = array.length;
            for (int i = 0; i < len; i++) {
                if (TextUtils.equals(array[i], localeCode)) {
                    index = i + 1;
                    break;
                }
            }
        }
        return LPApp.getInstance().getResources().getStringArray(R.array.locale_list_entry)[index];
    }

    public String getCurrentFontScale() {
        String value = AppPrefs.get(LPApp.getInstance()).getString(PK_APP_FONT_SCALE, "1");
        int index = getIndexFromArray(value, R.array.app_font_size_value, 1);
        return LPApp.getInstance().getResources().getStringArray(R.array.app_font_size_entry)[index];
    }

    public void switchLanguage(int index) {
        if (index == 0) {
            AppPrefs.get(LPApp.getInstance()).remove(PK_APP_LOCALE);
            restartApp();
        } else {
            String code = LPApp.getInstance().getResources().getStringArray(R.array.locale_list_value)[index - 1];
            Locale locale = getLocaleWithCode(code);
            if (locale != null) {
                switchLanguage(locale);
                AppPrefs.get(LPApp.getInstance()).putString(PK_APP_LOCALE, locale.toString());
                restartApp();
            }
        }
    }

    public void setFontScale(int index) {
        String value = LPApp.getInstance().getResources().getStringArray(R.array.app_font_size_value)[index];
        float scale = 1;
        try {
            scale = Float.valueOf(value);
        } catch (Exception e) {
        }
        switchFontScale(scale);
        AppPrefs.get(LPApp.getInstance()).putString(PK_APP_FONT_SCALE, String.valueOf(scale));
        restartApp();
    }

    private void switchFontScale(final float scale) {
        setAppConfig(new CommonLoadingListener<Configuration>() {
            @Override
            public void onResponse(Configuration configuration) {
                configuration.fontScale = scale;
            }
        });
    }

    private void switchLanguage(final Locale locale) {
        setAppConfig(new CommonLoadingListener<Configuration>() {
            @Override
            public void onResponse(Configuration configuration) {
                configuration.locale = locale;
                NetworkError.init();
            }
        });
    }

    private void setAppConfig(CommonLoadingListener<Configuration> listener) {
        Resources resources = LPApp.getInstance().getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();
        listener.onResponse(config);
        resources.updateConfiguration(config, dm);
    }

    public void restartApp() {
        Observable.just(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<AppManager>() {
                    @Override
                    public void call(AppManager appManager) {
                        try {
                            MyActivityManager.getInstance().finishAll();
                            Intent intent = LPApp.getInstance().getPackageManager().getLaunchIntentForPackage(LPApp.getInstance().getPackageName());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            LPApp.getInstance().startActivity(intent);
                        } catch (Exception e) {
                            LogUtil.exception(e);
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private long mLastLoadConfigTime = 0;

    public void autoLoadAppSettings(boolean newThread) {
        Observable<LPApp> observable = Observable.just(LPApp.getInstance());
        if (newThread) {
            observable = observable.observeOn(Schedulers.io());
        }
        observable.filter(new Func1<LPApp, Boolean>() {
            @Override
            public Boolean call(LPApp lpApp) {
                return System.currentTimeMillis() - mLastLoadConfigTime >= 2000;
            }
        }).subscribe(new Action1<LPApp>() {
            @Override
            public void call(LPApp app) {
                autoLoadLocale();
                autoLoadFontScale();
                FaceConversionUtil.getInstace().getFileText(LPApp.getInstance());
                mLastLoadConfigTime = System.currentTimeMillis();
            }
        }, CommonUtil.ACTION_EXCEPTION);
    }

    private void autoLoadFontScale() {
        String value = AppPrefs.get(LPApp.getInstance()).getString(PK_APP_FONT_SCALE, "1");
        int index = getIndexFromArray(value, R.array.app_font_size_value, 1);
        value = LPApp.getInstance().getResources().getStringArray(R.array.app_font_size_value)[index];
        float scale = 1;
        try {
            scale = Float.valueOf(value);
        } catch (Exception e) {
        }
        switchFontScale(scale);
    }

    private void autoLoadLocale() {
        String localeCode = AppPrefs.get(LPApp.getInstance()).getString(PK_APP_LOCALE, null);
        if (!TextUtils.isEmpty(localeCode)) {
            Locale locale = getLocaleWithCode(localeCode);
            if (locale != null) {
                switchLanguage(locale);
            } else {
                switchLanguage(Locale.getDefault());
            }
        } else {
            switchLanguage(Locale.getDefault());
        }
    }

    public int getCurrentLanguageType() {
        String language = LPApp.getInstance().getResources().getConfiguration().locale.getLanguage();
        int type = 1;
        if (!TextUtils.equals("zh", language)) {
            type = 2;
        }
        return type;
    }

    public void resumeActivity(BaseActivity baseActivity) {
        Observable.just(baseActivity)
                .subscribeOn(Schedulers.io())
                .filter(new Func1<BaseActivity, Boolean>() {
                    @Override
                    public Boolean call(BaseActivity baseActivity) {
                        Configuration configuration = baseActivity.getResources().getConfiguration();
                        String language = configuration.locale.getLanguage();
                        if (!TextUtils.equals("zh", language) && !TextUtils.equals("en", language)) {
                            language = "en";
                        }
                        String localeCode = AppPrefs.get(LPApp.getInstance()).getString(AppManager.PK_APP_LOCALE, null);
                        if (!TextUtils.isEmpty(localeCode)) {
                            Locale locale = AppManager.getInstance().getLocaleWithCode(localeCode);
                            if (locale != null && !TextUtils.equals(language, locale.getLanguage())) {
                                AppManager.getInstance().autoLoadAppSettings(false);
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BaseActivity>() {
                    @Override
                    public void call(BaseActivity baseActivity) {
                        LogUtil.d(TAG, "restore app language, restart.");
                        AppManager.getInstance().restartApp();
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }
}
