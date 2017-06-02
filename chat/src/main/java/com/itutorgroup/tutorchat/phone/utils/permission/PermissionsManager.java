package com.itutorgroup.tutorchat.phone.utils.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;

/**
 * Created by tom_zxzhang on 2016/10/25.
 */
public class PermissionsManager {


    private static PermissionsManager sInstance;
    public static final int REQUEST_CODE = 0x501; // 请求码
    private PermissionsChecker permissionsChecker;
    private static final String PACKAGE_URL_SCHEME = "package:";

    public static final String[] PERMISSION_GROUP_AUDIO = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    public static final String[] PERMISSION_GROUP_EXTERNAL_STORAGE = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSION_CAMERA = new String[]{
            Manifest.permission.CAMERA,
    };

    public static PermissionsManager getInstance() {
        if (sInstance == null) {
            synchronized (PermissionsManager.class) {
                if (sInstance == null) {
                    sInstance = new PermissionsManager();
                }
            }
        }
        return sInstance;
    }

    private PermissionsManager() {
        permissionsChecker = new PermissionsChecker(LPApp.getInstance());
    }

    public boolean checkPermissions(Context mContext, String[] permissions) {
        boolean lacksPermissions = permissionsChecker.lacksPermissions(permissions);
        if (lacksPermissions) {
            PermissionsActivity.startActivityForResult((Activity) mContext, REQUEST_CODE, permissions);
        }
        return !lacksPermissions;
    }

    public boolean checkPermissions(String[] permissions) {
        return  permissionsChecker.lacksPermissions(permissions);
    }


    // 显示缺失权限提示
    public void showMissingPermissionDialog(final Context mContext,String PermissionName) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.help);
        builder.setMessage(mContext.getString(R.string.string_help_text, PermissionName));

        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                startAppSettings(mContext);
            }
        });

        builder.setCancelable(false);

        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings(Context mContext) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + mContext.getPackageName()));
        mContext.startActivity(intent);
    }


}
