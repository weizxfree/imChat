package com.itutorgroup.tutorchat.phone.domain.event;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class GlobalActionEvent {

    public static final String ACTION_AUTO_REFRESH_CURRENT_USER_INFO = "action_tutor_refresh_current_user_info";
    public static final String ACTION_AUTO_GET_SERVICE_ACCOUNT_LIST = "action_tutor_get_service_account_list";

    public String mAction;

    public GlobalActionEvent() {
    }

    public GlobalActionEvent(String action) {
        mAction = action;
    }

    public static GlobalActionEvent getInstance(String action) {
        GlobalActionEvent event = new GlobalActionEvent(action);
        return event;
    }
}
