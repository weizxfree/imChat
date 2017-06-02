package com.itutorgroup.tutorchat.phone.domain.inter;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

/**
 * Created by tom_zxzhang on 2016/10/26.
 */
public interface OnRetrySendClickLintener {
    void retrySend(MessageModel messageModel);
    void cancelSend(MessageModel messageModel);
}
