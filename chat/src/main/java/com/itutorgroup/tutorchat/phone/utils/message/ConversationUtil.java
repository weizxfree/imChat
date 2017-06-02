package com.itutorgroup.tutorchat.phone.utils.message;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.event.ClearChatHistoryEvent;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.ConversationManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/6.
 */
public class ConversationUtil {
    public static void performClearChatHistory(final Context context, final String targetId, final boolean removeConversation) {
        int title = removeConversation ? R.string.ensure_remove_conversation : R.string.clear_chat_history;
        new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle(title)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Observable.just("")
                                .observeOn(Schedulers.io())
                                .filter(new Func1<String, Boolean>() {
                                    @Override
                                    public Boolean call(String s) {
                                        return removeConversation;
                                    }
                                })
                                .subscribe(new Action1<String>() {
                                    @Override
                                    public void call(String s) {
                                        ConversationDao.getInstance().remove(targetId);
                                        ConversationManager.getInstance().setConversationTopChat(targetId, 0, false);
                                    }
                                }, CommonUtil.ACTION_EXCEPTION);
                        ConversationManager.getInstance().removeConversation(targetId, new CommonLoadingListener<Integer>() {
                            @Override
                            public void onResponse(Integer integer) {
                                if (integer >= 0) {
                                    EventBusManager.getInstance().post(ClearChatHistoryEvent.getInstance());
                                    EventBusManager.getInstance().post(ConversationEvent.getInstance());
                                    ToastUtil.show(context.getString(R.string.common_successful_operation));
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    public static void performRemoveAllConversation(final Context context) {
        new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle(R.string.clear_chat_history)
                .setMessage(R.string.message_clear_chat_history)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConversationManager.getInstance().removeAllChatHistory(new CommonLoadingListener<Integer>() {
                            @Override
                            public void onResponse(Integer integer) {
                                if (integer >= 0) {
                                    EventBusManager.getInstance().post(ClearChatHistoryEvent.getInstance());
                                    ToastUtil.show(context.getString(R.string.common_successful_operation));
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null).show();
    }

    public static void setUnreadCountAndBg(TextView tv, int i) {
        setUnreadCountAndBg(tv, i, false);
    }

    public static void setUnreadCountAndBg(TextView tv, int i, Boolean isDisturb) {
        if (i > 0) {
            if (isDisturb) {
                tv.setText("");
                tv.setBackgroundResource(R.drawable.bg_conversation_unread_disturb);
                ViewGroup.LayoutParams lp = tv.getLayoutParams();
                lp.height = PixelUtil.dp2px(10);
                lp.width = lp.height;
            } else {
                boolean isBigCount = i > 99;
                String count = isBigCount ? "99+" : String.valueOf(i);
                tv.setText(count);
                if (isBigCount) {
                    tv.setBackgroundResource(R.drawable.ic_prompt_bg_new);
                    ViewGroup.LayoutParams lp = tv.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    lp.height = PixelUtil.dp2px(15);
                    tv.setLayoutParams(lp);
                } else {
                    tv.setBackgroundResource(R.drawable.ic_point_bg);
                    ViewGroup.LayoutParams lp = tv.getLayoutParams();
                    lp.height = PixelUtil.dp2px(15);
                    lp.width = lp.height;
                    tv.setLayoutParams(lp);
                }
            }
        }
        tv.setVisibility(i > 0 ? View.VISIBLE : View.GONE);
    }
}
