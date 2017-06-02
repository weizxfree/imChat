package com.itutorgroup.tutorchat.phone.adapter.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.itutorgroup.tutorchat.phone.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> mData;

    public MyBaseAdapter(Context context, List<T> data) {
        mContext = context;
        mData = data == null ? new ArrayList<T>() : data;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        if (mData == null || position >= mData.size()) {
            return null;
        } else {
            return mData.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 该方法得到每一项的布局文件id
     *
     * @param position 列表中第几项
     * @return 该项布局文件id
     */
    public abstract int getItemResource(int position);

    /**
     * 代替getView，需子类实现，如设置监听等
     *
     * @param position
     * @param convertView
     * @param holder
     * @return
     */
    public abstract View getItemView(int position, View convertView, ViewHolder holder);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, getItemResource(position), null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.tag_holder);
        }
        convertView.setTag(R.id.tag_position, position);
        convertView.setTag(R.id.tag_holder, holder);
        convertView.setTag(R.id.tag_bean, mData.get(position));
        return getItemView(position, convertView, holder);
    }

    // change data -- start --
    public void addAll(List<T> elem) {
        mData.addAll(elem);
        notifyDataSetChanged();
    }

    public void addAllAtFirst(int location, List<T> elem) {
        mData.addAll(location, elem);
        notifyDataSetChanged();
    }

    public void remove(T elem) {
        mData.remove(elem);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        mData.remove(index);
        notifyDataSetChanged();
    }

    public void replaceAll(List<T> elem) {
        mData.clear();
        if (elem != null) {
            mData.addAll(elem);
        }
        notifyDataSetChanged();
    }
    // change data -- end --
}
