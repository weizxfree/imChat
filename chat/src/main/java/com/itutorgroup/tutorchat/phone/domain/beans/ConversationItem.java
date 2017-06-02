package com.itutorgroup.tutorchat.phone.domain.beans;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;

import java.io.Serializable;

/**
 * Created by tom_zxzhang on 2016/8/19.
 */
public class ConversationItem<T> implements Serializable {

    public String imagePath;

    public String name;

    public String title;

    public String lastMessage;

    public String draft;

    public String posterId;

    public String posterName = "";

    public long time;

    public int unReadCount;

    public boolean isDisturb;

    public boolean isTop;

    public String targetId;

    public String altMeText;

    public String groupId;

    public T chatInfo;

    public MessageModel messageModel;

    public void copyFrom(ConversationItem item) {
        imagePath = item.imagePath;
        name = item.name;
        title = item.title;
        lastMessage = item.lastMessage;
        draft = item.draft;
        posterId = item.posterId;
        posterName = item.posterName;
        time = item.time;
        unReadCount = item.unReadCount;
        isDisturb = item.isDisturb;
        isTop = item.isTop;
        altMeText = item.altMeText;
        groupId = item.groupId;
        messageModel = item.messageModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConversationItem<?> that = (ConversationItem<?>) o;

        return chatInfo != null ? chatInfo.equals(that.chatInfo) : that.chatInfo == null;

    }

    @Override
    public int hashCode() {
        return chatInfo != null ? chatInfo.hashCode() : 0;
    }
}
