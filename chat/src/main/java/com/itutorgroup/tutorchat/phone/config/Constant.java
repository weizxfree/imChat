/**
 *
 */
package com.itutorgroup.tutorchat.phone.config;


import com.itutorgroup.tutorchat.phone.domain.response.CheckClientVersionResponse;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;

import java.io.File;

/**
 * app所用到的常量
 *
 * @author Tony Shen
 *
 */
public class Constant {


	/** http请求响应 **/
	public static final int RESULT_SUCCESS = 0;

	public static final int MESSAGE_DEVICE_TYPE = 2; //消息设备类型PC:1,Mobile:2

	public static final int DEVICE_TYPE = 2; //设备类型（1：wpf，2：android，3：ios）

	public static final int PAGE_NUMBER = 10; //聊天栏中每一页显示数目
	public static final int APP_FORCE_UPDATE = 2; //强制更新
	public static final int REQ_FROM_CROP = 3;
	public static final int REQ_FROM_PHOTO = 4;
	public static final int REQ_FROM_CAMERA = 5;
	public static final int ALT_MESSAGE_COUNT_WILL_SHOW = 10 ;
	public static final int UNREAD_MESSAGE_COUNT_WILL_SHOW = 10 ;  //  未读信息超过N条将会显示[有*条未读消息]
	public static final int SCROLL_MESSAGE_COUNT_WILL_SHOW = 1 ;  //  当回翻历史记录超过N条，出现新消息，显示[有新消息]，点击回到底部
	public static final int NOTIFY_REFRESH = 7 ;
	public static CheckClientVersionResponse.UpdateVersion updateVersion = null;
	public static File CAMERA_FILE = null;
	public static final String CHAT_IMAGE ="chat_camera.jpg";
	public static final String DIR = "/chat";
	public static String EXTERNAL_STOREPATH = FileUtils.getExternalStorePath();
	public static final String IMAGE_DIR = DIR+"/images";
	public static final String VOICE_DIR = DIR+"/voices";
	public static final String SAVE_DIR = "/TutorChat/images";
	public static final String DOWNLOAD_DIR = "/TutorChat/download";
	public static final String CACHE_DIR = EXTERNAL_STOREPATH + DIR + "/images";
	public static final String RECORD_DIR = EXTERNAL_STOREPATH + DIR + "/voices";
	public static final String IMAGE_LODER_CACHE_DIR = DIR + "/caches/";
	public static final String SINGLE_MESSAGE_ACTIVITY_NAME = "com.itutorgroup.tutorchat.phone.activity.chat.SingleChatActivity";
	public static final String GROUP_MESSAGE_ACTIVITY_NAME = "com.itutorgroup.tutorchat.phone.activity.group.GroupChatActivity";
	public static final String MAIN_ACTIVITY_NAME = "com.itutorgroup.tutorchat.phone.activity.MainActivity";
	public static final String MESSAGE_ID = "MessageID";
	public static final String RECEIVE_MESSAGE_ID ="ReceiverMessageID";
	public static final String AES_LOGIN_KEY = "123456789abcdefg";
	public static final String AES_IV = "123456789abcdefg";
	public static final String TCP_AES_LOGIN_KEY = "8qzXbJfEkaBBpbMyt2RPMptneguNHJCx";
	public static final String TCP_AES_IV = "ZdX6aU7EYqIvy5Mu";
	public static final String DB_KEY = "Vipabc";
	public static final String ACRA_ACCOUNT = "tom_wei";
	public static final String ACRA_PWD = "123456";

}
