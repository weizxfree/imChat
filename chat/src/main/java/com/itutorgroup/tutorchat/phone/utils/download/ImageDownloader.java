package com.itutorgroup.tutorchat.phone.utils.download;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.itutorgroup.tutorchat.phone.app.LPApp;

import java.io.File;

/**
 * Created by joyinzhao on 2016/10/27.
 */
public class ImageDownloader implements DownloadManager.IDownloadListener {
    private static ImageDownloader sInstance;

    public static ImageDownloader getInstance() {
        if (sInstance == null) {
            synchronized (ImageDownloader.class) {
                if (sInstance == null) {
                    sInstance = new ImageDownloader();
                }
            }
        }
        return sInstance;
    }

    private ImageDownloader() {
    }

    @Override
    public File download(String url) {
        try {
            return Glide.with(LPApp.getInstance()).load(url).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
