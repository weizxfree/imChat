package com.itutorgroup.tutorchat.phone.ui.popup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;

/**
 * Created by joyinzhao on 2016/8/29.
 */

public class SelectPicPopupWindow extends PopupWindow {


    private TextView mTvTakePic, mTvPickPhoto, mTvCancel;
    private View mMenuView;

    public SelectPicPopupWindow(Activity context, final OnClickListener itemListener) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.dialog_select_avatar, null);
        mTvTakePic = (TextView) mMenuView.findViewById(R.id.tv_take_photo);
        mTvPickPhoto = (TextView) mMenuView.findViewById(R.id.tv_pick_photo);
        mTvCancel = (TextView) mMenuView.findViewById(R.id.tv_cancel);
        //取消按钮
        mTvCancel.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (itemListener != null) {
                    itemListener.onClick(v);
                }
            }
        };
        mTvPickPhoto.setOnClickListener(listener);
        mTvTakePic.setOnClickListener(listener);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}