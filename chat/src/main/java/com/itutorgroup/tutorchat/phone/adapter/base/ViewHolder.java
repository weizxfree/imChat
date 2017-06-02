package com.itutorgroup.tutorchat.phone.adapter.base;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class ViewHolder {
    private SparseArray<View> mViews = new SparseArray<>();
    private View mConvertView;

    public ViewHolder(View view) {
        mConvertView = view;
    }

    public <T extends View> T getView(int resId) {
        View v = mViews.get(resId);
        if (null == v) {
            v = mConvertView.findViewById(resId);
            mViews.put(resId, v);
        }
        return (T) v;
    }
}
