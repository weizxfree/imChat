package com.itutorgroup.tutorchat.phone.ui.popup;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;

public class MessagePopWindow extends PopupWindow {

    private Activity mActivity;
    private TextView mTvSearchContacts;
    public TextView mTvCreateGroup;

    public MessagePopWindow(final Activity activity) {
        mActivity = activity;
        View view = activity.getLayoutInflater().inflate(R.layout.pop_menu_new_message, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setAnimationStyle(R.style.pop_window_anim_style);
        setOutsideTouchable(false);
        //设置SelectPicPopupWindow弹出窗体的背景
//        setBackgroundDrawable(new BitmapDrawable());
        mTvCreateGroup = (TextView) view.findViewById(R.id.text);
//        mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent(activity, GroupEditActivity.class);
////                intent.putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_CREATE_GROUP);
////                activity.startActivity(intent);
//                dismiss();
//            }
//        });

    }

    public void setText(int count){
            mTvCreateGroup.setText(LPApp.getInstance().getString(R.string.msg_new_come_with_number,count+""));
    }










}
