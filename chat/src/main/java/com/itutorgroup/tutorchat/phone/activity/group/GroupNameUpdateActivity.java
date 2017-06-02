package com.itutorgroup.tutorchat.phone.activity.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.event.GroupInfoEvent;
import com.itutorgroup.tutorchat.phone.domain.request.EditGroupRequest;
import com.itutorgroup.tutorchat.phone.domain.response.EditGroupResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetGroupInfoResponse;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.ContactsManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.EnableStateUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.InputMethodUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by joyinzhao on 2016/8/25.
 */
public class GroupNameUpdateActivity extends BaseActivity {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectExtra(key = "group_id")
    private String groupId;

    private TextView mTvMenu;

    @InjectView(id = R.id.edt_group_name)
    EditText mEdtName;

    public static final int RESULT_CODE_RENAME_GROUP = 0x02;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_name_update);
        initView();
    }

    private void initView() {
        mHeaderLayout.title(getString(R.string.group_name)).autoCancel(this);
        mTvMenu = mHeaderLayout.addRightText(getString(R.string.title_activity_sure), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGroupNameRequest(mEdtName.getText().toString());
            }
        });

        EnableStateUtil.proxy(mTvMenu, mEdtName);

        GroupInfo groupInfo = GroupInfoDao.getInstance().selectWithId(groupId);

        if (groupInfo != null && !TextUtils.isEmpty(groupInfo.GroupName)) {
            mEdtName.setText(groupInfo.GroupName);
            mTvMenu.setEnabled(false);
            mEdtName.setSelection(groupInfo.GroupName.length());
        }
    }

    private void updateGroupNameRequest(final String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        EditGroupRequest editGroupRequest = new EditGroupRequest();
        editGroupRequest.MessageDeviceType = Constant.MESSAGE_DEVICE_TYPE;
        editGroupRequest.UserID = AccountManager.getInstance().getCurrentUserId();
        editGroupRequest.Token = AccountManager.getInstance().getToken();
        editGroupRequest.GroupID = groupId;
        editGroupRequest.GroupName = name;
        new RequestHandler<EditGroupResponse>()
                .dialog(GroupNameUpdateActivity.this)
                .operation(Operation.EDIT_GROUP)
                .request(editGroupRequest)
                .exec(EditGroupResponse.class, new RequestHandler.RequestListener<EditGroupResponse>() {
                    @Override
                    public void onResponse(EditGroupResponse response, Bundle bundle) {
                        ToastUtil.show(R.string.common_successful_operation);
                        GroupInfoDao.getInstance().updateGroupName(groupId, name);
                        ContactsManager.getInstance().getGroupInfo(groupId, 0, new RequestHandler.RequestListener<GetGroupInfoResponse>() {
                            @Override
                            public void onResponse(GetGroupInfoResponse response, Bundle bundle) {
                                EventBusManager.getInstance().post(new GroupInfoEvent(response.Group));
                                Intent intent = new Intent();
                                intent.putExtra("group_name", name);
                                setResult(RESULT_CODE_RENAME_GROUP, intent);
                                finish();
                            }
                        });
                    }
                });


    }


}
