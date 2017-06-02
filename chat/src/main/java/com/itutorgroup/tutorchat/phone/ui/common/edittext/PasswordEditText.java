package com.itutorgroup.tutorchat.phone.ui.common.edittext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class PasswordEditText extends EditText {

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public PasswordEditText(Context context) {
        super(context);
    }

    private Drawable mDrawable;

    private void init(AttributeSet attrs) {
        initByAttributeSet(attrs);
        mDrawable = getCompoundDrawables()[2];
        mDrawable = getResources().getDrawable(R.drawable.eye_off);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());

        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], mDrawable, getCompoundDrawables()[3]);
        setLongClickable(false);
    }

    private void initByAttributeSet(AttributeSet attrs) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth()
                        - getPaddingRight() - mDrawable
                        .getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    doTouchLogic();
                    InputMethodUtil.hideSoftKeyBoard(getContext(), this);
                    requestFocus();
                    return true;
                } else {
                    requestFocus();
                    InputMethodUtil.showSoftKeyBoard(getContext(), this);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    int drawableId = R.drawable.eye_off;

    private void doTouchLogic() {
//        if (getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
        if (drawableId == R.drawable.eye_off) {
//            setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            drawableId = R.drawable.eye_on;
        } else {
//            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            setTransformationMethod(PasswordTransformationMethod.getInstance());
            drawableId = R.drawable.eye_off;
        }
        mDrawable = getResources().getDrawable(drawableId);
        mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());

        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], mDrawable, getCompoundDrawables()[3]);

        CharSequence text = getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());// 将光标移动到最后
        }
    }
}