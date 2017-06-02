package com.itutorgroup.tutorchat.phone.utils.download;

import android.text.TextUtils;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/27.
 */
public class DownloadManager {
    private static DownloadManager sInstance;

    public static class TYPE {
        public static final int IMAGE = 1;
    }

    public static DownloadManager getInstance() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }

    private DownloadManager() {
    }

    public void download(int type, final String url, final IDownloadCallBack callBack) {
        if (callBack == null) {
            throw new IllegalArgumentException("the callback must not be null.");
        } else if (TextUtils.isEmpty(url)) {
            return;
        }
        Observable.just(type)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Integer, IDownloadListener>() {
                    @Override
                    public IDownloadListener call(Integer type) {
                        IDownloadListener downloader = null;
                        switch (type) {
                            case TYPE.IMAGE:
                                downloader = ImageDownloader.getInstance();
                                break;
                        }
                        return downloader;
                    }
                })
                .filter(new Func1<IDownloadListener, Boolean>() {
                    @Override
                    public Boolean call(IDownloadListener iDownloadListener) {
                        return iDownloadListener != null;
                    }
                })
                .map(new Func1<IDownloadListener, File>() {
                    @Override
                    public File call(IDownloadListener downloader) {
                        return downloader.download(url);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        if (file != null && file.exists()) {
                            callBack.onDownloadSuccess(url, file);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        callBack.onDownloadFailed(url);
                    }
                });
    }

    public interface IDownloadCallBack {
        void onDownloadSuccess(String url, File file);

        void onDownloadFailed(String url);
    }

    public interface IDownloadListener {
        File download(String url);
    }
}
