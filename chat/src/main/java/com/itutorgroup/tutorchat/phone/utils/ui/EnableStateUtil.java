package com.itutorgroup.tutorchat.phone.utils.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class EnableStateUtil {
    /**
     * 通过输入框的内容设置View的enable状态。
     * 仅当所有输入框都有内容时enable才为true
     *
     * @param view
     */
    public static void proxy(final View view, final EditText... editTexts) {
        proxy(view, null, editTexts);
    }

    public static void proxy(final View view, CheckEnableListener listener, final EditText... editTexts) {
        if (view == null || editTexts == null) {
            return;
        }
        MyTextWatcher textWatcher = new MyTextWatcher(view, editTexts, listener);
        for (EditText edt : editTexts) {
            edt.addTextChangedListener(textWatcher);
        }
        textWatcher.checkEnabled();
    }

    public static final class MyTextWatcher implements TextWatcher {

        private View mView;
        private EditText[] mEdts;
        private CheckEnableListener mListener;

        public MyTextWatcher(View view, EditText[] editTexts, CheckEnableListener listener) {
            mView = view;
            mEdts = editTexts;
            mListener = listener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkEnabled();
        }

        public void checkEnabled() {
            if (mView == null || mEdts == null) {
                return;
            }
            boolean flag = true;
            for (EditText edt : mEdts) {
                if (edt.getText().toString().length() == 0) {
                    flag = false;
                }
            }
            if (mListener != null && flag) {
                flag = mListener.checkEnabled();
            }
            mView.setEnabled(flag);
        }
    }

    public static interface CheckEnableListener {
        boolean checkEnabled();
    }

    public static void setViewVisible(View view, int visible) {
        if (view != null && view.getVisibility() != visible) {
            view.setVisibility(visible);
        }
    }
}