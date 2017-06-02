package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.Bundle;
import android.os.Environment;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.request.CheckFileIsExitRequest;
import com.itutorgroup.tutorchat.phone.domain.request.DownImageFileRequest;
import com.itutorgroup.tutorchat.phone.domain.request.DownVoiceFileRequest;
import com.itutorgroup.tutorchat.phone.domain.request.UploadImageFileRequest;
import com.itutorgroup.tutorchat.phone.domain.request.UploadVoiceFileRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CheckFileIsExitResponse;
import com.itutorgroup.tutorchat.phone.domain.response.DownImageFileResponse;
import com.itutorgroup.tutorchat.phone.domain.response.DownVoiceFileResponse;
import com.itutorgroup.tutorchat.phone.domain.response.UploadImageFileResponse;
import com.itutorgroup.tutorchat.phone.domain.response.UploadVoiceFileResponse;
import com.itutorgroup.tutorchat.phone.utils.CompressImg;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.MD5Util;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.network.NBundle;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.io.File;

import cn.salesuite.saf.utils.StringUtils;

public class FileManager {
    private static FileManager sInstance;

    public static FileManager getInstance() {
        if (sInstance == null) {
            synchronized (FileManager.class) {
                if (sInstance == null) {
                    sInstance = new FileManager();
                }
            }
        }
        return sInstance;
    }

    public void uploadImageFile(byte[] fileBytes, RequestHandler.RequestListener<UploadImageFileResponse> listener){
        UploadImageFileRequest request = new UploadImageFileRequest();
        request.init();
        request.FileData = fileBytes;
        request.ExtendName = "jpg";
        new RequestHandler<UploadImageFileResponse>()
                .operation(Operation.UPLOAD_IMAGE_FILE)
                .request(request)
                .exec(UploadImageFileResponse.class, listener);
    }


    public void UploadVoiceFile(byte[] fileBytes,float seconds, RequestHandler.RequestListener<UploadVoiceFileResponse> listener){
        UploadVoiceFileRequest request = new UploadVoiceFileRequest();
        request.init();
        request.FileData = fileBytes;
        request.ExtendName = "amr";
        request.PlayTime = (long) (seconds);
        new RequestHandler<UploadVoiceFileResponse>()
                .operation(Operation.UPLOAD_VOICE_FILE)
                .request(request)
                .exec(UploadVoiceFileResponse.class, listener);
    }


    public void downloadImageFile(String imageFileID, final CommonLoadingListener<Boolean> listener){
        DownImageFileRequest request = new DownImageFileRequest();
        request.init();
        request.ImageFileID = imageFileID;
        request.IsOriginalImage = true;
        new RequestHandler<DownImageFileResponse>()
                .operation(Operation.DOWNDOAD_IMAGE_FILE)
                .bundle(new NBundle().ignoreAllError().build())
                .request(request)
                .exec(DownImageFileResponse.class, new RequestHandler.RequestListener<DownImageFileResponse>(){
                    @Override
                    public void onResponse(final DownImageFileResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        FileUtils.byte2File(response.FileData, FileUtils.getParentPath(true), response.ImageFileID);
                        if(listener!=null){
                            listener.onResponse(true);
                        }
                    }

                    @Override
                    public void onError(int errorCode, DownImageFileResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        if(listener!=null){
                            listener.onResponse(false);
                        }
                    }
                });
    }


    public void downloadVoiceFile(String imageFileID, final CommonLoadingListener<Integer> listener){
        DownVoiceFileRequest request = new DownVoiceFileRequest();
        request.init();
        request.VoiceFileID = imageFileID;
        new RequestHandler<DownVoiceFileResponse>()
                .operation(Operation.DOWNDOAD_VOICE_FILE)
                .bundle(new NBundle().ignoreAllError().build())
                .request(request)
                .exec(DownVoiceFileResponse.class,new RequestHandler.RequestListener<DownVoiceFileResponse>(){
                    @Override
                    public void onResponse(final DownVoiceFileResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        FileUtils.byte2File(response.FileData, FileUtils.getParentPath(false), response.VoiceFileID+".amr");
                        if(listener!=null){
                            listener.onResponse((int) response.PlayTime);
                        }
                    }

                    @Override
                    public void onError(int errorCode, DownVoiceFileResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        if(listener!=null){
                            listener.onResponse(-1);
                        }
                    }
                });
    }




    public void CheckFileIsExitRequest(final byte[] fileMd5, final CommonLoadingListener<Boolean> listener){

        if(StringUtils.isBlank(fileMd5)|| fileMd5.length == 0)
            return;
        final CheckFileIsExitRequest request = new CheckFileIsExitRequest();
        request.init();
        request.FileMD5 = MD5Util.getMD5String(fileMd5);
        request.ByteLength = fileMd5.length;
        new RequestHandler<CheckFileIsExitResponse>()
                .operation(Operation.CHECK_FILE_IS_EXIT)
                .request(request)
                .exec(CheckFileIsExitResponse.class, new RequestHandler.RequestListener<CheckFileIsExitResponse>(){
                    @Override
                    public void onResponse(CheckFileIsExitResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        if(response != null && response.IsExit){
                            if(listener!=null){
                                listener.onResponse(true);
                            }
                        }else if(response != null && !response.IsExit){
                            FileManager.getInstance().uploadImageFile(fileMd5, new RequestHandler.RequestListener<UploadImageFileResponse>() {
                                @Override
                                public void onResponse(UploadImageFileResponse response, Bundle bundle) {
                                    super.onResponse(response, bundle);
                                    if(listener!=null){
                                        listener.onResponse(true);
                                    }
                                }

                                @Override
                                public void onError(int errorCode, UploadImageFileResponse response, Exception e, Bundle bundle) {
                                    super.onError(errorCode, response, e, bundle);
                                    if(listener!=null){
                                        listener.onResponse(false);
                                    }
                                }
                                @Override
                                public void onNullResponse(Bundle bundle) {
                                    super.onNullResponse(bundle);
                                    if(listener!=null){
                                        listener.onResponse(false);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int errorCode, CheckFileIsExitResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);
                        if(listener!=null){
                            listener.onResponse(false);
                        }
                    }
                });
    }
























    /**
     *
     * 原图根据是否在指定的目录，选择是否创建副本。
     * 非原图(缩略图)直接压缩到指定目录
     * @param originUrl
     * @param isOriginPic
     * @return
     */
    public byte[] getFileBytes(String originUrl,boolean isOriginPic){
        try {
            return CompressImg.saveCompressImg(originUrl,isOriginPic);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void reNameFile(String oldPath,String FileID){
        File oldFile = new File(oldPath);
        String newName = FileID;
        if(!oldFile.getName().equals(newName)){
            File newFile = new File(oldFile.getParent()+"/"+newName);
            if(!oldFile.exists()){
                return;
            }
            if(newFile.exists()){
                return;
            }else{
                newFile.getParentFile().mkdirs();
            }
            oldFile.renameTo(newFile);
        }
    }

    public void reNameVoiceFile(String oldPath,String FileID){
        File oldFile = new File(oldPath);
        String newName = FileID+".amr";
        if(!oldFile.getName().equals(newName)){
            File newFile = new File(oldFile.getParent()+"/"+newName);
            if(!oldFile.exists()){
                return;
            }
            if(newFile.exists()){
                return;
            }else{
                newFile.getParentFile().mkdirs();
            }
            oldFile.renameTo(newFile);
        }
    }

    public  String getPathByFileId(String fileID){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() +Constant.IMAGE_DIR+File.separator+fileID;
        } else {
            return  LPApp.getInstance().getCacheDir()+Constant.IMAGE_DIR+File.separator+fileID;
        }
    }

    public  String getVoicePathByFileId(String fileID){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory() +Constant.VOICE_DIR+File.separator+fileID + ".amr";
        } else {
            return  LPApp.getInstance().getCacheDir()+Constant.VOICE_DIR+File.separator+fileID + ".amr";
        }
    }





}
