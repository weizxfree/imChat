package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class UpdateCurrentUserInfoEvent {
    public UserInfo mCurrentUserInfo;

    public int mState = 0;

    public UpdateCurrentUserInfoEvent(UserInfo info) {
        mCurrentUserInfo = info;
    }

    public UpdateCurrentUserInfoEvent(int state) {
        mState = state;
    }
}
