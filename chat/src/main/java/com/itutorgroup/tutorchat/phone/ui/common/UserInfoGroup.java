package com.itutorgroup.tutorchat.phone.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.BuildConfig;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.image.ImagePreviewActivity;
import com.itutorgroup.tutorchat.phone.activity.settings.AppDetailActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.inject.annotation.OnClick;

/**
 * Created by joyinzhao on 2016/9/29.
 */
public class UserInfoGroup extends RelativeLayout {

    private Context mContext;
    private View mContentView;
    private UserInfo mUserInfo;

    @InjectView(id = R.id.imv_header)
    CircleImageView mImvHeader;

    @InjectView(id = R.id.imv_bg_user_avatar)
    ImageView mImvBgAvatar;

    @InjectView(id = R.id.tv_english_name)
    TextView mTvEnglishName;

    @InjectView(id = R.id.tv_name)
    TextView mTvName;

    @InjectView(id = R.id.tv_english_titles)
    TextView mTvEnglishTitle;

    @InjectView(id = R.id.tv_professional_titles)
    TextView mTvProfessionalTitles;

    @InjectView(id = R.id.tv_department)
    TextView mTvDepartment;

    @InjectView(id = R.id.tv_mail)
    TextView mTvMail;

    @InjectView(id = R.id.tv_extension)
    TextView mTvExtension;

    private GestureDetectorCompat mGestureDetectorCompat;
    private String mImageUri;

//    @InjectView(id = R.id.tv_address)
//    TextView mTvAddress;

    public UserInfoGroup(Context context) {
        super(context);
        init(context);
    }

    public UserInfoGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserInfoGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mContentView = LayoutInflater.from(context).inflate(R.layout.group_user_info, null);
        addView(mContentView);
        Injector.injectInto(this, this);
        initView();
    }

    private void initView() {
        mImvHeader.setTag(R.id.tag_default, R.drawable.head_personal_blue);
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), new MyGestureListener());
        mTvName.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
    }

    public void setUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        mUserInfo = userInfo;
        UserInfoHelper.showAvatar(userInfo, mImvHeader, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mImageUri = imageUri;
            }
        });
        mTvEnglishName.setCompoundDrawables(null, null, UserInfoHelper.getSexDrawable(userInfo), null);
        mTvEnglishName.setText(formatNullText(userInfo.Name));
        mTvEnglishTitle.setText(mContext.getString(R.string.chat_desc_area, formatNullText(userInfo.Area)));
        mTvName.setText(mContext.getString(R.string.chat_desc_name, formatNullText(userInfo.ChineseName)));
        mTvProfessionalTitles.setText(mContext.getString(R.string.chat_desc_professional_titles, formatNullText(userInfo.Title)));
        mTvDepartment.setText(mContext.getString(R.string.chat_desc_department, formatNullText(userInfo.Department)));
        mTvMail.setText(mContext.getString(R.string.chat_desc_mail, formatNullText(userInfo.CompanyEmail)));
        mTvExtension.setText(mContext.getString(R.string.chat_desc_extension, formatNullText(userInfo.Ext)));
//        mTvAddress.setText(mContext.getString(R.string.chat_desc_address, formatNullText(userInfo.Area)));
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    private String formatNullText(String text) {
        if (TextUtils.isEmpty(text)) {
            return mContext.getString(R.string.user_info_item_blank);
        } else {
            return text;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            if (mUserInfo != null && e.getAction() == MotionEvent.ACTION_DOWN && !TextUtils.equals(BuildConfig.BUILD_TYPE, "product")) {
                Intent intent = new Intent(getContext(), AppDetailActivity.class);
                intent.putExtra(AppDetailActivity.EXTRA_TYPE, AppDetailActivity.TYPE_USER_INFO);
                intent.putExtra(AppDetailActivity.EXTRA_USER, mUserInfo);
                getContext().startActivity(intent);
            }
            return true;
        }
    }

    @OnClick(id = R.id.imv_header)
    void onAvatarClick(View v) {
        if (!TextUtils.isEmpty(mImageUri)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(mImageUri);
            ImagePreviewActivity.startNewActivity((Activity) mContext, v, list, mImageUri);
        }
    }
}
