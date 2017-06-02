package com.itutorgroup.tutorchat.phone.domain.beans;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.MD5Util;
import com.itutorgroup.tutorchat.phone.utils.manager.FileManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;

import cn.salesuite.saf.utils.StringUtils;

/**
 * Created by tom_zxzhang on 2017/1/9.
 */
public class IMMessage {

    private String content;
    private int type;
    private int chatType;
    private String status;
    private String targetId;
    private String groupId;
    private double length;
    private String localId;

    public interface ChatType {
        int CHATTYPE_SINGLE = 0;
        int CHATTYPE_GROUP = 1;
    }

    public interface Type {
        int TEXT = 1;
        int PIC = 2;
        int VOICE = 3;
        int FILE = 4;
        int GROUPANNOUNCEMENT = 5;
        int SYSTEM_MESSAGE = 6;
        int WITH_DRAWAL = 7;
        int SYSTEM_NOTICE = 8;
    }

    public interface Status {
        int MESSAGE_SEND_OK = 1;
        int MESSAGE_SNED_ERROE = -1;
        int MESSAGE_SNEDING = 0;
    }

    //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
    public static IMMessage createTxtSendMessage(String content, String targetId) {
        return createSendMessage(content, Type.TEXT, targetId);
    }

    //filePath为语音文件路径，length为录音时间(秒)
    public static IMMessage createVoiceSendMessage(String filePath, int length, String targetId) {
        final byte[] voiceBytes;
        try {
            voiceBytes = FileUtils.File2byte(filePath);
            if (StringUtils.isBlank(voiceBytes) || voiceBytes.length == 0) {
                return null;
            }
            if (length <= 0) {
                ToastUtil.show(R.string.voice_time_is_too_short);
                return null;
            }
            String content = MD5Util.getMD5String(voiceBytes);
            return createSendMessage(content, Type.VOICE, targetId, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public static IMMessage createImageSendMessage(String imagePath, boolean isOrigin, String targetId) {
        byte[] imageBytes;
        try {
            imageBytes = FileManager.getInstance().getFileBytes(imagePath, isOrigin);
            if (StringUtils.isBlank(imageBytes) || imageBytes.length == 0) {
                return null;
            }
            String content = MD5Util.getMD5String(imageBytes);
            return createSendMessage(content, Type.PIC, targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static IMMessage createSendMessage(String content, int type, String targetId, double... voiceTime) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(targetId))
            return null;
        IMMessage imMessage = new IMMessage();
        imMessage.content = content;
        imMessage.type = type;
        imMessage.chatType = ChatType.CHATTYPE_SINGLE;
        imMessage.targetId = targetId;
        imMessage.localId = MD5Util.getMD5String(System.currentTimeMillis() + "");
        if (type == Type.VOICE && voiceTime.length > 0) {
            imMessage.length = (float) voiceTime[0];
        }
        return imMessage;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }


}
