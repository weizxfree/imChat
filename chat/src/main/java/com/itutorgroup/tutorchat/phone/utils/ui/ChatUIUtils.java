package com.itutorgroup.tutorchat.phone.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;

import java.util.List;

/**
 * Created by tom_zxzhang on 2016/9/5.
 */
public class ChatUIUtils {


    /*判断超过5分钟间隔显示时间*/
    public static void showTime(TextView timeTextView, MessageModel message, List<MessageModel> mList, int position){
        if(position == 0){
            timeTextView.setText(TimeUtils.formatTimeString(mList.get(position).CreateTime));
            timeTextView.setVisibility(View.VISIBLE);
        }else{
            if(TimeUtils.needShowTime(mList.get(position).CreateTime,mList.get(position-1).CreateTime)){
                timeTextView.setVisibility(View.VISIBLE);
                timeTextView.setText(TimeUtils.formatTimeString(mList.get(position).CreateTime));
            }else{
                timeTextView.setVisibility(View.GONE);
            }
        }
    }

    public static void setMaxWidth(TextView view,Context mContext){
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        view.setMaxWidth(screenWidth*2/3);
    }
}
