package com.itutorgroup.tutorchat.phone.ui;


import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;


public class MonitoringEditText extends EditText  {


    private final Context mContext;

    public MonitoringEditText(Context context) {
        super(context);
        mContext = context;
    }

    public MonitoringEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MonitoringEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        switch (id) {
            case android.R.id.paste:
                try {
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        String value = clipboard.getText().toString();
                        Editable edit = getEditableText();
                        int index = this.getSelectionStart();
                        edit.insert(index, FaceConversionUtil.getInstace().getExpressionString(getContext(), value, 60));
                    } else {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        String value = clipboard.getText().toString();
                        Editable edit = getEditableText();
                        int index = this.getSelectionStart();
                        edit.insert(index, FaceConversionUtil.getInstace().getExpressionString(getContext(), value, 60));
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return super.onTextContextMenuItem(id);
    }




}
