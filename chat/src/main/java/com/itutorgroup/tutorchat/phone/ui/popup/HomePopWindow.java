package com.itutorgroup.tutorchat.phone.ui.popup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.group.GroupEditActivity;
import com.itutorgroup.tutorchat.phone.activity.search.SearchActivity;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;

/**
 * Created by joyinzhao on 2016/9/8.
 */
public class HomePopWindow extends PopupWindow {

    private Activity mActivity;
    private TextView mTvSearchContacts;
    private TextView mTvCreateGroup;

    public HomePopWindow(final Activity activity) {
        mActivity = activity;
        View view = activity.getLayoutInflater().inflate(R.layout.pop_menu_contacts_list, null);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setAnimationStyle(R.style.pop_window_anim_style);
        setOutsideTouchable(true);
        //设置SelectPicPopupWindow弹出窗体的背景
        setBackgroundDrawable(new BitmapDrawable());

        if (!UserSettingManager.getInstance().isHaveCreateGroupRight()) {
            view.findViewById(R.id.tv_create_group).setVisibility(View.GONE);
        }

        mTvCreateGroup = (TextView) view.findViewById(R.id.tv_create_group);
        mTvCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, GroupEditActivity.class);
                intent.putExtra(GroupEditActivity.EXTRA_EDIT_TYPE, GroupEditActivity.TYPE_CREATE_GROUP);
                activity.startActivity(intent);
                dismiss();
            }
        });

        mTvSearchContacts = (TextView) view.findViewById(R.id.tv_search_contacts);
        mTvSearchContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_TYPE, SearchManager.SEARCH_TYPE_CONTACTS);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_SOURCE, SearchActivity.SEARCH_SOURCE_NETWORK);
                mActivity.startActivity(intent);
                dismiss();
            }
        });
    }

    public void hideSearchContacts() {
        mTvSearchContacts.setVisibility(View.GONE);
    }
}
