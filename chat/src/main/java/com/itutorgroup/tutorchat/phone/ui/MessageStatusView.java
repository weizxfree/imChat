package com.itutorgroup.tutorchat.phone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageStatus;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

/**
 * Created by tom_zxzhang on 2016/10/24.
 */
public class MessageStatusView extends LinearLayout {


    private TextView mTextView;
    private ProgressBar mProgressBar;

    public MessageStatusView(Context context) {
        super(context);
        initView(context);
    }

    public MessageStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context){
        View.inflate(context, R.layout.chatting_message_status_view, this);
        mTextView = (TextView) findViewById(R.id.mTextView);
        mProgressBar = (ProgressBar)findViewById(R.id.mProgressBar);
    }


    public void setViewByMessageStatus(int messageStatus){

        switch (messageStatus){

            case MessageStatus.MESSAGE_SNEDING:
                mProgressBar.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
                break;

            case MessageStatus.MESSAGE_SNED_ERROE:
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("");
                mTextView.setBackgroundResource(R.drawable.chat_send_resend);
                break;

            case MessageStatus.MESSAGE_SEND_OK:
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("");
                mTextView.setBackground(null);
                mProgressBar.setVisibility(View.GONE);
                break;
        }


    }

    public void setmTextView(String text){
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setBackground(null);
        mTextView.setText(text);
        mProgressBar.setVisibility(View.GONE);
    }



    public void setBackgroundRed(){
        mProgressBar.setVisibility(View.GONE);
        mTextView.getLayoutParams().width = PixelUtil.dp2px(10);
        mTextView.getLayoutParams().height = PixelUtil.dp2px(10);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText("");
        mTextView.setBackgroundResource(R.drawable.ic_point_bg);
    }

    public void setBackgroudNull(){
        mTextView.setVisibility(View.VISIBLE);
        mTextView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mTextView.setText("");
        mTextView.setBackground(null);
        mProgressBar.setVisibility(View.GONE);
    }






    public String getmTextView(){
        return  mTextView.getText().toString();
    }







}
