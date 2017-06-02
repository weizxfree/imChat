package com.itutorgroup.tutorchat.phone.activity.image;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.image.ViewPagerGalleryAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.jude.swipbackhelper.SwipeBackHelper;

import java.util.ArrayList;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/10/25.
 */
public class ImagePreviewActivity extends BaseActivity {

    private static final String TRANSITION_NAME_CONTENT = "trans_pager";

    @InjectExtra(key = "pathList")
    ArrayList<String> mPathList;

    @InjectExtra(key = "defaultIndex")
    int mDefaultIndex;

    @InjectView(id = R.id.view_pager)
    ViewPager mViewPager;

    private GestureDetectorCompat mGestureDetectorCompat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        SwipeBackHelper.getCurrentPage(this).setSwipeBackEnable(false);

        ViewPagerGalleryAdapter adapter = new ViewPagerGalleryAdapter(this, mPathList, true);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mDefaultIndex);

        mGestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetectorCompat.onTouchEvent(ev);
        try{
            return super.dispatchTouchEvent(ev);
        } catch(IllegalArgumentException  ex) {
        }
        return false;
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            finish();
            return super.onSingleTapUp(e);
        }
    }

    /**
     * 启动图片预览页面
     *
     * @param activity   当前activity
     * @param v          共享的View
     * @param pathList   预览页面将显示的图片地址列表
     * @param defImgPath 默认显示的图片地址（必须包含在list中）
     */
    public static void startNewActivity(Activity activity, View v, ArrayList<String> pathList, String defImgPath) {
        if (pathList != null && pathList.size() > 0 && !TextUtils.isEmpty(defImgPath)) {
            int index = pathList.indexOf(defImgPath);
            if (index >= 0) {
                Intent intent = new Intent(activity, ImagePreviewActivity.class);
                intent.putExtra("pathList", pathList);
                intent.putExtra("defaultIndex", index);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.center_scale_in, R.anim.empty);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.empty, R.anim.center_scale_out);
    }
}
