package com.itutorgroup.tutorchat.phone.activity.chat;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.BaseActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.beans.DelayMessageModel;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.request.EditSchedulerMsgRequest;
import com.itutorgroup.tutorchat.phone.domain.request.GetSchedulerMsgsRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.domain.response.GetSchedulerMsgsResponse;
import com.itutorgroup.tutorchat.phone.ui.TextWithDrawableView;
import com.itutorgroup.tutorchat.phone.ui.TimeWheel.OnTimeChangedListener;
import com.itutorgroup.tutorchat.phone.ui.TimeWheel.TimeScopeLayout;
import com.itutorgroup.tutorchat.phone.ui.common.CommonSwitchButton;
import com.itutorgroup.tutorchat.phone.ui.common.HeaderLayout;
import com.itutorgroup.tutorchat.phone.ui.common.item.SelectItemView;
import com.itutorgroup.tutorchat.phone.ui.common.item.SwitchItemView;
import com.itutorgroup.tutorchat.phone.ui.dialog.ConfirmDialog;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import java.text.ParseException;

import cn.salesuite.saf.inject.annotation.InjectExtra;
import cn.salesuite.saf.inject.annotation.InjectView;
import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by tom_zxzhang on 2016/10/31.
 */
public class DelayedSendActivity extends BaseActivity implements OnTimeChangedListener {

    @InjectView(id = R.id.common_actionbar)
    HeaderLayout mHeaderLayout;

    @InjectView(id = R.id.sendTime)
    SelectItemView mSendTimeView;

    @InjectView(id = R.id.timeWheelview)
    TimeScopeLayout timeWheelview;

    @InjectView(id = R.id.sendDelay)
    SwitchItemView sendDelay;

    @InjectView(id = R.id.edt_send_delay)
    EditText edtSendContent;

    @InjectExtra(key = "target_id")
    String targetID;

    @InjectExtra(key = "is_group")
    boolean isGroup;
    static final int SCHEDULER_MESSAGE_START = 1;
    static final int SCHEDULER_MESSAGE_DRAFT = 2;
    static final int SCHEDULER_MESSAGE_DELETE = 3;
    static int SchedulerMsgStatus = SCHEDULER_MESSAGE_START;   //1 启用 2：草稿 3：删除
    private String selectTime;
    private long excuteTime;
    private TextView mSureTextView;
    private boolean isEdit = false;
    public String DELAY_MESSAGE_KEY ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_delay_send);
        DELAY_MESSAGE_KEY = "delay_message_key_"+ AccountManager.getInstance().getCurrentUserId() + targetID;
        initData();
        initView();
        GetSchedulerMsgsRequest();
    }


    private void initView() {
        sendDelay.mSwitchButton.setSwitchState(true);

        final DelayMessageModel messageModel = (DelayMessageModel) AppPrefs.get(mContext).getObject(DELAY_MESSAGE_KEY);

        mHeaderLayout.mLayoutLeftContainer.addView(new TextWithDrawableView(mContext));
        mHeaderLayout.mLayoutLeftContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmSaveContent();
            }
        });

        if (SchedulerMsgStatus == SCHEDULER_MESSAGE_DRAFT && messageModel.isSendSuccess) {
            mSureTextView = mHeaderLayout.title(getString(R.string.delay_send_title)).addRightText(getString(R.string.delay_send_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editSchedulerMsg(SCHEDULER_MESSAGE_DELETE);
                }
            });
            mSureTextView.setEnabled(true);

        } else if (SchedulerMsgStatus == SCHEDULER_MESSAGE_START || (SchedulerMsgStatus == SCHEDULER_MESSAGE_DRAFT && !messageModel.isSendSuccess)) {
            mSureTextView = mHeaderLayout.title(getString(R.string.delay_send_title)).addRightText(getString(R.string.done), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editSchedulerMsg(SCHEDULER_MESSAGE_START);

                }
            });
            mSureTextView.setEnabled(false);
        }

        edtSendContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (StringUtils.isNotEmpty(s.toString()) && StringUtils.isNotBlank(messageModel) && !s.toString().equals(messageModel.content)
                        || StringUtils.isNotEmpty(s.toString()) && StringUtils.isBlank(messageModel)) {
                    UpdateMessageInDraftSate();
                    mSureTextView.setEnabled(true);
                    isEdit = true;
                }

            }
        });


        mSendTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInput();
                if (timeWheelview.isShown()) {
                    if (!mSendTimeView.mTvTitle.getText().toString().equals(getString(R.string.delay_send_time))) {
                        mSendTimeView.reset(mSendTimeView.mTvTitle.getText().toString());
                    }
                    timeWheelview.conceal();
                } else {
                    timeWheelview.show();
                }

            }
        });

        sendDelay.mSwitchButton.setOnSwitchStateListener(new CommonSwitchButton.OnSwitchListener() {
            @Override
            public void onSwitched(boolean isSwitchOn) {
                if (isSwitchOn) {
                    mSendTimeView.setVisibility(View.VISIBLE);
                } else {
                    if (timeWheelview.isShown()) {
                        timeWheelview.conceal();
                        setSureEnbleWhenConceal();
                    }
                    mSendTimeView.setVisibility(View.GONE);
                }
            }
        });
        timeWheelview.setOnTimeChangedListener(this);

    }


    @Override
    public void onTime(String time) {
        selectTime = time;
        if (!isEdit) {
            isEdit = true;
        }
        try {
            excuteTime = TimeUtils.DETAIL_DATE_FORMAT.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setEditTime(TimeUtils.ConvertUiSendTime(time));
        setSureEnbleWhenConceal();
    }

    private void setEditTime(String time) {
        mSendTimeView.clear();
        mSendTimeView.mTvTitle.setText(time);
        mSendTimeView.mTvTitle.setTextColor(getResources().getColor(R.color.app_blackground));
    }


    private void initData() {


        DelayMessageModel messageModel = (DelayMessageModel) AppPrefs.get(mContext).getObject(DELAY_MESSAGE_KEY);
        if (StringUtils.isNotBlank(messageModel)) {
            SchedulerMsgStatus = SCHEDULER_MESSAGE_DRAFT;
            mSendTimeView.reset(TimeUtils.ConvertUiSendTime(TimeUtils.getTime(messageModel.excuteTime, TimeUtils.DETAIL_DATE_FORMAT) + ""));
            excuteTime = messageModel.excuteTime;
            edtSendContent.setText(messageModel.content);
        } else {
            SchedulerMsgStatus = SCHEDULER_MESSAGE_START;
            mSendTimeView.reset(TimeUtils.ConvertUiSendTime(TimeUtils.DETAIL_DATE_FORMAT.format(new java.util.Date())));
        }

    }

    private void ConfirmSaveContent() {

        if (!isEdit) {
            finish();
            return;
        }
        new ConfirmDialog(mContext)
                .message(mContext.getString(R.string.str_delay_message_save))
                .confirmText(mContext.getString(R.string.confirm))
                .cancelText(mContext.getString(R.string.cancel))
                .confirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DelayMessageModel messageModel = new DelayMessageModel();
                        messageModel.content = edtSendContent.getText().toString();
                        messageModel.excuteTime = excuteTime == 0 ? System.currentTimeMillis() : excuteTime;
                        messageModel.isSendSuccess = false;
                        AppPrefs.get(mContext).putObject(DELAY_MESSAGE_KEY, messageModel);
                        finish();
                    }
                })
                .cancel(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                })
                .show();
    }


    private void setSureEnbleWhenConceal() {
        if (StringUtils.isNotEmpty(edtSendContent.getText().toString())) {
            mSureTextView.setEnabled(true);
            UpdateMessageInDraftSate();
        }
    }

    private void hideInput() {
        InputMethodManager manager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(edtSendContent.getWindowToken(), 0);
    }


    private void editSchedulerMsg(final int msgStatus) {
        if (TimeUtils.ComPareTimeIsOverDay(selectTime)) {
            ToastUtil.show(getString(R.string.delay_send_time_incorrect));
            return;
        }
        final EditSchedulerMsgRequest request = new EditSchedulerMsgRequest();
        request.init();
        request.ScheduleType = 4;         // 1：每天 2：每周 3：每月 4：一次性
        request.ExecuteTime = excuteTime;
        request.Content = edtSendContent.getText().toString();
        request.Type = MessageType.TEXT;
        request.ReceiverID = targetID;
        request.IsGroup = isGroup ? 2 : 1;
        request.SchedulerMsgStatus = msgStatus;  // 1 启用 2：草稿 3：删除
        new RequestHandler<CommonResponse>()
                .operation(Operation.EDIT_SCHEDULE_MESSAGE)
                .request(request)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        ToastUtil.show(LPApp.getInstance().getString(R.string.common_successful_operation));
                        if (msgStatus == SCHEDULER_MESSAGE_DELETE) {
                            resetUi();
                        } else {
                            DelayMessageModel messageModel = new DelayMessageModel();
                            messageModel.content = request.Content;
                            messageModel.excuteTime = request.ExecuteTime;
                            messageModel.isSendSuccess = true;
                            AppPrefs.get(mContext).putObject(DELAY_MESSAGE_KEY, messageModel);
                        }
                        finish();
                    }
                });
    }


    private void GetSchedulerMsgsRequest() {

        GetSchedulerMsgsRequest request = new GetSchedulerMsgsRequest();
        request.init();
        request.receiverID = targetID;
        new RequestHandler<GetSchedulerMsgsResponse>()
                .operation(Operation.GET_SCHEDULE_MESSAGE)
                .request(request)
                .exec(GetSchedulerMsgsResponse.class, new RequestHandler.RequestListener<GetSchedulerMsgsResponse>() {
                    @Override
                    public void onResponse(GetSchedulerMsgsResponse response, Bundle bundle) {
                        super.onResponse(response, bundle);
                        DelayMessageModel lastMessageModel = (DelayMessageModel) AppPrefs.get(mContext).getObject(DELAY_MESSAGE_KEY);
                        if (StringUtils.isNotBlank(lastMessageModel) && lastMessageModel.isSendSuccess && (StringUtils.isBlank(response) || StringUtils.isBlank(response.SchedulerMsgs))){
                            resetUi();
                        }
                        else if(StringUtils.isNotBlank(response) && StringUtils.isNotBlank(response.SchedulerMsgs)){
                            if (System.currentTimeMillis() > response.SchedulerMsgs.ExecuteTime) {
                                resetUi();
                            }else if(StringUtils.isNotBlank(response)){
                                DelayMessageModel messageModel = new DelayMessageModel();
                                messageModel.content = response.SchedulerMsgs.Content;
                                messageModel.excuteTime = response.SchedulerMsgs.ExecuteTime;
                                messageModel.isSendSuccess = true;
                                AppPrefs.get(mContext).putObject(DELAY_MESSAGE_KEY, messageModel);
                            }
                        }
                        initData();
                    }

                    @Override
                    public void onError(int errorCode, GetSchedulerMsgsResponse response, Exception e, Bundle bundle) {
                        super.onError(errorCode, response, e, bundle);


                    }
                });


    }


    private void resetUi() {
        edtSendContent.setText("");
        edtSendContent.requestFocus();
        AppPrefs.get(mContext).remove(DELAY_MESSAGE_KEY);
        mHeaderLayout.mLayoutRightContainer.removeAllViews();
        mSureTextView = mHeaderLayout.title(getString(R.string.delay_send_title)).addRightText(getString(R.string.done), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSchedulerMsg(SCHEDULER_MESSAGE_START);
            }
        });
        mSureTextView.setEnabled(false);
    }

    private void UpdateMessageInDraftSate() {
        if (SchedulerMsgStatus == SCHEDULER_MESSAGE_DRAFT) {
            mHeaderLayout.mLayoutRightContainer.removeAllViews();
            mSureTextView = mHeaderLayout.title(getString(R.string.delay_send_title)).addRightText(getString(R.string.done), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editSchedulerMsg(SCHEDULER_MESSAGE_START);
                }
            });
        }
    }


}
