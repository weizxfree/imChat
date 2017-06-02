package com.itutorgroup.tutorchat.phone.adapter.image;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.ui.photo.view.PhotoView;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.ui.ListDialogHelper;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/10/25.
 */
public class ViewPagerGalleryAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> mList;
    private boolean mCanSave;

    public ViewPagerGalleryAdapter(Context context, ArrayList<String> list, boolean canSave) {
        mList = list;
        mContext = context;
        mCanSave = canSave;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView view = new PhotoView(mContext);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (mCanSave) {
            view.setTag(R.id.tag_bean, mList.get(position));
            view.setOnLongClickListener(mOnLongClickListener);
        }
        Glide.with(mContext).load(mList.get(position)).into(view);
        container.addView(view);
        return view;
    }

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            List<String> items = new ArrayList<>();
            final List<Runnable> actionList = new ArrayList<>();
            items.add(mContext.getString(R.string.msg_operation_pic_save));
            actionList.add(new Runnable() {
                @Override
                public void run() {
                    final String path = "" + v.getTag(R.id.tag_bean);
                    String newName = System.currentTimeMillis() + ".jpg";
                    FileUtils.copyFile(path, FileUtils.getSavePath() + newName);
                    FileUtils.saveImageToGallery(mContext, FileUtils.getSavePath(), newName);
                    ToastUtil.show(mContext.getString(R.string.common_successful_file_copy, FileUtils.getSavePath()));
                }
            });

            ListDialogHelper helper = new ListDialogHelper(mContext, items, actionList);
            helper.show();
            return true;
        }
    };
}