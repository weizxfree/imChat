package com.itutorgroup.tutorchat.phone.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by dss886 on 15/9/26.
 */
public class EmotionInputDetector {

    private static final String SHARE_PREFERENCE_NAME = "com.dss886.emotioninputdetector";
    private static final String SHARE_PREFERENCE_TAG = "soft_input_height";

    private Activity mActivity;
    private InputMethodManager mInputManager;
    private SharedPreferences sp;
    private View mEmotionLayout;
    private EditText mEditText;
    private View mContentView;
    private RadioButton mEmotionButton;
    private View mOthersView;
    private Button mOthersButton;
    private CheckBox mVoiceButton;
    private boolean isHavePicAndVoiceRight = true;
    private boolean isNeedShowSoftKeyboard;

    private EmotionInputDetector() {
    }

    public static EmotionInputDetector with(Activity activity) {
        EmotionInputDetector emotionInputDetector = new EmotionInputDetector();
        emotionInputDetector.mActivity = activity;
        emotionInputDetector.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        emotionInputDetector.sp = activity.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return emotionInputDetector;
    }

    public EmotionInputDetector bindToContent(View contentView) {
        mContentView = contentView;

        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hideSoftInput();
                        mContentView.requestFocus();
                        hideEmotionLayout(false);
                        hideOthersLayout(false);
                }
                return false;
            }
        });

        return this;
    }

    public EmotionInputDetector bindToEditText(EditText editText) {
        mEditText = editText;
        mEditText.requestFocus();
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && (mEmotionLayout.isShown() || mOthersView.isShown())) {
                    lockContentHeight();
                    hideOthersLayout(true);
                    hideEmotionLayout(true);
                    mEditText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            unlockContentHeightDelayed();
                        }
                    }, 200L);
                }
                return false;
            }
        });

        return this;
    }

    public EmotionInputDetector bindToEmotionButton(View emotionButton) {
        mEmotionButton = (RadioButton) emotionButton;
        emotionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmotionLayout.isShown()) {
                    lockContentHeight();
                    hideEmotionLayout(true);
                    unlockContentHeightDelayed();
                } else {
                    if (isSoftInputShown()) {
                        lockContentHeight();
                        showEmotionLayout();
                        unlockContentHeightDelayed();
                    } else {
                        showEmotionLayout();
                    }
                    mEditText.requestFocus();

                }
            }
        });
        return this;
    }

    public EmotionInputDetector bindToOthersButton(View othersButton, final FaceRelativeLayout.onClickSendListener listener) {
        mOthersButton = (Button) othersButton;
        mOthersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((int) v.getTag() == R.drawable.send) {
                    if (listener != null) {
                        listener.onClickSend();
                    }
                } else {
                    if (mOthersView.isShown()) {
                        lockContentHeight();
                        hideOthersLayout(true);
                        unlockContentHeightDelayed();
                    } else {
                        isNeedShowSoftKeyboard = true;
                        mTextLayout.setVisibility(View.VISIBLE);
                        mVoiceButton.setChecked(false);
                        if (isSoftInputShown()) {
                            lockContentHeight();
                            showOtherLayout();
                            unlockContentHeightDelayed();
                        } else {
                            showOtherLayout();
                        }

                    }

                }


            }
        });
        return this;
    }


    public EmotionInputDetector setEmotionView(View emotionView) {
        mEmotionLayout = emotionView;
        return this;
    }

    public EmotionInputDetector setAddOthersView(View othersView) {
        mOthersView = othersView;
        return this;
    }


    public EmotionInputDetector build() {
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        hideSoftInput();
        return this;
    }

    public boolean interceptBackPress() {

        if (mEmotionLayout.isShown()) {
            hideEmotionLayout(false);
            return true;
        }

        return false;
    }

    private void showEmotionLayout() {
        int softInputHeight = getSupportSoftInputHeight();
        if (softInputHeight == 0) {
            softInputHeight = sp.getInt(SHARE_PREFERENCE_TAG, PixelUtil.dp2px(300));
        }
        hideSoftInput();
        mEmotionLayout.getLayoutParams().height = softInputHeight;
        mOthersView.setVisibility(View.GONE);
        mEmotionLayout.setVisibility(View.VISIBLE);
    }


    public void hideEmotionLayout(boolean showSoftInput) {
        if (mEmotionLayout.isShown()) {
            mEmotionButton.setChecked(false);
            mEmotionLayout.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }
        }

    }

    private void showOtherLayout() {
        int softInputHeight = getSupportSoftInputHeight();
        if (softInputHeight == 0) {
            softInputHeight = sp.getInt(SHARE_PREFERENCE_TAG, PixelUtil.dp2px(300));
        }
        hideSoftInput();
        mOthersView.getLayoutParams().height = softInputHeight;
        mEmotionLayout.setVisibility(View.GONE);
        mOthersView.setVisibility(View.VISIBLE);
    }

    public void hideOthersLayout(boolean showSoftInput) {
        if (mOthersView.isShown()) {
            mOthersView.setVisibility(View.GONE);
            if (showSoftInput) {
                showSoftInput();
            }
        }

    }

    private View mTextLayout;

    public EmotionInputDetector bindToVoiceButton(CheckBox btn_voice, final View text_face_layout, final View btnRecordLayout, final boolean isHavePicAndVoiceRight) {
        mVoiceButton = btn_voice;
        mTextLayout = text_face_layout;
        this.isHavePicAndVoiceRight = isHavePicAndVoiceRight;
        mVoiceButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mOthersView.isShown()) {
                    hideOthersLayout(false);
                    showRecordLayout(btnRecordLayout, text_face_layout, mOthersButton);
                } else if (mEmotionLayout.isShown()) {
                    hideEmotionLayout(false);
                    showRecordLayout(btnRecordLayout, text_face_layout, mOthersButton);
                } else if (isSoftInputShown()) {
                    hideSoftInput();
                    showRecordLayout(btnRecordLayout, text_face_layout, mOthersButton);
                } else {
                    if (isChecked) {
                        showRecordLayout(btnRecordLayout, text_face_layout, mOthersButton);
                    } else {
                        mOthersButton.setVisibility(View.VISIBLE);
                        if (StringUtils.isEmpty(mEditText.getText().toString()) && isHavePicAndVoiceRight) {
                            mOthersButton.setTag(R.drawable.more_function);
                            mOthersButton.setBackgroundResource(R.drawable.more_function);
                        } else {
                            mOthersButton.setTag(R.drawable.send);
                            mOthersButton.setBackgroundResource(R.drawable.send);
                        }
                        text_face_layout.setVisibility(View.VISIBLE);
                        btnRecordLayout.setVisibility(View.GONE);
                        if (!isNeedShowSoftKeyboard) {
                            showSoftInput();
                        }
                        isNeedShowSoftKeyboard = false;
                    }
                }

            }

        });
        return this;

    }


    private void showRecordLayout(View mRcordView, View mFaceView, Button mButton) {


        if (!isHavePicAndVoiceRight) {
            mOthersButton.setVisibility(View.GONE);
        } else {
            mOthersButton.setVisibility(View.VISIBLE);
            mButton.setTag(R.drawable.more_function);
            mButton.setBackgroundResource(R.drawable.more_function);
        }
        mFaceView.setVisibility(View.GONE);
        mRcordView.setVisibility(View.VISIBLE);
    }


    private void lockContentHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        params.height = mContentView.getHeight();
        params.weight = 0.0F;
    }

    private void unlockContentHeightDelayed() {
        mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
            }
        }, 200L);
    }

    private void showSoftInput() {
        mEditText.requestFocus();
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                mInputManager.showSoftInput(mEditText, 0);
            }
        });
    }

    private void hideSoftInput() {
        if (isSoftInputShown()) {
            mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int screenHeight = mActivity.getWindow().getDecorView().getRootView().getHeight();
        int softInputHeight = screenHeight - r.bottom;
        if (Build.VERSION.SDK_INT >= 20) {
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        }
        if (softInputHeight < 0) {
            Log.w("EmotionInputDetector", "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            sp.edit().putInt(SHARE_PREFERENCE_TAG, softInputHeight).apply();
        }
        return softInputHeight;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

}
