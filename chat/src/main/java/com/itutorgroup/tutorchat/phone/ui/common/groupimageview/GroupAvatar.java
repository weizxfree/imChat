package com.itutorgroup.tutorchat.phone.ui.common.groupimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.BitmapUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/27.
 */
public class GroupAvatar extends ViewGroup {

    private List<ImageView> mImageViewList = new ArrayList<>();
    private List<String> mImgDataList;
    private int mGap;

    private int mWidth;

    private Paint mPaint;

    private GroupAvatarAdapter<String> mAdapter = new GroupAvatarAdapter<String>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, String s) {
            imageView.setTag(R.id.tag_default, R.drawable.head_personal_square);
            UserInfoHelper.showAvatar(s, imageView);
        }

        @Override
        protected ImageView generateImageView(Context context) {
            return super.generateImageView(context);
        }
    };

    public GroupAvatar(Context context) {
        this(context, null);
    }

    public GroupAvatar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGap = 1;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * 设定宽高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = measureWidth(widthMeasureSpec);
        int parentHeight = measureHeight(heightMeasureSpec);

        mWidth = Math.min(parentWidth, parentHeight);
        setMeasuredDimension(mWidth, mWidth);
    }

    /**
     * 对宫格的宽高进行重新定义
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 200;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildrenView(l, t, r, b);
    }

    public void setGroupId(final String groupId) {
//        int maxSize = getResources().getInteger(R.integer.max_count_group_avatar_item);
        GroupManager.getInstance().getUserInfoListById(groupId, 20, new CommonLoadingListener<List<UserInfo>>() {
            @Override
            public void onResponse(List<UserInfo> userInfoList) {
                if (userInfoList != null && userInfoList.size() > 0) {
                    Observable.just(userInfoList)
                            .subscribeOn(Schedulers.io())
                            .map(new Func1<List<UserInfo>, List>() {
                                @Override
                                public List call(List<UserInfo> userInfoList) {
                                    ArrayList<String> list = new ArrayList<>();
                                    for (UserInfo info : userInfoList) {
                                        list.add(info.Image);
                                    }
                                    return list;
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<List>() {
                                @Override
                                public void call(List list) {
                                    Object tag = getTag(R.id.tag_bean);
                                    if (tag != null && groupId.equals(tag)) {
                                        setImagesData(list);
                                    }
                                }
                            }, CommonUtil.ACTION_EXCEPTION);
                }
            }
        });
    }

    private void setImagesData(List list) {
        if (list == null || list.isEmpty()) {
            this.setVisibility(GONE);
            return;
        } else {
            this.setVisibility(VISIBLE);
        }
        int maxSize = getResources().getInteger(R.integer.max_count_group_avatar_item);
        if (maxSize > 0 && list.size() > maxSize) {
            list = list.subList(0, maxSize);
        }
        if (list.equals(mImgDataList)) {
            return;
        }
        if (mImgDataList == null) {
            int i = 0;
            while (i < list.size()) {
                ImageView iv = getImageView(i);
                if (iv == null) {
                    return;
                }
                addView(iv, generateDefaultLayoutParams());
                i++;
            }
        } else {
            int oldViewCount = mImgDataList.size();
            int newViewCount = list.size();
            if (oldViewCount > newViewCount) {
                removeViews(newViewCount, oldViewCount - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = oldViewCount; i < newViewCount; i++) {
                    ImageView iv = getImageView(i);
                    if (iv == null) {
                        return;
                    }
                    addView(iv, generateDefaultLayoutParams());
                }
            }
        }
        mImgDataList = list;
        requestLayout();
    }

    /**
     * 获得 ImageView
     * 保证了 ImageView的重用
     *
     * @param position 位置
     */
    private ImageView getImageView(final int position) {
        if (position < mImageViewList.size()) {
            return mImageViewList.get(position);
        } else {
            if (mAdapter != null) {
                ImageView imageView = mAdapter.generateImageView(getContext());
                mImageViewList.add(imageView);
                return imageView;
            } else {
                return null;
            }
        }
    }

    private void layoutChildrenView(int l, int t, int r, int b) {
        if (mImgDataList == null) {
            return;
        }
        int count = mImgDataList.size();
        if (count == 0) {
            return;
        }
        int width = r - l;
        int height = b - t;
        for (int i = 0; i < count; i++) {
            ImageView iv = (ImageView) getChildAt(i);
            if (mAdapter != null) {
                mAdapter.onDisplayImage(getContext(), iv, mImgDataList.get(i));
            }
            if (count == 3) {
                int w = (width - mGap) / 2;
                if (i == 0) {
                    iv.layout(l, t, l + w, b);
                } else {
                    int h = (height - mGap) / 2;
                    int top = (h + mGap) * (i - 1);
                    iv.layout(l + w + mGap, top, r, top + h);
                }
            } else if (count == 4) {
                int w = (width - mGap) / 2;
                int h = (height - mGap) / 2;
                int left = (w + mGap) * (i % 2);
                int top = (h + mGap) * (int) Math.floor(i / 2);
                int right = left + w;
                int bottom = top + h;
                iv.layout(left, top, right, bottom);
            } else if (count == 5) {
                if (i < 3) {
                    int w = (width - mGap * 2) / 3;
                    int left = (w + mGap) * i;
                    int right = left + w;
                    iv.layout(left, 0, right, (height - mGap) / 2);
                } else {
                    int w = (width - mGap) / 2;
                    int left = (w + mGap) * (i - 3);
                    int right = left + w;
                    iv.layout(left, (height + mGap) / 2, right, b);
                }
            } else if (count == 6) {
                int w = (width - mGap * 2) / 3;
                int h = (height - mGap) / 2;
                int left = (w + mGap) * (i % 3);
                int right = left + w;
                int top = (h + mGap) * (int) Math.floor(i / 3);
                int bottom = i < 3 ? h : b;
                iv.layout(left, top, right, bottom);
            } else if (count == 7) {
                int w = (width - mGap * 2) / 3;
                int h = (height - mGap * 2) / 3;
                if (i == 0) {
                    iv.layout(w + mGap, 0, 2 * w + mGap, h);
                } else {
                    int left = (w + mGap) * ((i - 1) % 3);
                    int right = left + w;
                    int top = (h + mGap) * (int) Math.floor((i + 2) / 3);
                    int bottom = top + h;
                    iv.layout(left, top, right, bottom);
                }
            } else if (count == 8) {
                int w = (width - mGap * 2) / 3;
                int h = (height - mGap * 2) / 3;
                if (i < 2) {
                    int left = (width - mGap) / 2 - w + (w + mGap) * i;
                    iv.layout(left, 0, left + w, h);
                } else {
                    int left = (w + mGap) * ((i - 2) % 3);
                    int right = left + w;
                    int top = (h + mGap) * (int) Math.floor((i + 1) / 3);
                    int bottom = top + h;
                    iv.layout(left, top, right, bottom);
                }
            } else if (count == 9) {
                int w = (width - mGap * 2) / 3;
                int h = (height - mGap * 2) / 3;
                int left = (w + mGap) * (i % 3);
                int right = left + w;
                int top = (h + mGap) * (int) Math.floor(i / 3);
                int bottom = top + h;
                iv.layout(left, top, right, bottom);
            } else {
                int w = (width - mGap * (count - 1)) / count;
                int left = (w + mGap) * i;
                iv.layout(left, t, left + w, b);
            }
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mWidth, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawColor(getResources().getColor(R.color.bg_dialog_weak_divider_line));
        super.dispatchDraw(c);
        bitmap = BitmapUtil.toRoundBitmap(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
    }
}
