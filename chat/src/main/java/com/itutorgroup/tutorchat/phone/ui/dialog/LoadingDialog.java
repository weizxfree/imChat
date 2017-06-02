package com.itutorgroup.tutorchat.phone.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2016/9/26.
 */
public class LoadingDialog extends BaseDialog {
    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected View getDefaultView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        ImageView icon = (ImageView) view.findViewById(R.id.icon_loading);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading_rotate);
        icon.startAnimation(animation);
        return view;
    }
}
