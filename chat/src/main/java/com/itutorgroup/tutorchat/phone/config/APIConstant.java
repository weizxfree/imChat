/**
 *
 */
package com.itutorgroup.tutorchat.phone.config;

import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

/**
 * @author Tony Shen
 */
public class APIConstant {

    public static String API_ENV;

    public static final String ENV_DEV = "Dev";
    public static final String ENV_STAGE = "Stage";
    public static final String ENV_PRE_PRODUCT = "PreProduct";
    public static final String ENV_PRODUCT = "Product";

    // 头像 -----start-----
    public static final String URL_IMAGE_AVATAR_SANDY = "http://192.168.236.159/tutorchatimg/";
    public static final String URL_IMAGE_AVATAR_RELEASE = "http://source.vipabc.com/ext/tutorchat/headimg/";
    public static final String URL_IMAGE_AVATAR_STAGE = "http://172.16.233.39/tutorchatstageHeadImg/";
    // 头像 -----end-----

    // web api -----start-----
    private static final String URL_HOST_DEBUG = "http://tutorchatapi.vipabc.com/TutorChatDevServer/api/home";
    public static final String URL_HOST_RELEASE = "http://tutorchatapi.vipabc.com/TutorChatServer/api/home";
    public static final String URL_HOST_PRE_PRODUCT = "http://tutorchatapi.vipabc.com/TutorChatDevServer/api/chat";
    public static final String URL_HOST_PRODUCT = "http://tutorchatapi.vipabc.com/TutorChatServer/api/chat";
    public static final String URL_HOST_SAYID = "http://192.168.236.217/iTutorGroup.TutorChat.Server/api/chat";
    public static final String URL_HOST_SAYID_THIRD = "http://192.168.236.217:9001/api/Chat";
    public static final String URL_HOST_SANDY_SH = "http://192.168.236.159:8925/Api/Chat";
    public static final String URL_HOST_SANDY_TW = "http://192.168.236.159:8075/Api/Chat";
    public static final String URL_HOST_STAGE = "http://172.16.233.39/TutorChatShStageServer/api/chat";
    // web api -----end-----

    // file -----start-----
    public static final String URL_FILE_SAYID_DEBUG = "http://192.168.236.217/iTutorGroup.TutorChat.FileServer/api/chat";
    public static final String URL_FILE_SAYID = "http://192.168.236.217/FileServer_tp/api/chat";
    public static final String URL_FILE_STAGE = "http://172.16.233.39/TutorChatStageFileServer/api/chat";
    public static final String URL_FILE_PRE_PRODUCT = "http://tcfile.vipabc.com/TutorChatFileServer/api/chat";
    // file -----end-----

    // tcp -----start-----
    public static final String TCP_SERVER_HOST_RICK = "192.168.236.152";
    public static final String TCP_SERVER_HOST_STAGE = "172.16.233.40";
    public static final String TCP_SERVER_HOST_PRE_PRODUCT = "Longtc.vipabc.com";
    public static final int TCP_SERVER_PORT_SH = 7000;
    public static final int TCP_SERVER_PORT_TW = 7001;
    public static final int TCP_SERVER_PORT_RICK = 7005;
    public static final int TCP_SERVER_PORT_STAGE = 7000;
    public static final int TCP_SERVER_PORT_PRE_PRODUCT = 8080;
    // tcp -----end-----

    // all url -----start-----
    public static String URL_HOST = URL_HOST_SANDY_TW;
    public static String URL_IMAGE_AVATAR = URL_IMAGE_AVATAR_SANDY;
    public static String URL_FILE = URL_FILE_SAYID;

    public static int TCP_SERVER_PORT = TCP_SERVER_PORT_SH;
    public static String TCP_SERVER_HOST = TCP_SERVER_HOST_RICK;
    // all url -----end-----

    public static final String ACRA_URL = "http://192.168.236.154:5984/acra-myapp/_design/acra-storage/_update/report";

    static {
        chooseEnv();
    }

    public static void chooseEnv() {
        String env = BuildConfig.BUILD_TYPE;
        String customEnv = AppManager.getInstance().loadCustomEnv();
        if (!TextUtils.isEmpty(customEnv)) {
            env = customEnv;
            ToastUtil.show(LPApp.getInstance().getString(R.string.tip_current_app_env, customEnv));
        }
        env = env.toLowerCase();
        if (env.equals("stage")) {
            switchStage();
        } else if (env.equals("product")) {
            switchProduct();
        } else if (env.equals("preproduct")) {
            switchPreProduct();
        } else {
            switchDev();
        }
    }

    private static void switchDev() {
        API_ENV = ENV_DEV;
        URL_HOST = URL_HOST_SANDY_TW;
        URL_IMAGE_AVATAR = URL_IMAGE_AVATAR_SANDY;
        URL_FILE = URL_FILE_SAYID;
        TCP_SERVER_PORT = TCP_SERVER_PORT_RICK;
        TCP_SERVER_HOST = TCP_SERVER_HOST_RICK;
    }

    private static void switchPreProduct() {
        API_ENV = ENV_PRE_PRODUCT;
        URL_HOST = URL_HOST_PRE_PRODUCT;
        URL_IMAGE_AVATAR = URL_IMAGE_AVATAR_RELEASE;
        URL_FILE = URL_FILE_PRE_PRODUCT;
        TCP_SERVER_HOST = TCP_SERVER_HOST_PRE_PRODUCT;
        TCP_SERVER_PORT = TCP_SERVER_PORT_PRE_PRODUCT;
    }

    private static void switchProduct() {
        API_ENV = ENV_PRODUCT;
        URL_HOST = URL_HOST_PRODUCT;
        URL_IMAGE_AVATAR = URL_IMAGE_AVATAR_RELEASE;
        URL_FILE = URL_FILE_PRE_PRODUCT;
        TCP_SERVER_HOST = TCP_SERVER_HOST_PRE_PRODUCT;
        TCP_SERVER_PORT = TCP_SERVER_PORT_PRE_PRODUCT;
    }

    private static void switchStage() {
        API_ENV = ENV_STAGE;
        URL_HOST = URL_HOST_STAGE;
        URL_IMAGE_AVATAR = URL_IMAGE_AVATAR_STAGE;
        URL_FILE = URL_FILE_STAGE;
        TCP_SERVER_HOST = TCP_SERVER_HOST_STAGE;
        TCP_SERVER_PORT = TCP_SERVER_PORT_STAGE;
    }

    public static final int TCP_TIME_OUT_IDLE_READ = 65; // 读超时
    public static final int TCP_TIME_OUT_IDLE_WRITE = 60;// 写超时
    public static final int TCP_TIME_OUT_IDLE_ALL = 70; // 所有超时
}