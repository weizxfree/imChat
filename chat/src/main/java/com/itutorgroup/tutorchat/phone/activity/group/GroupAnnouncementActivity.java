package com.itutorgroup.tutorchat.phone.activity.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.request.UpdateGroupAnnouncementRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.dialog.ConfirmDialog;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.EnableStateUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class GroupAnnouncementActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.edt_announcement)
    EditText mEdtContent;

    @InjectView(id = R.id.fl_group_edt)
    View mGroupEdt;

    @InjectExtra(key = "group_id")
    private String mGroupId;

    private int mGroupRight;

    private TextView mTvMenu;

    public static final String EXTRA_MODE = "show_mode";
    public static final int MODE_SHOW = 0x21;
    public static final int MODE_EDIT = 0x22;

    private int mMode = MODE_SHOW;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_announcement);
        mMode = getIntent().getIntExtra(EXTRA_MODE, MODE_SHOW);
        initView();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.title_group_announcement))
                .leftText(getString(R.string.cancel), new InputMethodUtil.CancelListener(this));

        String currentUserId = AccountManager.getInstance().getCurrentUserId();
        GroupManager.getInstance().getGroupRightByUserId(mGroupId, currentUserId, new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer integer) {
                mGroupRight = integer;
                if (integer > 1) {
                    mHeaderLayout.mLayoutLeftContainer.removeAllViews();
                    mHeaderLayout.autoCancel(GroupAnnouncementActivity.this);
                    mEdtContent.setEnabled(false);
                    return;
                }
                if (mMode == MODE_SHOW) {
                    mTvMenu = mHeaderLayout.addRightText(getString(R.string.edit), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMode == MODE_SHOW) {
                                changeStateToEdit();
                            } else if (mMode == MODE_EDIT) {
                                onBtnDoneClicked();
                            }
                        }
                    });
                }
            }
        });

        if (mMode == MODE_SHOW) {
            GroupManager.getInstance().getGroupInfo(mGroupId, new CommonLoadingListener<GroupInfo>() {
                @Override
                public void onResponse(GroupInfo groupInfo) {
                    String announcementText = groupInfo.AnnouncementText;
                    mEdtContent.setText(announcementText);
                }
            });
            mEdtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    changeStateToEdit();
                }
            });
        }
        mEdtContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    private void changeStateToEdit() {
        if (mMode == MODE_SHOW && mGroupRight <= 1 && mTvMenu != null) {
            mMode = MODE_EDIT;
            mTvMenu.setText(R.string.done);
            findViewById(R.id.rl_bottom_edit).setVisibility(View.GONE);
            mEdtContent.setClickable(true);
            mEdtContent.setOnFocusChangeListener(null);
            mGroupEdt.setClickable(false);
            mGroupEdt.setFocusableInTouchMode(false);
            String text = mEdtContent.getText().toString();
            mEdtContent.requestFocus();
            InputMethodUtil.showSoftKeyBoard(GroupAnnouncementActivity.this, mEdtContent);
            if (!TextUtils.isEmpty(text)) {
                mEdtContent.setSelection(text.length());
            }
            mEdtContent.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            EnableStateUtil.proxy(mTvMenu, mEdtContent);
        }
    }

    void onBtnDoneClicked() {
        InputMethodUtil.hideInputMethod(GroupAnnouncementActivity.this);
        new ConfirmDialog(GroupAnnouncementActivity.this)
                .message(getString(R.string.tip_send_group_announcement))
                .cancelText(getString(R.string.cancel))
                .confirmText(getString(R.string.release))
                .confirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        releaseGroupAnnouncement();
                    }
                })
                .show();
    }

    private void releaseGroupAnnouncement() {
        String text = mEdtContent.getText().toString();
        final UpdateGroupAnnouncementRequest request = new UpdateGroupAnnouncementRequest();
        request.init();
        request.GroupID = mGroupId;
        request.AnnouncementText = text;

        new RequestHandler()
                .request(request)
                .operation(Operation.UPDATE_GROUP_ANNOUNCEMENT)
                .dialog(GroupAnnouncementActivity.this)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        ToastUtil.show(getString(R.string.common_successful_operation));
                        GroupManager.getInstance().onGroupAnnouncementUpdate(mGroupId, request.AnnouncementText);
                        setResult(100);
                        finish();
                    }
                });
    }
}
