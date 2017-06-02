package com.itutorgroup.tutorchat.phone.ui.photo.picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.model.PhotoDirectory;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.utils.PhotoUtils;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;

import java.util.List;

public class FolderAdapter extends BaseAdapter {

    List<PhotoDirectory> mData;
    Context mContext;

    public FolderAdapter(Context context, List<PhotoDirectory> mData) {
        this.mData = mData;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_picker_folder, null);
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(73));
            convertView.setLayoutParams(lp);
            holder.photoIV = (ImageView) convertView.findViewById(R.id.imageview_floder_img);
            holder.folderNameTV = (TextView) convertView.findViewById(R.id.textview_floder_name);
            holder.photoNumTV = (TextView) convertView.findViewById(R.id.textview_photo_num);
            holder.selectIV = (ImageView) convertView.findViewById(R.id.imageview_floder_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.selectIV.setVisibility(View.GONE);
        holder.photoIV.setImageResource(R.drawable.picker_ic_photo_loading);
        PhotoDirectory folder = mData.get(position);
        if (folder.isSelected()) {
            holder.selectIV.setVisibility(View.VISIBLE);
        }
        holder.folderNameTV.setText(folder.getName());
        holder.photoNumTV.setText(mContext.getString(R.string.picker_photos_num, folder.getPhotos().size()));
        if (folder.getPhotos().size() != 0) {
            Glide.with(mContext).load(folder.getPhotos().get(0).getPath()).dontAnimate()
                    .thumbnail(0.1f).into(holder.photoIV);
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoIV;
        private TextView folderNameTV;
        private TextView photoNumTV;
        private ImageView selectIV;
    }

}
