package com.itutorgroup.tutorchat.phone.activity.image;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.FolderAdapter;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.PhotoAdapter;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.model.Photo;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.model.PhotoDirectory;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.utils.MediaStoreHelper;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.utils.PhotoUtils;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.permission.PermissionsActivity;
import com.itutorgroup.tutorchat.phone.utils.permission.PermissionsManager;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ScreenUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.inject.annotation.OnClick;

public class PhotoPickerActivity extends BaseActivity implements PhotoAdapter.PhotoClickCallBack {

    public final static String TAG = "PhotoPickerActivity";

    public final static int REQUEST_CAMERA = 1;
    public final static int REQUEST_PREVIEW = 2;
    public final static String KEY_RESULT = "picker_result";
    public final static String KEY_SEND_ORIGINAL = "picker_original";

    /**
     * 是否显示相机
     */
    public final static String EXTRA_SHOW_CAMERA = "is_show_camera";
    /**
     * 照片选择模式
     */
    public final static String EXTRA_SELECT_MODE = "select_mode";
    /**
     * 最大选择数量
     */
    public final static String EXTRA_MAX_MUN = "max_num";
    /**
     * 单选
     */
    public final static int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public final static int MODE_MULTI = 1;
    /**
     * 默认最大选择数量
     */
    public final static int DEFAULT_NUM = 9;

    private final static String ALL_PHOTO = "所有图片";
    /**
     * 是否显示相机，默认不显示
     */
    private boolean mIsShowCamera = false;
    /**
     * 照片选择模式，默认是单选模式
     */
    private int mSelectMode = 0;
    /**
     * 最大选择数量，仅多选模式有用
     */
    private int mMaxNum;

    /**
     * 是否发送原图
     */
    private boolean mSendOriginal;

    private GridView mGridView;
    private ArrayList<PhotoDirectory> mSrcFloderMap;
    private ArrayList<Photo> mPhotoLists = new ArrayList<>();
    private ArrayList<String> mSelectList = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private ListView mFloderListView;

    private TextView mPhotoNumTV;
    private TextView mPhotoNameTV;
    private HeaderLayout mHeaderLayout;
    private TextView mTvConfirm;

    /**
     * 文件夹列表是否处于显示状态
     */
    boolean mIsFloderViewShow = false;
    /**
     * 文件夹列表是否被初始化，确保只被初始化一次
     */
    boolean mIsFloderViewInit = false;

    /**
     * 拍照时存储拍照结果的临时文件
     */
    private File mTmpFile;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_photo_picker);
        mSrcFloderMap = new ArrayList<>();
        initIntentParams();
        initView();
        if (PermissionsManager.getInstance().checkPermissions(this, PermissionsManager.PERMISSION_GROUP_EXTERNAL_STORAGE)) {
            loadData();
        }
    }

    private void loadData() {
        if (!PhotoUtils.isExternalStorageAvailable()) {
            ToastUtil.show(R.string.tip_storage_access_failed);
            finish();
            return;
        }

        MediaStoreHelper.getPhotoDirs(this, new Bundle(),
                new MediaStoreHelper.PhotosResultCallback() {

                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        mSrcFloderMap.clear();
                        mSrcFloderMap.addAll(dirs);
                        getPhotosSuccess();
                    }
                });
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.photo_gridview);
        mPhotoNumTV = (TextView) findViewById(R.id.tv_selected_preview);
        mPhotoNameTV = (TextView) findViewById(R.id.floder_name);
        mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);

        mHeaderLayout.mContentView.setBackgroundColor(Color.BLACK);
        mHeaderLayout.title(getString(R.string.picker_image_and_video))
                .autoCancel(PhotoPickerActivity.this);
        if (mSelectMode == MODE_MULTI) {
            mTvConfirm = mHeaderLayout.addRightText(getString(R.string.send), mOnConfirmClickListener);
            mTvConfirm.setBackgroundResource(R.drawable.bg_btn_green_radius);
            int padding = PixelUtil.dp2px(5);
            mTvConfirm.setPadding(padding * 2, padding, padding * 2, padding);
            mTvConfirm.setEnabled(false);
        }
    }

    @OnClick(id = R.id.tv_selected_preview)
    void onSelectedPreviewClick() {
        mSelectList = mPhotoAdapter.getSelectList();
        if (mSelectList == null || mSelectList.size() == 0) {
            return;
        }
        Intent intent = new Intent(mContext, PhotoPickerDetailActivity.class);
        intent.putExtra("position", 0);
        intent.putStringArrayListExtra("selected", mSelectList);
        intent.putExtra("original", mSendOriginal);
        intent.putExtra("max", mMaxNum);
        intent.putStringArrayListExtra("data", mSelectList);
        startActivityForResult(intent, PhotoPickerActivity.REQUEST_PREVIEW);
    }

    /**
     * 初始化选项参数
     */
    private void initIntentParams() {
        mIsShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, false);
        mSelectMode = getIntent().getIntExtra(EXTRA_SELECT_MODE, MODE_SINGLE);
        mMaxNum = getIntent().getIntExtra(EXTRA_MAX_MUN, DEFAULT_NUM);
    }

    private void getPhotosSuccess() {
        mPhotoLists.addAll(mSrcFloderMap.get(0).getPhotos());

        mPhotoAdapter = new PhotoAdapter(this, mPhotoLists, mSendOriginal);
        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
        mPhotoAdapter.setSelectMode(mSelectMode);
        mPhotoAdapter.setMaxNum(mMaxNum);
        mPhotoAdapter.setPhotoClickCallBack(this);
        mGridView.setAdapter(mPhotoAdapter);
        final List<PhotoDirectory> folders = new ArrayList<PhotoDirectory>();
        for (int i = 0; i < mSrcFloderMap.size(); i++) {
            if (i == 0) {
                PhotoDirectory folder = mSrcFloderMap.get(i);
                folder.setIsSelected(true);
                folders.add(0, folder);
            } else {
                folders.add(mSrcFloderMap.get(i));
            }
        }
        mPhotoNameTV.setOnClickListener(new View.OnClickListener() {
            //@TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                toggleFolderList(folders);
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mPhotoAdapter.isShowCamera() && position == 0) {
                    showCamera();
                    return;
                }
                selectPhoto(mPhotoAdapter.getItem(position));
            }
        });
    }

    /**
     * 点击选择某张照片
     *
     * @param photo 某张照片
     */
    private void selectPhoto(Photo photo) {
        if (photo == null) {
            return;
        }
        String path = photo.getPath();
        if (mSelectMode == MODE_SINGLE) {
            mSelectList.add(path);
            returnData();
        }
    }

    @Override
    public void onPhotoClick() {
        mSelectList = mPhotoAdapter.getSelectList();
        if (mSelectList != null && mSelectList.size() > 0) {
            mTvConfirm.setText(getString(R.string.picker_commit_num, mSelectList.size(), mMaxNum));
            mTvConfirm.setEnabled(true);
            mPhotoNumTV.setText(getString(R.string.preview_num, mSelectList.size()));
            mPhotoNumTV.setEnabled(true);
        } else {
            mTvConfirm.setText(getString(R.string.send));
            mTvConfirm.setEnabled(false);
            mPhotoNumTV.setText(getString(R.string.preview));
            mPhotoNumTV.setEnabled(false);
        }
    }

    /**
     * 返回选择图片的路径
     */
    private void returnData() {
        // 返回已选择的图片数据
        Intent data = new Intent();
        data.putStringArrayListExtra(KEY_RESULT, mSelectList);
        data.putExtra(KEY_SEND_ORIGINAL, mSendOriginal);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 显示或者隐藏文件夹列表
     *
     * @param folders
     */
    private void toggleFolderList(final List<PhotoDirectory> folders) {
        //初始化文件夹列表
        if (!mIsFloderViewInit) {
            ViewStub folderStub = (ViewStub) findViewById(R.id.folder_stub);
            folderStub.inflate();
            View dimLayout = findViewById(R.id.dim_layout);
            mFloderListView = (ListView) findViewById(R.id.list_folder);
            final FolderAdapter adapter = new FolderAdapter(this, folders);
            mFloderListView.setAdapter(adapter);
            mFloderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (PhotoDirectory folder : folders) {
                        folder.setIsSelected(false);
                    }
                    PhotoDirectory folder = folders.get(position);
                    folder.setIsSelected(true);
                    adapter.notifyDataSetChanged();

                    mPhotoLists.clear();
                    mPhotoLists.addAll(folder.getPhotos());
                    if (ALL_PHOTO.equals(folder.getName())) {
                        mPhotoAdapter.setIsShowCamera(mIsShowCamera);
                    } else {
                        mPhotoAdapter.setIsShowCamera(false);
                    }
                    //这里重新设置adapter而不是直接notifyDataSetChanged，是让GridView返回顶部
                    mGridView.setAdapter(mPhotoAdapter);
                    mPhotoNameTV.setText(folder.getName());
                    toggle();
                }
            });
            dimLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mIsFloderViewShow) {
                        toggle();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            initAnimation(dimLayout);
            mIsFloderViewInit = true;
        }
        toggle();
    }

    @Override
    public void onBackPressed() {
        if (mIsFloderViewShow) {
            outAnimatorSet.start();
            mIsFloderViewShow = false;
            return;
        }
        super.onBackPressed();
    }

    /**
     * 弹出或者收起文件夹列表
     */
    private void toggle() {
        if (mIsFloderViewShow) {
            outAnimatorSet.start();
            mIsFloderViewShow = false;
        } else {
            inAnimatorSet.start();
            mIsFloderViewShow = true;
        }
    }


    /**
     * 初始化文件夹列表的显示隐藏动画
     */
    AnimatorSet inAnimatorSet = new AnimatorSet();
    AnimatorSet outAnimatorSet = new AnimatorSet();

    private void initAnimation(View dimLayout) {
        ObjectAnimator alphaInAnimator, alphaOutAnimator, transInAnimator, transOutAnimator;
        //获取actionBar的高
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        /**
         * 这里的高度是，屏幕高度减去上、下tab栏，并且上面留有一个tab栏的高度
         * 所以这里减去3个actionBarHeight的高度
         */
        int height = PhotoUtils.getHeightInPx(this) - 3 * actionBarHeight;
        alphaInAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0f, 0.7f);
        alphaOutAnimator = ObjectAnimator.ofFloat(dimLayout, "alpha", 0.7f, 0f);
        transInAnimator = ObjectAnimator.ofFloat(mFloderListView, "translationY", height, 0);
        transOutAnimator = ObjectAnimator.ofFloat(mFloderListView, "translationY", 0, ScreenUtil.getScreenHeight(this));

        LinearInterpolator linearInterpolator = new LinearInterpolator();

        inAnimatorSet.play(transInAnimator).with(alphaInAnimator);
        inAnimatorSet.setDuration(300);
        inAnimatorSet.setInterpolator(linearInterpolator);
        outAnimatorSet.play(transOutAnimator).with(alphaOutAnimator);
        outAnimatorSet.setDuration(300);
        outAnimatorSet.setInterpolator(linearInterpolator);
    }

    /**
     * 选择文件夹
     *
     * @param photoFolder
     */
    public void selectFolder(PhotoDirectory photoFolder) {
        mPhotoAdapter.setData(photoFolder.getPhotos());
        mPhotoAdapter.notifyDataSetChanged();
    }

    /**
     * 选择相机
     */
    private void showCamera() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = PhotoUtils.createFile(getApplicationContext());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            ToastUtil.show(R.string.picker_msg_no_camera);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            onTakePictureResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_PREVIEW) {
            onPreviewResult(requestCode, resultCode, data);
        } else if (requestCode == PermissionsManager.REQUEST_CODE) {
            onCheckPermissionResult(requestCode, resultCode, data);
        }
    }

    private void onCheckPermissionResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        } else {
            loadData();
        }
    }

    private void onPreviewResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.hasExtra(PhotoPickerActivity.KEY_RESULT)) {
            mSelectList = data.getStringArrayListExtra(KEY_RESULT);
        }
        mSendOriginal = data.getBooleanExtra(KEY_SEND_ORIGINAL, false);
        mPhotoAdapter.setSelectedPhotos(mSelectList, mSendOriginal);
        onPhotoClick();
        if (resultCode == PhotoPickerDetailActivity.RESULT_SEND) {
            returnData();
        }
    }

    private void onTakePictureResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (mTmpFile != null) {
                mSelectList.add(mTmpFile.getAbsolutePath());
                returnData();
            }
        } else {
            if (mTmpFile != null && mTmpFile.exists()) {
                mTmpFile.delete();
            }
        }
    }

    private View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSelectList = mPhotoAdapter.getSelectList();
            returnData();
        }
    };
}
