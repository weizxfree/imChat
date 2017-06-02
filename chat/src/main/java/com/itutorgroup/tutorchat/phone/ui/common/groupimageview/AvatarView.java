package com.itutorgroup.tutorchat.phone.ui.common.groupimageview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.ui.CircleImageView;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import cn.salesuite.saf.inject.Injector;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/10/24.
 */
public class AvatarView extends FrameLayout {

    public static final int TYPE_USER = 0x11;
    public static final int TYPE_GROUP = 0x12;

    public int mType = TYPE_USER;

    @InjectView(id = R.id.imv_user_header)
    public CircleImageView mUserView;

    @InjectView(id = R.id.img_group_header)
    GroupAvatar mGroupView;

    public AvatarView(Context context) {
        super(context);
        init(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_avatar, null);
        addView(view);
        Injector.injectInto(this, this);
    }

    public void setType(int type) {
        mType = type;
        mUserView.setVisibility(type == TYPE_USER ? VISIBLE : GONE);
        mGroupView.setVisibility(type == TYPE_GROUP ? VISIBLE : GONE);
    }

    public void setUserHead(String imagePath, int defResId) {
        setType(TYPE_USER);
        mUserView.setTag(R.id.tag_default, defResId);
        mUserView.setTag(R.id.tag_bean, imagePath);
        UserInfoHelper.showAvatar(imagePath, mUserView);
    }

    public void setUserHead(String imagePath) {
        setUserHead(imagePath, R.drawable.head_personal_blue);
    }

    public void setGroupId(String groupId) {
        setType(TYPE_GROUP);
        mGroupView.setTag(R.id.tag_bean, groupId);
        mGroupView.setGroupId(groupId);
    }

    public void showDefault() {
        mUserView.setVisibility(VISIBLE);
        mUserView.setImageResource(R.drawable.head_personal_blue);
        mGroupView.setVisibility(GONE);
    }
}
