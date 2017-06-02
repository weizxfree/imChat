package com.itutorgroup.tutorchat.phone.adapter.settings;

import android.content.Context;
import android.view.View;
import android.widget.CheckedTextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;

import java.util.List;

/**
 * Created by joyinzhao on 2016/12/28.
 */
public class AppLocaleSettingAdapter extends MyBaseAdapter<String> {

    public AppLocaleSettingAdapter(Context context, List<String> data) {
        super(context, data);
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_setting_language;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        bindData(position, convertView, holder);
        return convertView;
    }

    private void bindData(int position, View convertView, ViewHolder holder) {
        CheckedTextView tv = holder.getView(android.R.id.text1);
        tv.setText(getItem(position));
    }

}
