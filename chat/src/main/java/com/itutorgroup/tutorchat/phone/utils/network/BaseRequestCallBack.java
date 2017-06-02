package com.itutorgroup.tutorchat.phone.utils.network;

import android.content.Context;

import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.ui.dialog.LoadingDialog;
import com.itutorgroup.tutorchat.phone.utils.AesEncryptionUtil;
import com.itutorgroup.tutorchat.phone.utils.ProtoStuffSerializerUtil;
import com.itutorgroup.tutorchat.phone.utils.StreamUtils;
import com.lzy.okhttputils.callback.AbsCallback;
import com.lzy.okhttputils.request.BaseRequest;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public abstract class BaseRequestCallBack<T extends CommonResponse> extends AbsCallback<T> {
    private Class<T> mClass;
    private Context mContext;
    private boolean mShowDialog;
    private String mAESValue;
    private LoadingDialog mDialog;

    public BaseRequestCallBack(Class clazz, Context context, String aesValue, boolean showDialog) {
        mClass = clazz;
        mContext = context;
        mAESValue = aesValue;
        mShowDialog = showDialog;
    }

    @Override
    public T parseNetworkResponse(Response response) throws Exception {
        byte[] tmp = response.body().bytes();
        short headLen = StreamUtils.byteToShort(StreamUtils.subBytes(tmp, 4, 2));
        byte[] body = StreamUtils.subBytes(tmp, headLen, tmp.length - headLen);
        if (headLen == 20) {
            byte[] opBytes = StreamUtils.subBytes(tmp, 8, 4);
            int operation = StreamUtils.bytesToInt(opBytes);
            if (operation != Operation.AES_DECRYPT_FAILED) {
                body = AesEncryptionUtil.decrypt(body, mAESValue, Constant.AES_IV);
            }
        }
        final T result = ProtoStuffSerializerUtil.deserialize(body, mClass);
        return result;
    }

    @Override
    public void onAfter(boolean isFromCache, T t, Call call, Response response, Exception e) {
        if (mShowDialog && mContext != null && mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    public void onBefore(BaseRequest request) {
        if (mShowDialog && mContext != null) {
            mDialog = new LoadingDialog(mContext);
            mDialog.show();
        }
    }
}
