package com.itutorgroup.tutorchat.phone.ui.photo.picker.utils;

import android.content.Context;
import android.os.Environment;

/**
 * Created by zhy93 on 2015/9/11.
 */
public class FileUtil {
    /**
     * 判断是否有sdk
     * 若设备无外部存储sdcard,则更换存储路径
     */
    public static boolean checkSdCard() {
        //getExternalStorageDirectory()获取sdcard根目录
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static String getFileDir(Context context) {
        //存在sdcard  null参数，返回外部存储的根目录
        if (checkSdCard()) {
            //getExternalFilesDir获取应用程序下的存储目录，卸载应用时该文件夹的数据会被删除
            return context.getExternalFilesDir(null)
                    .getAbsolutePath();
        } else {
            return context.getFilesDir().getAbsolutePath();
        }
    }
}
