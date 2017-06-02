package com.itutorgroup.tutorchat.phone.utils.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.io.FileNotFoundException;

/**
 * Created by joyinzhao on 2016/8/30.
 */
public class UserInfoHelper {

    private static final String CACHE_NAME_IMAGE_NOT_FOUND = "file_not_found:%s";

    public static void showAvatar(UserInfo userInfo, ImageView imageView) {
        if (userInfo != null) {
            showAvatar(userInfo.Image, imageView);
        }
    }

    public static void showAvatar(UserInfo userInfo, ImageView imageView, ImageLoadingListener listener) {
        if (userInfo != null) {
            showAvatar(userInfo.Image, imageView, listener);
        }
    }

    public static void showAvatar(String userAvatar, ImageView imageView) {
        showAvatar(userAvatar, imageView, null);
    }

    public static void showAvatar(String userAvatar, ImageView imageView, final ImageLoadingListener listener) {
        if (imageView != null) {
            String imagePath = APIConstant.URL_IMAGE_AVATAR + userAvatar;
            String key = String.format(CACHE_NAME_IMAGE_NOT_FOUND, imagePath);
            if (!TextUtils.isEmpty(userAvatar) && !AppPrefs.get(LPApp.getInstance()).getBoolean(key, false)) {
                ImageLoader.getInstance().displayImage(imagePath, imageView, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {
                        if (listener != null) {
                            listener.onLoadingStarted(s, view);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        if (failReason != null && failReason.getType() == FailReason.FailType.IO_ERROR && failReason.getCause() != null && failReason.getCause() instanceof FileNotFoundException) {
                            String key = String.format(CACHE_NAME_IMAGE_NOT_FOUND, s);
                            AppPrefs.get(LPApp.getInstance()).putBoolean(key, true);
                        }
                        if (listener != null) {
                            listener.onLoadingFailed(s, view, failReason);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        if (listener != null) {
                            listener.onLoadingComplete(s, view, bitmap);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {
                        if (listener != null) {
                            listener.onLoadingCancelled(s, view);
                        }
                    }
                });
            } else {
                if (imageView.getTag(R.id.tag_default) != null) {
                    int resId = (int) imageView.getTag(R.id.tag_default);
                    imageView.setImageResource(resId);
                } else {
                    imageView.setImageDrawable(new ColorDrawable(0x00000000));
                }
            }
        }
    }

    public static Drawable getSexDrawable(UserInfo user) {
        return null;
        /*
        Drawable drawable = null;
        switch (user.Sex) {
            case "0":
                drawable = LPApp.getInstance().getResources().getDrawable(R.drawable.sex_female);
                break;
            case "1":
                drawable = LPApp.getInstance().getResources().getDrawable(R.drawable.sex_male);
                break;
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
        // 性别标志暂时不需要
        return drawable;
        */
    }
}
