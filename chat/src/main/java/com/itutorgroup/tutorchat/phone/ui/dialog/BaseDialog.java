package com.itutorgroup.tutorchat.phone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public abstract class BaseDialog {
    private Dialog mDialog;

    public BaseDialog(Context context) {
        View view = getDefaultView(context);
        mDialog = createDialog(context, view);
    }

    public Dialog dialog() {
        return mDialog;
    }

    /**
     * 子类重写该方法，即可创建样式相同的对话框。
     *
     * @param context
     * @return
     */
    protected abstract View getDefaultView(Context context);

    private static Dialog createDialog(Context context, View v) {
        Dialog dialog = new Dialog(context, R.style.CommonDialog);
//        dialog.setCancelable(false);
        dialog.setContentView(v);
        return dialog;
    }

    public BaseDialog force() {
        mDialog.setCancelable(false);
        return this;
    }

    public void show() {
        if (mDialog != null) {
            try {
                mDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        return false;
    }

    public BaseDialog onCancel(DialogInterface.OnCancelListener listener) {
        if (mDialog != null) {
            mDialog.setOnCancelListener(listener);
        }
        return this;
    }

    /**
     * 将OnClickListener再用一层Listener包裹起来
     * 用于点击对话框上元素时自动取消对话框
     */
    public class DialogListenerWrapper implements View.OnClickListener {

        View.OnClickListener listener;

        /**
         * 更新DialogListenerWrapper中的Listener
         * 点击后回调
         */
        public void setOnClickListener(View.OnClickListener listener) {
            this.listener = listener;
        }

        /**
         * 自动关闭Dialog，然后再调用设置的listener
         */
        @Override
        public void onClick(View v) {
            dismiss();
            if (listener != null) {
                listener.onClick(v);
            }
        }
    }
}
