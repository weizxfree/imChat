package com.itutorgroup.tutorchat.phone.app;

import android.content.Context;
import android.os.Handler;
import android.os.Process;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.CrashHandler;
import com.itutorgroup.tutorchat.phone.utils.common.GlobalActionUtil;
import com.itutorgroup.tutorchat.phone.utils.daemon.Receiver1;
import com.itutorgroup.tutorchat.phone.utils.daemon.Receiver2;
import com.itutorgroup.tutorchat.phone.utils.daemon.Service1;
import com.itutorgroup.tutorchat.phone.utils.daemon.Service2;
import com.itutorgroup.tutorchat.phone.utils.kernel.Kernel;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.permission.platform.AutoStartUtil;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheEntity;
import com.lzy.okhttputils.cache.CacheMode;
import com.lzy.okhttputils.cookie.store.PersistentCookieStore;
import com.lzy.okhttputils.model.HttpHeaders;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;





public class LPApp extends MultiDexApplication {

    private static LPApp mInstance = null;
    public Handler mHandler = new Handler();
    private DaemonClient mDaemonClient;
    public static final int CONNECT_TIME_OUT = 5000;
    public static final int READ_TIME_OUT = 5000;
    public static final int WRITE_TIME_OUT = 5000;

    private RefWatcher mRefWatcher;

    public static LPApp getInstance() {
        return mInstance;
    }

    public RefWatcher getRefWatcher() {
        return getInstance().mRefWatcher;
    }

    public void onCreate() {
        super.onCreate();
        initial();
    }

    private void initial() {
        mInstance = this;
        initOkHttp();
        String processName = AppUtils.getProcessName(this, Process.myPid());
        if (!TextUtils.equals(processName, getPackageName())) {
            return;
        }
        initImageLoader();
        GlobalActionUtil.getInstance().init();
        CrashHandler.getInstance().init(this);
        Kernel.getInstance().startTcpService(this);
        AppManager.getInstance().autoLoadAppSettings(true);
        mRefWatcher = LeakCanary.install(this);
    }

    private void initOkHttp() {
        OkHttpUtils.init(this);
        HttpHeaders headers = new HttpHeaders();
        headers.put("Accept", "application/x-TutorGroupIMFormatter");
        OkHttpUtils.getInstance()
                //打开该调试开关,控制台会使用 红色error 级别打印log,并不是错误,是为了显眼,不需要就不要加入该行
//				.debug("OkHttpUtils")
                //如果使用默认的 60秒,以下三行也不需要传
                .setConnectTimeout(CONNECT_TIME_OUT)               //全局的连接超时时间
                .setReadTimeOut(READ_TIME_OUT)                  //全局的读取超时时间
                .setWriteTimeOut(WRITE_TIME_OUT)                 //全局的写入超时时间
                //可以全局统一设置缓存模式,默认就是Default,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy0216/
                .setCacheMode(CacheMode.NO_CACHE)
                //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setCookieStore(new PersistentCookieStore())                       //cookie持久化存储，如果cookie不过期，则一直有效
                //这两行同上,不需要就不要传
                .addCommonHeaders(headers);                                 //设置全局公共头
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), Constant.IMAGE_LODER_CACHE_DIR);
        DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.head_personal_blue)//设置图片uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.head_personal_square)
                .cacheInMemory().cacheOnDisc().build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(imageOptions)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .memoryCacheSize(2 * 1024 * 1024)
                .build();
        com.nostra13.universalimageloader.utils.L.disableLogging();
        ImageLoader.getInstance().init(config);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mDaemonClient = new DaemonClient(createDaemonConfigurations());
        mDaemonClient.onAttachBaseContext(base);
    }

    private DaemonConfigurations createDaemonConfigurations() {
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                "com.itutorgroup.tutorchat.phone:process1",
                Service1.class.getCanonicalName(),
                Receiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                "com.itutorgroup.tutorchat.phone:process2",
                Service2.class.getCanonicalName(),
                Receiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }


    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
            AutoStartUtil.init(LPApp.getInstance());
        }
    }

}
