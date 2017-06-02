package com.itutorgroup.tutorchat.phone.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class ConfirmDialog extends BaseDialog {

    /**
     * 默认隐藏该元素，设置文本时才显示
     */
    private TextView mTvTitle;

    private TextView mTvMessage;
    private TextView mTvCancel;
    private TextView mTvConfirm;

    private DialogListenerWrapper mOnCancelListener;
    private DialogListenerWrapper mOnConfirmListener;

    public ConfirmDialog(Context context) {
        super(context);
    }

    @Override
    protected View getDefaultView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null);
        mTvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        mTvMessage = (TextView) view.findViewById(R.id.tv_dialog_message);
        mTvCancel = (TextView) view.findViewById(R.id.tv_dialog_cancel);
        mTvConfirm = (TextView) view.findViewById(R.id.tv_dialog_ok);

        mOnCancelListener = new DialogListenerWrapper();
        mOnConfirmListener = new DialogListenerWrapper();

        mTvCancel.setOnClickListener(mOnCancelListener);
        mTvConfirm.setOnClickListener(mOnConfirmListener);
        return view;
    }

    /**
    /**
     * 设置对话框标题
     * 并自动显示标题（默认隐藏）
     */
    public ConfirmDialog title(String title) {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setText(title);
        return this;
    }

    /**
     * 设置对话框消息
     */
    public ConfirmDialog message(String message) {
        mTvMessage.setText(message);
        return this;
    }

    /**
     * 设置取消按钮文字
     * （默认‘取消’）
     */
    public ConfirmDialog cancelText(String cancel) {
        mTvCancel.setText(cancel);
        return this;
    }

    /**
     * 设置确认按钮文字
     * （默认‘好的’）
     */
    public ConfirmDialog confirmText(String confirm) {
        mTvConfirm.setText(confirm);
        return this;
    }

    /**
     * 点击‘好的’回调
     */
    public ConfirmDialog confirm(View.OnClickListener listener) {
        mOnConfirmListener.setOnClickListener(listener);
        return this;
    }

    /**
     * 点击‘取消’回调
     */
    public ConfirmDialog cancel(View.OnClickListener listener) {
        mOnCancelListener.setOnClickListener(listener);
        return this;
    }
}