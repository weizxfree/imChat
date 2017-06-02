package com.itutorgroup.tutorchat.phone.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.widget.RemoteViews;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.SplashActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.lzy.okhttputils.OkHttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import cn.salesuite.saf.utils.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tom_zxzhang on 2016/10/17.
 */
public class UpdateService extends Service {
    // BT字节参考量
    private static final float SIZE_BT = 1024L;
    // KB字节参考量
    private static final float SIZE_KB = SIZE_BT * 1024.0f;
    // MB字节参考量
    private static final float SIZE_MB = SIZE_KB * 1024.0f;

    private final static int DOWNLOAD_COMPLETE = 1;// 完成
    private final static int DOWNLOAD_NOMEMORY = -1;// 内存异常
    private final static int DOWNLOAD_FAIL = -2;// 失败

    private String appName = null;// 应用名字
    private String appUrl = null;// 应用升级地址
    private File updateDir = null;// 文件目录
    private File updateFile = null;// 升级文件

    // 通知栏
    private NotificationManager updateNotificationManager = null;
    private Notification updateNotification = null;

    private Intent updateIntent = null;// 下载完成
    private PendingIntent updatePendingIntent = null;// 在下载的时候
    OkHttpClient mOkHttpClient ;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        appName = getResources().getString(R.string.app_name);
//        appUrl = intent.getStringExtra("appurl");
        mOkHttpClient = new OkHttpClient();
        updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        updateNotification = new Notification();
        //通知图标
        updateNotification.icon = R.drawable.push;
        //通知信息描述
        updateNotification.tickerText = LPApp.getInstance().getString(R.string.app_download_notification_name,appName);
        updateNotification.when = System.currentTimeMillis();
        updateIntent = new Intent(this, SplashActivity.class);
        updatePendingIntent = PendingIntent.getActivity(this, 0, updateIntent,
                0);
        updateNotification.contentIntent = updatePendingIntent;
        updateNotification.contentIntent.cancel();
        updateNotification.contentView = new RemoteViews(getPackageName(),
                R.layout.notification_download);
        updateNotification.contentView.setTextViewText(
                R.id.download_notice_name_tv,  LPApp.getInstance().getString(R.string.app_download_notification_name,appName));
        updateNotification.contentView.setTextViewText(
                R.id.download_notice_speed_tv, "0MB (0%)");
        updateNotificationManager.notify(0, updateNotification);
        downloadApk();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }



    private void updateNotification(int status){
        switch (status) {
            case DOWNLOAD_COMPLETE:
                String cmd = "chmod 777 " + updateDir.getPath();
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(updateFile);
                //安装程序
                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setDataAndType(uri,
                        "application/vnd.android.package-archive");
                updatePendingIntent = PendingIntent.getActivity(
                        UpdateService.this, 0, installIntent, 0);
                updateNotification.contentIntent = updatePendingIntent;
                updateNotification.contentView.setTextViewText(
                        R.id.download_notice_speed_tv,
                        getString(R.string.app_update_success));
                updateNotification.tickerText = appName + getString(R.string.app_download_success);

                updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
                updateNotification.defaults = Notification.DEFAULT_SOUND;
                updateNotificationManager.notify(0, updateNotification);
                //启动安装程序
                UpdateService.this.startActivity(installIntent);
                break;
            case DOWNLOAD_NOMEMORY:
                updateNotification.tickerText = appName + getString(R.string.app_download_fail);
                updateNotification.contentView.setTextViewText(
                        R.id.download_notice_speed_tv,
                        getString(R.string.app_download_memory_is_not_avalible));
                updateNotificationManager.notify(0, updateNotification);
                break;

            case DOWNLOAD_FAIL:
                updateNotification.tickerText = appName + getString(R.string.app_download_fail);
                updateNotification.contentView.setTextViewText(
                        R.id.download_notice_speed_tv,
                        getString(R.string.app_update_fail));
                break;
        }
        updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        updateNotification.defaults = Notification.DEFAULT_SOUND;
        updateNotificationManager.notify(0, updateNotification);
        try {
            Thread.sleep(3000);
            updateNotificationManager.cancel(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();


    }


    private void downloadApk(){


        if(StringUtils.isNotBlank(updateDir)){
            File file = new File(updateDir.getPath(), appName + ".apk");
            if(file.exists()){
                file.delete();
            }
        }

        Request request = new Request.Builder().url(Constant.updateVersion.Url).build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                int count = 0;
                long totalSize = 0;   //总大小
                long downloadSize = 0;   //下载的大小
                try {
                    totalSize = response.body().contentLength();
                    /*内存够用的时候*/
                    if(MemoryAvailable(totalSize)){
                        inputStream = response.body().byteStream();
                        fileOutputStream = new FileOutputStream(new File(updateDir.getPath(), appName + ".apk"));
                        byte[] buffer = new byte[2048];
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                            downloadSize += len;
                            if ((count == 0) || (int) (downloadSize * 100 / totalSize) >= count) {
                                count += 5;
                                updateNotification.contentView
                                        .setTextViewText(
                                                R.id.download_notice_speed_tv,getMsgSpeed(downloadSize,totalSize)
                                        );
                                updateNotificationManager.notify(0, updateNotification);
                            }
                        }
                        fileOutputStream.flush();
                        if (totalSize >= downloadSize) {
                            updateNotification(DOWNLOAD_COMPLETE);
                        } else {
                            updateNotification(DOWNLOAD_FAIL);
                        }
                    }else{
                        updateNotification(DOWNLOAD_NOMEMORY);
                    }

                } catch (IOException e) {
                    updateNotification(DOWNLOAD_FAIL);
                    e.printStackTrace();
                } finally {

                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }

                }
            }

        });


    }





    /**
     * 可用内存大小
     * @param fileSize
     * @return
     */
    private boolean MemoryAvailable(long fileSize) {
        fileSize += (1024 << 10);
        if (MemoryStatus.externalMemoryAvailable()) {
            if ((MemoryStatus.getAvailableExternalMemorySize() <= fileSize)) {
                if ((MemoryStatus.getAvailableInternalMemorySize() > fileSize)) {
                    createFile(false);
                    return true;
                } else {
                    return false;
                }
            } else {
                createFile(true);
                return true;
            }
        } else {
            if (MemoryStatus.getAvailableInternalMemorySize() <= fileSize) {
                return false;
            } else {
                createFile(false);
                return true;
            }
        }
    }

    /**
     * 获取下载进度
     * @param downSize
     * @param allSize
     * @return
     */
    public static String getMsgSpeed(long downSize, long allSize) {
        StringBuffer sBuf = new StringBuffer();
        sBuf.append(getSize(downSize));
        sBuf.append("/");
        sBuf.append(getSize(allSize));
        sBuf.append(" ");
        sBuf.append(getPercentSize(downSize, allSize));
        return sBuf.toString();
    }

    /**
     * 获取大小
     * @param size
     * @return
     */
    public static String getSize(long size) {
        if (size >= 0 && size < SIZE_BT) {
            return (double) (Math.round(size * 10) / 10.0) + "B";
        } else if (size >= SIZE_BT && size < SIZE_KB) {
            return (double) (Math.round((size / SIZE_BT) * 10) / 10.0) + "KB";
        } else if (size >= SIZE_KB && size < SIZE_MB) {
            return (double) (Math.round((size / SIZE_KB) * 10) / 10.0) + "MB";
        }
        return "";
    }

    /**
     * 获取到当前的下载百分比
     * @param downSize   下载大小
     * @param allSize    总共大小
     * @return
     */
    public static String getPercentSize(long downSize, long allSize) {
        String percent = (allSize == 0 ? "0.0" : new DecimalFormat("0.0")
                .format((double) downSize / (double) allSize * 100));
        return "(" + percent + "%)";
    }


    /**
     * 创建file文件
     * @param sd_available    sdcard是否可用
     */
    private void createFile(boolean sd_available) {
        if (sd_available) {
            updateDir = new File(Environment.getExternalStorageDirectory(),
                    "download");
        } else {
            updateDir = getFilesDir();
        }
        updateFile = new File(updateDir.getPath(), appName + ".apk");
        if (!updateDir.exists()) {
            updateDir.mkdirs();
        }
        if (!updateFile.exists()) {
            try {
                updateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updateFile.delete();
            try {
                updateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static class MemoryStatus {

        static final int ERROR = -1;

        /**
         * 是否有外部可用内存
         *
         * @return
         */
        public static boolean externalMemoryAvailable() {
            return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        }

        /**
         * 得到可用的内存大小
         *
         * @return
         */
        public static long getAvailableInternalMemorySize() {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }

        /**
         * 得到总共的内存大小
         *
         * @return
         */
        public static long getTotalInternalMemorySize() {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        }

        /**
         * 得到外部可用内存大小
         *
         * @return
         */
        public static long getAvailableExternalMemorySize() {
            if (externalMemoryAvailable()) {
                File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long availableBlocks = stat.getAvailableBlocks();
                return availableBlocks * blockSize;
            } else {
                return ERROR;
            }
        }

        /**
         * 得到外部总共内存大小
         *
         * @return
         */
        public static long getTotalExternalMemorySize() {
            if (externalMemoryAvailable()) {
                File path = Environment.getExternalStorageDirectory();
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                return totalBlocks * blockSize;
            } else {
                return ERROR;
            }
        }

        /**
         * 装换内存大小格式
         *
         * @param size
         * @return
         */
        public static String formatSize(long size) {
            String suffix = null;

            if (size >= 1024) {
                suffix = "KiB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = "MiB";
                    size /= 1024;
                }
            }

            StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

            int commaOffset = resultBuffer.length() - 3;
            while (commaOffset > 0) {
                resultBuffer.insert(commaOffset, ',');
                commaOffset -= 3;
            }

            if (suffix != null)
                resultBuffer.append(suffix);
            return resultBuffer.toString();
        }
    }


}
