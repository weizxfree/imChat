package com.itutorgroup.tutorchat.phone.utils.network;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.CommonRequest;
import com.itutorgroup.tutorchat.phone.domain.request.impl.IGetTickListener;
import com.itutorgroup.tutorchat.phone.domain.request.impl.ISetTickListener;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.StreamUtils;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.cache.CacheMode;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class RequestHandler<T extends CommonResponse> {

    public static final String TUTOR_MEDIA_TYPE = "application/x-TutorGroupIMFormatter";

    private Context mContext;
    private boolean mShowDialog = false;
    private Object mTag;
    private int mOperation = -1;
    private Object mRequest;
    private Bundle mBundle;
    private BaseRequestCallBack<T> mRequestCallBack;

    public RequestHandler() {
    }

    /**
     * 网络请求过程中显示对话框
     *
     * @param context 为null则不显示对话框
     */
    public RequestHandler dialog(Context context) {
        if (context == null) {
            return this;
        }
        mContext = context;
        mShowDialog = true;
        return this;
    }

    public RequestHandler tag(Object tag) {
        mTag = tag;
        return this;
    }

    public RequestHandler operation(int operation) {
        mOperation = operation;
        return this;
    }

    public RequestHandler request(Object request) {
        mRequest = request;
        return this;
    }

    public RequestHandler bundle(Bundle bundle) {
        mBundle = bundle;
        return this;
    }

    public RequestHandler exec(Class clazz, final AbsRequestListener listener) {
        checkParams();
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null!");
        }

        if (mRequest instanceof CommonRequest) {
            String userId = ((CommonRequest) mRequest).UserID;
            String token = ((CommonRequest) mRequest).Token;
            if (TextUtils.isEmpty(userId)) {
                LogUtil.d("userId is null, abort.");
                return this;
            } else if (TextUtils.isEmpty(token)) {
                LogUtil.d("token is null, abort.");
                return this;
            }
        }

        int aesKey = AccountManager.getInstance().getAESKey();
        String aesValue = AccountManager.getInstance().getAESValue();

        if (aesKey == 0 && mOperation != Operation.USER_LOGIN_V2) {
            LogUtil.d("request: " + mOperation + ", AES key == 0, abort. " + mRequest);
            EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(NetworkError.ERROR_AES_DECRYPT_FAILED));
            return this;
        }

        mBundle = new NBundle(mBundle).setOperation(mOperation).build();
        mRequestCallBack = new BaseRequestCallBack<T>(clazz, mContext, aesValue, mShowDialog) {
            @Override
            public void onResponse(boolean b, T t, Request request, Response response) {
                if (t == null) {
                    listener.onNullResponse(mBundle);
                } else if (1 == t.ResultCode) {
                    if (!(mBundle != null && mBundle.getBoolean(NetworkError.KEY_IGNORE_RESPONSE_LOG, false))) {
                        LogUtil.d("op = " + mOperation + ", response: " + t.toString());
                    }
                    if (t instanceof ISetTickListener) {
                        ((ISetTickListener) t).setTicks();
                    }
                    listener.onResponse(t, mBundle);
                } else {
                    NetworkError.dispatchError(t.ResultCode, t.ResultMsg, mBundle);
                    listener.onError(t.ResultCode, t, null, mBundle);
                }
            }

            @Override
            public void onError(boolean isFromCache, Call call, Response response, Exception e) {
                listener.onError(-1, null, e, mBundle);
            }
        };

        if (mRequest instanceof IGetTickListener) {
            ((IGetTickListener) mRequest).loadTicks();
        }
        if (!(mBundle != null && mBundle.getBoolean(NetworkError.KEY_IGNORE_RESPONSE_LOG, false))) {
            LogUtil.d("request: " + mOperation + ", " + mRequest);
        }

        byte[] bytes = StreamUtils.postBytesBuild(mOperation, mRequest, aesKey, aesValue);
        String url = null;
        if (mOperation == Operation.UPLOAD_IMAGE_FILE || mOperation == Operation.CHECK_FILE_IS_EXIT || mOperation == Operation.UPLOAD_VOICE_FILE || mOperation == Operation.DOWNDOAD_IMAGE_FILE
                || mOperation == Operation.DOWNDOAD_VOICE_FILE) {
            url = APIConstant.URL_FILE;
        } else {
            url = APIConstant.URL_HOST;
        }
        OkHttpUtils.post(url)
                .tag(mTag)
                .postBytes(bytes)
                .mediaType(MediaType.parse(TUTOR_MEDIA_TYPE))
                .cacheMode(CacheMode.NO_CACHE)
                .execute(mRequestCallBack);
        return this;
    }

    private void checkParams() {
        if (mOperation == -1) {
            throw new IllegalArgumentException("operation error!");
        } else if (mRequest == null) {
            throw new IllegalArgumentException("request must not be null!");
        }
    }

    public static class RequestListener<R extends CommonResponse> implements AbsRequestListener<R> {

        @Override
        public void onResponse(R response, Bundle bundle) {

        }

        @Override
        public void onNullResponse(Bundle bundle) {
            LogUtil.d("onNullResponse");
        }

        @Override
        public void onError(int errorCode, R response, Exception e, Bundle bundle) {
            StringBuilder ss = new StringBuilder("onError: ");
            ss.append(errorCode);
            ss.append(", operation = ");
            ss.append(new NBundle(bundle).getOperation());
            if (e != null) {
                ss.append(", ");
                ss.append(e.getLocalizedMessage());
                LogUtil.exception(e);
            }
            LogUtil.d(ss.toString());
        }
    }

    public interface AbsRequestListener<R extends CommonResponse> {
        void onResponse(R response, Bundle bundle);

        void onNullResponse(Bundle bundle);

        void onError(int errorCode, R response, Exception e, Bundle bundle);
    }
}
