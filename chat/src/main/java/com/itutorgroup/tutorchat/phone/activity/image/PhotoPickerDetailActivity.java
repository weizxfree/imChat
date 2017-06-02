package com.itutorgroup.tutorchat.phone.activity.image;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.image.ViewPagerGalleryAdapter;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/25.
 */
public class PhotoPickerDetailActivity extends BaseActivity {

    public static final int RESULT_SEND = 0x111;

    @InjectExtra(key = "position")
    int mPosition;

    @InjectExtra(key = "max")
    int mMaxNum;

    @InjectExtra(key = "data")
    ArrayList<String> mData;

    @InjectExtra(key = "original")
    boolean mSendOriginal;

    @InjectExtra(key = "selected")
    ArrayList<String> mSelectedList;

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.view_pager)
    ViewPager mViewPager;

    @InjectView(id = R.id.cb_original)
    CheckBox mCheckBoxOriginal;

    @InjectView(id = R.id.cb_choose)
    CheckBox mCheckBoxChoose;

    private TextView mTvConfirm;
    private TextView mTvLeftMenu;

    private HashMap<String, Long> mSizeMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker_detail);
        initView();
    }

    private void initView() {
        mHeaderLayout.mContentView.setBackgroundColor(Color.BLACK);
        mTvLeftMenu = mHeaderLayout.addCancelMenu(mOnBackClickListener);
        mTvConfirm = mHeaderLayout.addRightText(getString(R.string.send), mOnConfirmClickListener);
        mTvConfirm.setBackgroundResource(R.drawable.bg_btn_green_radius);
        int padding = PixelUtil.dp2px(5);
        mTvConfirm.setPadding(padding * 2, padding, padding * 2, padding);
        if (mSelectedList == null) {
            mSelectedList = new ArrayList<>();
        }
        mSizeMap = new HashMap<>();
        initListener();
        if (mData != null && mData.size() > 0) {
            updateTitle();
            ViewPagerGalleryAdapter adapter = new ViewPagerGalleryAdapter(PhotoPickerDetailActivity.this, mData, false);
            mViewPager.setAdapter(adapter);
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
            mViewPager.setCurrentItem(mPosition);
            if (mPosition == 0) {
                mOnPageChangeListener.onPageSelected(0);
            }
            refreshAllPicSize();
        } else {
            finish();
        }
    }

    private void initListener() {
        mCheckBoxChoose.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCheckBoxOriginal.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private void updateTitle() {
        mHeaderLayout.title(getString(R.string.title_image_preview, mPosition + 1, mData.size()));
        if (mSelectedList.size() > 0) {
            mTvConfirm.setText(getString(R.string.picker_commit_num, mSelectedList.size(), mMaxNum));
        } else {
            mTvConfirm.setText(R.string.send);
        }
    }

    private void initCheckBoxState() {
        String path = mData.get(mPosition);
        boolean containsKey = mSelectedList.contains(path);
        mFlag = false;
        mCheckBoxChoose.setChecked(containsKey);
        mCheckBoxOriginal.setChecked(mSendOriginal);
        mFlag = true;
    }

    private boolean mFlag = true;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mFlag) {
                switch (buttonView.getId()) {
                    case R.id.cb_original:
                        onSendOriginalCheckedChange(isChecked);
                        break;
                    case R.id.cb_choose:
                        onChooseCheckedChange(isChecked);
                        break;
                }
                updateTitle();
            }
        }
    };

    private void onSendOriginalCheckedChange(boolean isChecked) {
        mSendOriginal = isChecked;
        if (isChecked && mSelectedList.isEmpty()) {
            mCheckBoxChoose.setChecked(true);
        }
    }

    private void onChooseCheckedChange(boolean isChecked) {
        String path = mData.get(mPosition);
        if (isChecked) {
            if (mSelectedList.size() >= mMaxNum) {
                ToastUtil.show(R.string.picker_msg_maxi_capacity);
                mCheckBoxChoose.setChecked(false);
                return;
            }
            if (!mSelectedList.contains(path)) {
                mSelectedList.add(path);
            }
        } else {
            mSelectedList.remove(path);
        }
        refreshAllPicSize();
    }

    private void refreshAllPicSize() {
        if (mSelectedList == null || mSelectedList.size() == 0) {
            mCheckBoxOriginal.setText(R.string.picker_original_image);
            return;
        }
        Observable.just(mSelectedList)
                .subscribeOn(Schedulers.io())
                .map(new Func1<ArrayList<String>, HashMap<String, Long>>() {
                         @Override
                         public HashMap<String, Long> call(ArrayList<String> list) {
                             for (String path : list) {
                                 if (!mSizeMap.containsKey(path)) {
                                     long size = FileUtils.getFileSize(path);
                                     mSizeMap.put(path, size);
                                 }
                             }
                             return mSizeMap;
                         }
                     }
                )
                .filter(new Func1<HashMap<String, Long>, Boolean>() {
                            @Override
                            public Boolean call(HashMap<String, Long> map) {
                                return map != null && map.size() > 0;
                            }
                        }
                )
                .map(new Func1<HashMap<String, Long>, String>() {
                         @Override
                         public String call(HashMap<String, Long> map) {
                             long allSize = 0;
                             for (String path : mSelectedList) {
                                 allSize += mSizeMap.get(path);
                             }
                             return FileUtils.formatSize(allSize);
                         }
                     }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                               @Override
                               public void call(String sizeStr) {
                                   mCheckBoxOriginal.setText(getString(R.string.picker_original_image_size, sizeStr));
                               }
                           }, CommonUtil.ACTION_EXCEPTION
                );
    }

    private View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSelectedList.isEmpty()) {
                mSelectedList.add(mData.get(mPosition));
            }
            Intent intent = new Intent();
            intent.putStringArrayListExtra(PhotoPickerActivity.KEY_RESULT, mSelectedList);
            intent.putExtra(PhotoPickerActivity.KEY_SEND_ORIGINAL, mSendOriginal);
            setResult(RESULT_SEND, intent);
            finish();
        }
    };

    private View.OnClickListener mOnBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(PhotoPickerActivity.KEY_RESULT, mSelectedList);
            intent.putExtra(PhotoPickerActivity.KEY_SEND_ORIGINAL, mSendOriginal);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            mPosition = position;
            updateTitle();
            initCheckBoxState();
        }
    };

    @Override
    public void onBackPressed() {
        mOnBackClickListener.onClick(mTvLeftMenu);
    }
}
