package com.itutorgroup.tutorchat.phone.ui.common.groupimageview;

import android.content.Context;
import android.widget.ImageView;

public abstract class GroupAvatarAdapter<T> {
    protected abstract void onDisplayImage(Context context, ImageView imageView, T t);

    protected ImageView generateImageView(Context context){
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }
}
