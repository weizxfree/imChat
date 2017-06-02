package com.itutorgroup.tutorchat.phone.utils.ui;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.itutorgroup.tutorchat.phone.utils.PhotoUtils;

/**
 * Created by joyinzhao on 2017/1/23.
 */
public class ChatImageTransformation extends BitmapTransformation {

    private int mDirect;

    public ChatImageTransformation(Context context, int direct) {
        super(context);
        mDirect = direct;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return PhotoUtils.canvasTriangle(toTransform, mDirect);
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}
