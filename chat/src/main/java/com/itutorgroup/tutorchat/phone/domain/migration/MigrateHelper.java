package com.itutorgroup.tutorchat.phone.domain.migration;

import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.TcpMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/11/19.
 */
public class MigrateHelper {
    public static <T, R> List<R> migration(List<T> srcList, IMigrationListener<T, R> listener) {
        if (srcList == null || srcList.size() == 0 || listener == null) {
            return null;
        }
        List<R> list = new ArrayList<>();
        for (T t : srcList) {
            R r = listener.migrate(t);
            list.add(r);
        }
        return list;
    }

    public static final IMigrationListener<TcpMessageModel, MessageModel> sMessageModelMigrationListener = new IMigrationListener<TcpMessageModel, MessageModel>() {
        @Override
        public MessageModel migrate(TcpMessageModel src) {
            MessageModel messageModel = new MessageModel();
            messageModel.Content = src.Content;
            messageModel.Type = src.Type;
            messageModel.PosterID = src.PosterID;
            messageModel.GroupId = src.GroupId;
            messageModel.Priority = src.Priority;
            messageModel.CreateTime = src.CreateTime;
            messageModel.ReceiverMessageID = src.ReceiverMessageID;
            messageModel.ReceiverID = src.ReceiverID;
            messageModel.IsRead = src.IsRead;
            messageModel.IsSelf = src.IsSelf;
            messageModel.AltReceivers = src.AltReceivers;
            messageModel.MessageID = String.valueOf(src.MessageID);
            messageModel.IsReceipt = src.IsReceipt;
            return messageModel;
        }
    };
}
