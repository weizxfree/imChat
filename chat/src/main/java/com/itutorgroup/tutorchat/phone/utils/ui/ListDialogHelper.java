package com.itutorgroup.tutorchat.phone.utils.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

import java.util.List;

/**
 * Created by joyinzhao on 2016/12/21.
 */
public class ListDialogHelper {
    private Context mContext;
    private List<String> mItems;
    private List<Runnable> mActionList;
    private AlertDialog mDialog;

    public ListDialogHelper(Context context, List<String> items, final List<Runnable> actionList) {
        mContext = context;
        mItems = items;
        mActionList = actionList;
    }

    public void show() {
        ListView listView = new ListView(mContext);
        listView.setDivider(new ColorDrawable(Color.parseColor("#e3e7f1")));
        listView.setDividerHeight(1);
        listView.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mItems));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionList != null && mActionList.size() > position && mActionList.get(position) != null) {
                    mActionList.get(position).run();
                }
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
        mDialog = builder.create();
        mDialog.show();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = ScreenUtil.getScreenWidth(mContext) - PixelUtil.dp2px(50);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mDialog.getWindow().setAttributes(lp);
    }

}
