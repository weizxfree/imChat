package com.itutorgroup.tutorchat.phone.utils.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.event.GlobalActionEvent;
import com.itutorgroup.tutorchat.phone.domain.response.UpdateUserPhotoResponse;
import com.itutorgroup.tutorchat.phone.ui.photo.crop.Crop;
import com.itutorgroup.tutorchat.phone.ui.popup.SelectPicPopupWindow;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.io.File;

/**
 * Created by joyinzhao on 2016/10/13.
 */
public class AvatarPhotoHelper {

    private Activity mActivity;
    private SelectPicPopupWindow mPopupSelectAvatar;

    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;

    public interface IPickPhotoListener {
        void onOpenPicker();
    }

    public AvatarPhotoHelper(Activity activity) {
        mActivity = activity;
    }

    public void showChooseAvatarDialog(View parent) {
        if (mPopupSelectAvatar == null) {
            mPopupSelectAvatar = new SelectPicPopupWindow(mActivity, mOnMenuITemClickListener);
        }
        //显示窗口
        mPopupSelectAvatar.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }


    private View.OnClickListener mOnMenuITemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_pick_photo:
//                    Intent openAlbumIntent = new Intent(mActivity, PhotoPickerActivity.class);
//                    openAlbumIntent.setType("image/*");
                    Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    mActivity.startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                    break;
                case R.id.tv_take_photo:
                    Intent openCameraIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    tempUri = Uri.fromFile(new File(Environment
                            .getExternalStorageDirectory(), "image.tmp"));
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                    mActivity.startActivityForResult(openCameraIntent, TAKE_PICTURE);
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    Crop.of(tempUri, Uri.fromFile(FileUtils.getHeadImgCropTmpFile())).start(mActivity);
                    break;
                case CHOOSE_PICTURE:
//                    ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
//                    if (result != null && result.size() > 0) {
//                        Crop.of(Uri.fromFile(new File(result.get(0))), Uri.fromFile(FileUtils.getHeadImgCropTmpFile())).asSquare().withMaxSize(200, 200).start(mActivity);
//                    }
                    Crop.of(data.getData(), Uri.fromFile(FileUtils.getHeadImgCropTmpFile())).asSquare().asPng(false).withMaxSize(200, 200).start(mActivity);
                    break;
                case Crop.REQUEST_CROP:
                    byte[] bytes = com.itutorgroup.tutorchat.phone.utils.PhotoUtils.encodeImageToByte(FileUtils.getHeadImgCropTmpPath());
                    AccountManager.getInstance().updateAvatar(mActivity, bytes, new RequestHandler.RequestListener<UpdateUserPhotoResponse>() {
                        @Override
                        public void onResponse(UpdateUserPhotoResponse response, Bundle bundle) {
                            EventBusManager.getInstance().post(GlobalActionEvent.getInstance(GlobalActionEvent.ACTION_AUTO_REFRESH_CURRENT_USER_INFO));
                        }
                    });
                    break;
            }
        } else if (resultCode == Crop.RESULT_ERROR && requestCode == Crop.REQUEST_CROP) {
            Throwable throwable = (Throwable) data.getSerializableExtra("error");
            if (throwable != null) {
                if (throwable.toString().contains("Image failed to decode using GIF decoder")) {
                    ToastUtil.show("不支持的文件格式");
                }
            }
        }
    }
}
