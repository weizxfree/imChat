package com.itutorgroup.tutorchat.phone.utils.network;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class NBundle {
    private Bundle mBundle;

    public Bundle build() {
        return mBundle;
    }

    public NBundle() {
        mBundle = new Bundle();
    }

    public NBundle(Bundle bundle) {
        mBundle = bundle;
        if (mBundle == null) {
            mBundle = new Bundle();
        }
    }

    public NBundle setOperation(int operation) {
        mBundle.putInt(NetworkError.KEY_REQUEST_OPERATION, operation);
        return this;
    }

    public int getOperation() {
        int operation = mBundle.getInt(NetworkError.KEY_REQUEST_OPERATION);
        return operation;
    }

    /**
     * 忽略所有errorCode
     */
    public NBundle ignoreAllError() {
        mBundle.putBoolean(NetworkError.KEY_IGNORE_ALL_ERROR, true);
        return this;
    }

    public NBundle ignoreResponseLog() {
        mBundle.putBoolean(NetworkError.KEY_IGNORE_RESPONSE_LOG , true);
        return this;
    }

    /**
     * 忽略errorCode
     *
     * @param codes 需忽略的errorCode列表
     */
    public NBundle addIgnoreToastErrorCode(Integer... codes) {
        if (codes == null || codes.length < 1) {
            return this;
        }
        ArrayList<Integer> ignoreList = mBundle.getIntegerArrayList(NetworkError.KEY_IGNORE_ERROR_CODE);
        if (ignoreList == null) {
            ignoreList = new ArrayList<>();
        }
        for (Integer code : codes) {
            ignoreList.add(code);
        }
        mBundle.putIntegerArrayList(NetworkError.KEY_IGNORE_ERROR_CODE, ignoreList);
        return this;
    }
}
