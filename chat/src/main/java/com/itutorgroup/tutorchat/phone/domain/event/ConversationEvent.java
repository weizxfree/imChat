package com.itutorgroup.tutorchat.phone.domain.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class ConversationEvent {
    private static ConversationEvent sInstance;

    public List<String> mIdList;

    public int mState;
    public static final int STATE_REFRESH = 0x3001;

    public static ConversationEvent getInstance() {
        if (sInstance == null) {
            synchronized (ConversationEvent.class) {
                if (sInstance == null) {
                    sInstance = new ConversationEvent();
                }
            }
        }
        return sInstance;
    }

    private ConversationEvent() {

    }

    public ConversationEvent(int state) {
        mState = state;
    }

    public void setRefreshIdList(List<String> idList) {
        mIdList = idList;
    }

    public void setRefreshId(String id) {
        mIdList = new ArrayList<>();
        mIdList.add(id);
    }
}
