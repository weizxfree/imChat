package com.itutorgroup.tutorchat.phone.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class MessageDialog extends BaseDialog {

    private TextView mTvTitle;
    private TextView mTvMessage;
    private TextView mTvDone;

    private DialogListenerWrapper mOnCancelListener;

    public MessageDialog(Context context) {
        super(context);
    }

    /**
     * 设置对话框标题
     * 并自动显示标题（默认隐藏）
     */
    public MessageDialog title(String title) {
        mTvTitle.setVisibility(View.VISIBLE);
        mTvTitle.setText(title);
        return this;
    }

    /**
     * 设置对话框消息
     */
    public MessageDialog message(String message) {
        mTvMessage.setText(message);
        return this;
    }

    /**
     * 设置下方点击按钮文字
     * （默认‘我知道了’）
     */
    public MessageDialog closeText(String tip) {
        mTvDone.setText(tip);
        return this;
    }

    @Override
    protected View getDefaultView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        mTvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        mTvMessage = (TextView) view.findViewById(R.id.tv_dialog_message);
        mTvDone = (TextView) view.findViewById(R.id.tv_dialog_ok);

        mOnCancelListener = new DialogListenerWrapper();
        mTvDone.setOnClickListener(mOnCancelListener);
        return view;
    }

    /**
     * 点击‘我知道了’回调
     */
    public MessageDialog confirm(View.OnClickListener listener) {
        mOnCancelListener.setOnClickListener(listener);
        return this;
    }
}