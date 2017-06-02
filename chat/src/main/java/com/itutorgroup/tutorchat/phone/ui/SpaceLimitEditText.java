package com.itutorgroup.tutorchat.phone.ui;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 限制输入空格的EditText
 * Created by Administrator on 2015/12/18
 */

public class SpaceLimitEditText extends EditText implements TextWatcher{

	public SpaceLimitEditText(Context context) {
		super(context);
		initView(context);
	}

	public SpaceLimitEditText(Context context,AttributeSet attrs) {
		super(context,attrs);
		initView(context);
	}

	public SpaceLimitEditText(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);
		initView(context);
	}
	
	private void initView(Context context) {
		this.addTextChangedListener(this);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		isEditNotBlank(this);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

	/**
	 * 
	 * @note:EditText 不能输入空格(TextWatcher.afterTextChanged())
	 * @param mEditText
	 */

	public void isEditNotBlank(EditText mEditText) {
		String string = mEditText.getText()+"";
		int index = mEditText.getSelectionStart();
		String startString = string.substring(0, index);
		String endString = string.substring(index);
		if (index > 0) {
			char charAt = startString.charAt(index - 1);
			if (charAt == ' ') {
				index = index - 1;
				mEditText.setText(getFoot(startString)+endString);
				mEditText.setSelection(index);
			}
		}
	}

	/** 去String 去尾 */
	public String getFoot(String s) {
		return s.substring(0, s.length() - 1);
	}

}
