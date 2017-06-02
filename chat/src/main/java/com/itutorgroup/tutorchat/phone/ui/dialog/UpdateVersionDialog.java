package com.itutorgroup.tutorchat.phone.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.service.UpdateService;

/**
 * Created by tom_zxzhang on 2016/12/6.
 */
public class UpdateVersionDialog {

    public static void showUpdateVersionDialog(final Activity mContext,int IsForce) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.str_client_version_update_title);
        if (IsForce != Constant.APP_FORCE_UPDATE) {
            builder.setMessage(mContext.getString(R.string.str_client_version_update_not_force));
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mContext.startService(new Intent(mContext, UpdateService.class));
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
        } else {
            builder.setMessage(mContext.getString(R.string.str_client_version_update_is_force));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mContext.startService(new Intent(mContext, UpdateService.class));
                    mContext.finish();
                }
            });
        }
        AlertDialog dialog  =  builder.create();
        if(!mContext.isFinishing()){
            dialog.show();
        }
    }

}
