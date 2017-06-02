package com.itutorgroup.tutorchat.phone.domain.db.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.protostuff.Tag;


/**
 * Created by joyinzhao on 2016/8/30.
 */
@DatabaseTable(tableName = "single_messages")
public class MessageModel implements Serializable {

    @DatabaseField
    @Tag(1)
    public String Content;

    @DatabaseField
    @Tag(2)
    public int Type; //消息类型 1 文本 2 图片3 语音 4 文件 5 群公告

    @DatabaseField
    @Tag(3)
    public String PosterID;

    @DatabaseField
    @Tag(4)
    public String GroupId;

    @DatabaseField
    @Tag(5)
    public int Priority;

    @DatabaseField
    @Tag(6)
    public long CreateTime;

    @DatabaseField
    @Tag(7)
    public String ReceiverMessageID;

    @DatabaseField
    @Tag(8)
    public String ReceiverID;

    @DatabaseField
    @Tag(9)
    public int IsRead;

    @DatabaseField
    @Tag(10)
    public int IsSelf; // 1 自己发的 2 别人发的

    @DatabaseField(id = true, unique = true)
    @Tag(11)
    public String MessageID;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    @Tag(12)
    public ArrayList<String> AltReceivers;

    @DatabaseField
    @Tag(13)
    public String currentUserId;

    @DatabaseField
    @Tag(14)
    public String targetId;

    @Deprecated
    @DatabaseField
    @Tag(15)
    public int GroupUnReadNumbers = Integer.MAX_VALUE;

    @Deprecated
    @DatabaseField
    @Tag(16)
    public int GroupAnnouncementIsRead ;

    @DatabaseField(defaultValue = "0")
    @Tag(17)
    public int IsReceiveAndRead;

    @DatabaseField(defaultValue = "0")
    @Tag(18)
    public int MessageSendStatus;

    @DatabaseField(defaultValue = "0")
    @Tag(19)
    public float VoiceTime;

    @DatabaseField()
    @Tag(20)
    public String LocalId;

    // 是否需要设置已读回执
    @DatabaseField(defaultValue = "0")
    @Tag(21)
    public int IsReceipt;

    // AccessReadStatusPermission
    @DatabaseField(defaultValue = "0")
    @Tag(22)
    public int IsHavePermissionAccessReadStatus;

    @DatabaseField(defaultValue = "0")
    @Tag(23)
    public int targetType; // 0 单聊， 1 群聊， 2 服务号


    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getGroupUnReadNumbers() {
        return GroupUnReadNumbers;
    }

    public void setGroupUnReadNumbers(int groupUnReadNumbers) {
        GroupUnReadNumbers = groupUnReadNumbers;
    }

    public MessageModel() {
    }

    public int getGroupAnnouncementIsRead() {
        return GroupAnnouncementIsRead;
    }

    public void setGroupAnnouncementIsRead(int groupAnnouncementIsRead) {
        GroupAnnouncementIsRead = groupAnnouncementIsRead;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public String getPosterID() {
        return PosterID;
    }

    public void setPosterID(String posterID) {
        PosterID = posterID;
    }

    public String getGroupId() {
        return GroupId;
    }

    public void setGroupId(String groupId) {
        GroupId = groupId;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(long createTime) {
        CreateTime = createTime;
    }

    public String getReceiverMessageID() {
        return ReceiverMessageID;
    }

    public void setReceiverMessageID(String receiverMessageID) {
        ReceiverMessageID = receiverMessageID;
    }

    public String getReceiverID() {
        return ReceiverID;
    }

    public void setReceiverID(String receiverID) {
        ReceiverID = receiverID;
    }

    public int getIsRead() {
        return IsRead;
    }

    public void setIsRead(int isRead) {
        IsRead = isRead;
    }

    public int getIsSelf() {
        return IsSelf;
    }

    public void setIsSelf(int isSelf) {
        IsSelf = isSelf;
    }

    public String getMessageID() {
        return MessageID;
    }

    public void setMessageID(String messageID) {
        MessageID = messageID;
    }

    public List<String> getAltReceivers() {
        return AltReceivers;
    }

    public void setAltReceivers(ArrayList<String> altReceivers) {
        AltReceivers = altReceivers;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageModel that = (MessageModel) o;

        if (Type != that.Type) return false;
        if (Priority != that.Priority) return false;
        if (CreateTime != that.CreateTime) return false;
        if (IsRead != that.IsRead) return false;
        if (IsSelf != that.IsSelf) return false;
        if (GroupUnReadNumbers != that.GroupUnReadNumbers) return false;
        if (GroupAnnouncementIsRead != that.GroupAnnouncementIsRead) return false;
        if (IsReceiveAndRead != that.IsReceiveAndRead) return false;
        if (Content != null ? !Content.equals(that.Content) : that.Content != null) return false;
        if (PosterID != null ? !PosterID.equals(that.PosterID) : that.PosterID != null)
            return false;
        if (GroupId != null ? !GroupId.equals(that.GroupId) : that.GroupId != null) return false;
        if (ReceiverMessageID != null ? !ReceiverMessageID.equals(that.ReceiverMessageID) : that.ReceiverMessageID != null)
            return false;
        if (ReceiverID != null ? !ReceiverID.equals(that.ReceiverID) : that.ReceiverID != null)
            return false;
        if (MessageID != null ? !MessageID.equals(that.MessageID) : that.MessageID != null)
            return false;
        if (AltReceivers != null ? !AltReceivers.equals(that.AltReceivers) : that.AltReceivers != null)
            return false;
        if (currentUserId != null ? !currentUserId.equals(that.currentUserId) : that.currentUserId != null)
            return false;
        return !(targetId != null ? !targetId.equals(that.targetId) : that.targetId != null);

    }

    @Override
    public int hashCode() {
        int result = Content != null ? Content.hashCode() : 0;
        result = 31 * result + Type;
        result = 31 * result + (PosterID != null ? PosterID.hashCode() : 0);
        result = 31 * result + (GroupId != null ? GroupId.hashCode() : 0);
        result = 31 * result + Priority;
        result = 31 * result + (int) (CreateTime ^ (CreateTime >>> 32));
        result = 31 * result + (ReceiverMessageID != null ? ReceiverMessageID.hashCode() : 0);
        result = 31 * result + (ReceiverID != null ? ReceiverID.hashCode() : 0);
        result = 31 * result + IsRead;
        result = 31 * result + IsSelf;
        result = 31 * result + (MessageID != null ? MessageID.hashCode() : 0);
        result = 31 * result + (AltReceivers != null ? AltReceivers.hashCode() : 0);
        result = 31 * result + (currentUserId != null ? currentUserId.hashCode() : 0);
        result = 31 * result + (targetId != null ? targetId.hashCode() : 0);
        result = 31 * result + GroupUnReadNumbers;
        result = 31 * result + GroupAnnouncementIsRead;
        result = 31 * result + IsReceiveAndRead;
        return result;
    }


    @Override
    public String toString() {
        return "MessageModel{" +
                "Content='" + Content + '\'' +
                ", Type=" + Type +
                ", PosterID='" + PosterID + '\'' +
                ", GroupId='" + GroupId + '\'' +
                ", Priority=" + Priority +
                ", CreateTime=" + CreateTime +
                ", ReceiverMessageID='" + ReceiverMessageID + '\'' +
                ", ReceiverID='" + ReceiverID + '\'' +
                ", IsRead=" + IsRead +
                ", IsSelf=" + IsSelf +
                ", MessageID='" + MessageID + '\'' +
                ", AltReceivers=" + AltReceivers +
                ", currentUserId='" + currentUserId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", GroupUnReadNumbers=" + GroupUnReadNumbers +
                ", GroupAnnouncementIsRead=" + GroupAnnouncementIsRead +
                ", IsReceiveAndRead=" + IsReceiveAndRead +
                ", MessageSendStatus=" + MessageSendStatus +
                ", VoiceTime=" + VoiceTime +
                ", LocalId='" + LocalId + '\'' +
                ", IsReceipt=" + IsReceipt +
                ", IsHavePermissionAccessReadStatus=" + IsHavePermissionAccessReadStatus +
                '}';
    }
}
