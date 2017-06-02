package com.itutorgroup.tutorchat.phone.adapter.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.adapter.base.MyBaseAdapter;
import com.itutorgroup.tutorchat.phone.adapter.base.ViewHolder;
import com.itutorgroup.tutorchat.phone.domain.event.UpdateCurrentUserInfoEvent;
import com.itutorgroup.tutorchat.phone.ui.common.item.AbsItemView;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;
import com.itutorgroup.tutorchat.phone.utils.network.NetworkError;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by joyinzhao on 2016/9/29.
 */
public class AppDetailAdapter extends MyBaseAdapter<Map.Entry<String, String>> {

    AlertDialog mEnvDialog;

    public AppDetailAdapter(Context context, List data) {
        super(context, data);
    }

    @Override
    public int getItemResource(int position) {
        return R.layout.list_item_app_detail;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {
        setData(position, convertView, holder);
        return convertView;
    }

    private void setData(int position, View convertView, ViewHolder holder) {
        AbsItemView itemView = holder.getView(R.id.item_view);
        String key = mData.get(position).getKey();
        itemView.mTvTitle.setText(key);
        itemView.mTvSummary.setText(mData.get(position).getValue());
        itemView.setTag(R.id.tag_bean, mData.get(position));
        itemView.setOnLongClickListener(mOnItemLongClickListener);

        if (TextUtils.equals("app_env", key)) {
            itemView.setOnClickListener(mOnEnvClickListener);
        } else {
            itemView.setOnClickListener(null);
        }
    }

    private View.OnLongClickListener mOnItemLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) v.getTag(R.id.tag_bean);
            AppUtils.copyToClipboard(entry.getKey() + " = " + entry.getValue());
            ToastUtil.show(R.string.copy_clipboard_done);
            return true;
        }
    };

    private View.OnClickListener mOnEnvClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPromptDialog();
        }
    };

    private void showPromptDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.change_app_env_title)
                .setMessage(R.string.change_app_env_content)
                .setPositiveButton(R.string.common_prompt_continue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showListDialog();
                    }
                });
        builder.setNegativeButton(R.string.cancel, null).show();
    }

    private void showListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.change_app_env_title);
        builder.setItems(R.array.app_env_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] array = mContext.getResources().getStringArray(R.array.app_env_list);
                if (array != null && array.length > 0) {
                    String env = array[which];
                    boolean flag = AppManager.getInstance().modifyAppEnv(env);
                    ToastUtil.show(flag ? R.string.common_successful_operation : R.string.common_failed_operation);
                    mEnvDialog.dismiss();
                    if (flag) {
                        EventBusManager.getInstance().post(new UpdateCurrentUserInfoEvent(NetworkError.LOGIN_CAUSE_ENV_CHANGED));
                    }
                }
            }
        });
        mEnvDialog = builder.create();
        mEnvDialog.show();
    }
}
