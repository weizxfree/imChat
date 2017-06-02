package com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher;

import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.Instruction;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch.ContactPatches;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch.GroupPatches;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch.MessageReadPatches;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch.SettingPatches;
import com.itutorgroup.tutorchat.phone.domain.beans.objectupdate.patch.UserPatches;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.event.MessageReadEvent;
import com.itutorgroup.tutorchat.phone.domain.request.tcp.VerifyMessageRequest;
import com.itutorgroup.tutorchat.phone.domain.response.tcp.TcpInstructionResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.ProtoStuffSerializerUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.common.ObjectUpdateHelper;
import com.itutorgroup.tutorchat.phone.utils.manager.PushInfoManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.tcp.IDispatchListener;

import java.util.ArrayList;

/**
 * Created by joyinzhao on 2016/11/21.
 */
public class InstructionDispatcher implements IDispatchListener {
    private static InstructionDispatcher sInstance;

    public static InstructionDispatcher getInstance() {
        if (sInstance == null) {
            synchronized (InstructionDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new InstructionDispatcher();
                }
            }
        }
        return sInstance;
    }

    private InstructionDispatcher() {
    }

    @Override
    public void dispatch(int operation, byte[] bytes, DataDispatcher.IDataListener listener) {
        TcpInstructionResponse response = ProtoStuffSerializerUtil.deserialize(bytes, TcpInstructionResponse.class);
        LogUtil.d("instruction response = " + response);
        if (response != null && response.ReceiptID != 0) {
            if (response.InstructionList != null && response.InstructionList.size() > 0) {
                VerifyMessageRequest request = new VerifyMessageRequest();
                request.ReceiptID = response.ReceiptID;
                int op = (operation == Operation.TCP_INSTRUCTION ?
                        Operation.TCP_INSTRUCTION_VERIFY :
                        Operation.TCP_OFFLINE_INSTRUCTION_VERIFY);
                listener.sendRequest(op, request);
                for (Instruction item : response.InstructionList) {
                    dispatchInstruction(item, listener);
                }
            }
        }
    }

    private void dispatchInstruction(Instruction item, DataDispatcher.IDataListener listener) {
        int type = item.InstType;
        switch (type) {
            case ObjectUpdateHelper.INST_TYPE_CURRENT_USER_SETTING:
                instUserSettings(type, item.InstData, listener);
                break;
            case ObjectUpdateHelper.INST_TYPE_MESSAGE_READ_STATE:
                instMessageRead(item.InstData, listener);
                break;
            case ObjectUpdateHelper.INST_TYPE_USER_INFO:
                instUserInfo(type, item.InstData, listener);
                break;
            case ObjectUpdateHelper.INST_TYPE_CURRENT_USER_CONTACTS:
                instContacts(type, item.InstData, listener);
                break;
            case ObjectUpdateHelper.INST_TYPE_GROUP:
                instGroup(type, item.InstData, listener);
                break;
        }
    }

    private void instGroup(int type, byte[] instData, DataDispatcher.IDataListener listener) {
        GroupPatches patches = ProtoStuffSerializerUtil.deserialize(instData, GroupPatches.class);
        if (patches != null) {
            listener.onPatches(type, patches.TimeSpan, patches.GroupId);
        }
    }

    private void instUserInfo(int type, byte[] instData, DataDispatcher.IDataListener listener) {
        UserPatches patches = ProtoStuffSerializerUtil.deserialize(instData, UserPatches.class);
        if (patches != null) {
            listener.onPatches(type, patches.TimeSpan, patches.UserId);
        }
    }

    private void instUserSettings(int type, byte[] instData, DataDispatcher.IDataListener listener) {
        SettingPatches patches = ProtoStuffSerializerUtil.deserialize(instData, SettingPatches.class);
        if (patches != null) {
            listener.onPatches(type, patches.TimeSpan, null);
        }
    }

    private void instContacts(int type, byte[] instData, DataDispatcher.IDataListener listener) {
        ContactPatches patches = ProtoStuffSerializerUtil.deserialize(instData, ContactPatches.class);
        if (patches != null) {
            listener.onPatches(type, patches.TimeSpan, null);
        }
    }

    private void instMessageRead(byte[] instData, DataDispatcher.IDataListener listener) {
        MessageReadPatches data = ProtoStuffSerializerUtil.deserialize(instData, MessageReadPatches.class);
        if (data == null) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        for (Long id : data.MessageIdList) {
            list.add(String.valueOf(id));
        }
        if (list.size() > 0) {
            MessageDao.getInstance().setMessageRead(list);
            EventBusManager.getInstance().post(new MessageReadEvent(list));
            listener.updateConversationList();
            PushInfoManager.getInstance().refreshNotificationStatus(list);
        }

    }
}
