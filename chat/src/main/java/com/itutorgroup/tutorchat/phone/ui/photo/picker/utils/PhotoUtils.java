package com.itutorgroup.tutorchat.phone.ui.photo.picker.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.config.Constant;

import java.io.File;
import java.util.Date;

public class PhotoUtils {

    /**
     * 判断外部存储卡是否可用
     *
     * @return 是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public static int getHeightInPx(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getWidthInPx(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeightInDp(Context context) {
        final float height = context.getResources().getDisplayMetrics().heightPixels;
        return px2dip(context, height);
    }

    public static int getWidthInDp(Context context) {
        final float width = context.getResources().getDisplayMetrics().widthPixels;
        return px2dip(context, width);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 资源格式化字符串
     *
     * @param context  上下文
     * @param resource 资源
     * @param args     参数
     * @return 字符串
     */
    public static String formatResourceString(Context context, int resource,
                                              Object... args) {
        String str = context.getResources().getString(resource);
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return String.format(str, args);
    }

    /**
     * 获取拍照相片存储文件
     *
     * @param context 上下文
     * @return 文件对象
     */
    public static File createFile(Context context) {
        File file;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            String timeStamp = String.valueOf(new Date().getTime());
            file = new File(Environment.getExternalStorageDirectory()
                    +Constant.IMAGE_DIR + File.separator + timeStamp + ".jpg");
        } else {
            File cacheDir = context.getCacheDir();
            String timeStamp = String.valueOf(new Date().getTime());
            file = new File(cacheDir+Constant.IMAGE_DIR, timeStamp + ".jpg");
        }
        return file;
    }

}
