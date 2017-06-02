package com.itutorgroup.tutorchat.phone.ui.photo.picker;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerActivity;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerDetailActivity;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.model.Photo;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.utils.PhotoUtils;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.widgets.SquareImageView;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PhotoAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_PHOTO = 1;

    private ArrayList<Photo> mData;
    //存放已选中的Photo数据
    private ArrayList<String> mSelectList;
    private boolean mSendOriginal;
    private Context mContext;
    private int mWidth;
    //是否显示相机，默认不显示
    private boolean mIsShowCamera = false;
    //照片选择模式，默认单选
    private int mSelectMode = PhotoPickerActivity.MODE_SINGLE;
    //图片选择数量
    private int mMaxNum = PhotoPickerActivity.DEFAULT_NUM;

    private View.OnClickListener mOnPhotoClick;
    private View.OnClickListener mOnSelectClick;
    private PhotoClickCallBack mCallBack;

    public PhotoAdapter(Context context, ArrayList<Photo> mData, boolean sendOriginal) {
        this.mData = mData;
        this.mContext = context;
        this.mSendOriginal = sendOriginal;
        int screenWidth = PhotoUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - PhotoUtils.dip2px(mContext, 4)) / 3;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mIsShowCamera) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Photo getItem(int position) {
        if (mIsShowCamera) {
            if (position == 0) {
                return null;
            }
            return mData.get(position - 1);
        } else {
            return mData.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getId();
    }

    public void setData(ArrayList<Photo> mData) {
        this.mData = mData;
    }

    public void setIsShowCamera(boolean isShowCamera) {
        this.mIsShowCamera = isShowCamera;
    }

    public boolean isShowCamera() {
        return mIsShowCamera;
    }

    public void setMaxNum(int maxNum) {
        this.mMaxNum = maxNum;
    }

    public void setPhotoClickCallBack(PhotoClickCallBack callback) {
        mCallBack = callback;
    }


    /**
     * 获取已选中相片
     *
     * @return 已选中相片
     */
    public ArrayList<String> getSelectList() {
        return mSelectList;
    }

    public void setSelectedPhotos(ArrayList<String> list, boolean sendOriginal) {
        mSelectList = list;
        mSendOriginal = sendOriginal;
        notifyDataSetChanged();
    }

    public void setSelectMode(int selectMode) {
        this.mSelectMode = selectMode;
        if (mSelectMode == PhotoPickerActivity.MODE_MULTI) {
            initMultiMode();
        }
    }

    /**
     * 初始化多选模式所需要的参数
     */
    private void initMultiMode() {
        mSelectList = new ArrayList<>();
        mOnPhotoClick = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mData == null || mData.size() == 0) {
                    return;
                }
                Observable.just(mData)
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ArrayList<Photo>, ArrayList<String>>() {
                            @Override
                            public ArrayList<String> call(ArrayList<Photo> photos) {
                                ArrayList<String> list = new ArrayList<>();
                                for (Photo photo : photos) {
                                    list.add(photo.getPath());
                                }
                                return list;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<String>>() {
                            @Override
                            public void call(ArrayList<String> list) {
                                Intent intent = new Intent(mContext, PhotoPickerDetailActivity.class);
                                int position = (int) v.getTag(R.id.tag_position);
                                intent.putExtra("position", position);
                                intent.putStringArrayListExtra("selected", mSelectList);
                                intent.putExtra("original", mSendOriginal);
                                intent.putExtra("max", mMaxNum);
                                intent.putStringArrayListExtra("data", list);
                                ((Activity) mContext).startActivityForResult(intent, PhotoPickerActivity.REQUEST_PREVIEW);
                            }
                        }, CommonUtil.ACTION_EXCEPTION);
            }
        };

        mOnSelectClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parent = (View) v.getTag(R.id.tag_bean);
                String path = ((SquareImageView) parent.findViewById(R.id.imageview_photo)).key;
                if (mSelectList.contains(path)) {
                    parent.findViewById(R.id.mask).setVisibility(View.GONE);
                    parent.findViewById(R.id.checkmark).setSelected(false);
                    mSelectList.remove(path);
                } else {
                    if (mSelectList.size() >= mMaxNum) {
                        ToastUtil.show(R.string.picker_msg_maxi_capacity);
                        return;
                    }
                    mSelectList.add(path);
                    parent.findViewById(R.id.mask).setVisibility(View.VISIBLE);
                    parent.findViewById(R.id.checkmark).setSelected(true);
                }
                if (mCallBack != null) {
                    mCallBack.onPhotoClick();
                }
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.picker_item_camera_layout, null);
            convertView.setTag(null);
            //设置高度等于宽度
            GridView.LayoutParams lp = new GridView.LayoutParams(mWidth, mWidth);
            convertView.setLayoutParams(lp);
        } else {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.picker_item_photo_layout, null);
                holder.photoImageView = (ImageView) convertView.findViewById(R.id.imageview_photo);
                holder.selectView = (ImageView) convertView.findViewById(R.id.checkmark);
                holder.maskView = convertView.findViewById(R.id.mask);
                holder.wrapLayout = (FrameLayout) convertView.findViewById(R.id.wrap_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.photoImageView.setImageResource(R.drawable.picker_ic_photo_loading);
            Photo photo = getItem(position);
            if (mSelectMode == PhotoPickerActivity.MODE_MULTI) {
                holder.wrapLayout.setTag(R.id.tag_position, position);
                holder.wrapLayout.setOnClickListener(mOnPhotoClick);
                holder.selectView.setTag(R.id.tag_bean, holder.wrapLayout);
                holder.selectView.setOnClickListener(mOnSelectClick);
                ((SquareImageView) holder.photoImageView).key = (photo.getPath());
                holder.selectView.setVisibility(View.VISIBLE);
                if (mSelectList != null && mSelectList.contains(photo.getPath())) {
                    holder.selectView.setSelected(true);
                    holder.maskView.setVisibility(View.VISIBLE);
                } else {
                    holder.selectView.setSelected(false);
                    holder.maskView.setVisibility(View.GONE);
                }
            } else {
                holder.selectView.setVisibility(View.GONE);
            }
            Glide.with(mContext).load(photo.getPath()).dontAnimate()
                    .thumbnail(0.1f).into(holder.photoImageView);
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoImageView;
        private ImageView selectView;
        private View maskView;
        private FrameLayout wrapLayout;
    }

    /**
     * 多选时，点击相片的回调接口
     */
    public interface PhotoClickCallBack {
        void onPhotoClick();
    }
}
