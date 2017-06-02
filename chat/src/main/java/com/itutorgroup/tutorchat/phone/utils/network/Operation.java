package com.itutorgroup.tutorchat.phone.utils.network;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class Operation {
    // 登录
    public static final int USER_LOGIN = 1;

    // 设备连接
    public static final int CONNECT = 2;

    // 设备断开连接
    public static final int DISCONNECT = 3;

    // 添加人/群组到通讯录列表
    public static final int ADD_CONTACT = 5;


    // 移除人/群组到通讯录列表
    public static final int REMOVE_CONTACT = 6;

    // 创建群组
    public static final int CREATE_GROUP = 7;

    // 编辑群组
    public static final int EDIT_GROUP = 8;

    // 离开群组
    public static final int LEAVE_GROUP = 9;

    // 增加\取消群组管理员
    public static final int SET_GROUP_ADMIN = 10;

    // 根据英文名/中文名查询员工信息，显示前10位
    public static final int SEARCH_USER_BY_KEY = 11;

    // 获取群组信息
    public static final int GET_GROUP_INFO = 12;

    //  通讯录 
    public static final int MY_ADDRESS_LIST = 13;

    // 获取用户信息
    public static final int GET_USER = 14;

    // 更新用户头像
    public static final int UPDATE_USER_PHOTO = 15;

    // 转让群主
    public static final int ATTORN_GROUP_MASTER = 17;

    // 更新收藏的联系人与群组
    public static final int GET_ALL_CONTRACT = 21;

    // 更新群聊设置
    public static final int UPDATE_GROUP_SETTING = 22;

    // 更新单聊设置
    public static final int UPDATE_CHAT_SETTING = 23;

    // 更新个人设置
    public static final int UPDATE_USER_SETTING = 24;

    // 发送消息
    public static final int SEND_MESSAGE = 25;

    // 给群组发消息
    public static final int SEND_GROUP_MESSAGE = 26;

    // 接受消息
    public static final int RECEIVE_MESSAGE = 27;

    // 确认回执消息
    public static final int VERIFY_MESSAGE = 28;

    // 设置消息已读
    public static final int SET_READ = 29;

    // 检查消息是否
    public static final int CHECK_IS_READ = 30;

    // 查询群组中的一条消息的已读状态
    public static final int VIEW_GROUP_READ = 31;

    // 更新群公告
    public static final int UPDATE_GROUP_ANNOUNCEMENT = 32;

    // 查看群公告信息
    public static final int GET_ANNOUNCEMENT_INFO = 33;

    // 根据UserID列表获取用户信息
    public static final int GET_USER_BY_IDS = 35;

    // 发送语音消息
    public static final int SEND_VOICE_MESSAGE = 36;

    // 获取用户权限列表
    public static final int GET_RIGHT_BY_USER_ID = 37;

    // 添加错误日志
    public static final int LogError = 38;

    // 增加\取消群组管理员列表
    public static final int SET_GROUP_ADMIN_LIST = 40;

    // 上传图片文件
    public static final int UPLOAD_IMAGE_FILE = 41;

    // 下载图片文件
    public static final int DOWNDOAD_IMAGE_FILE = 42;

    // 上传语音
    public static final int UPLOAD_VOICE_FILE = 43;

    // 下载语音文件
    public static final int DOWNDOAD_VOICE_FILE = 44;

    // 检查文件是否存在
    public static final int CHECK_FILE_IS_EXIT = 45;

    // 登录（需加密）
    public static final int USER_LOGIN_V2 = 46;

    // 检查APP是否更新
    public static final int CLIENT_UPDATE = 47;

    // 设置聊天对话排序与置顶
    public static final int SET_ORDERS = 48;

    // 获取用户所有设置
    public static final int GET_USER_SETTINGS = 49;

    // 检查数据增量更新
    public static final int GET_OBJECT_UPDATE = 50;

    // 设置新消息开关
    public static final int SET_NEWS_NOTICE_DISTURB = 51;

    // 获取延迟消息
    public static final int GET_SCHEDULE_MESSAGE = 52;

    // 发送延迟消息
    public static final int EDIT_SCHEDULE_MESSAGE = 53;

    // 拉取系统公告
    public static final int SYSTEM_NOTICE = 56;

    // AES 解密失败
    public static final int AES_DECRYPT_FAILED = 57;

    // 获取服务号列表
    public static final int GET_SERVICE_ACCOUNT_LIST = 58;

    // 发送服务号请求
    public static final int SEND_SERVICE_MESSAGE = 59;

    // 增量更新-群组-名称、描述更新
    public static final int OBJECT_UPDATE_GROUP_PROFILE = 201;

    // 增量更新-群组-人员变化-普通人员
    public static final int OBJECT_UPDATE_GROUP_MEMBER_NORMAL = 203;

    // 增量更新-群组-人员变化-群主
    public static final int OBJECT_UPDATE_GROUP_MEMBER_MASTER = 204;

    // 增量更新-群组-人员变化-管理员
    public static final int OBJECT_UPDATE_GROUP_MEMBER_ADMIN = 205;

    // 增量更新-群组-人员变化-离群
    public static final int OBJECT_UPDATE_GROUP_MEMBER_LEAVE = 206;

    // 增量更新-通讯录-收藏
    public static final int OBJECT_UPDATE_CONTACTS_ADD_PEOPLE = 207;

    // 增量更新-通讯录-取消收藏
    public static final int OBJECT_UPDATE_CONTACTS_REMOVE_PEOPLE = 208;

    // 增量更新-通讯录-收藏
    public static final int OBJECT_UPDATE_CONTACTS_ADD_GROUP = 209;

    // 增量更新-通讯录-取消收藏
    public static final int OBJECT_UPDATE_CONTACTS_REMOVE_GROUP = 210;

    // 长连接 身份验证
    public static final int TCP_AUTH = 501;

    // 连接就绪
    public static final int TCP_READY = 502;

    // 断开连接
    public static final int TCP_DISCONNECTION = 503;

    // 接收消息
    public static final int TCP_RECEIVE_MESSAGE = 504;

    // 回执
    public static final int TCP_VERIFY_MESSAGE = 505;

    // 接收离线消息
    public static final int TCP_RECEIVE_OFFLINE_MESSAGE = 506;

    // 离线消息回执
    public static final int TCP_VERIFY_OFFLINE_MESSAGE = 507;

    // 上线，离线消息已接收完毕
    public static final int TCP_DEVICE_ONLINE = 508;

    // TCP推过来表示有增量更新，收到指令后进行HTTP请求
    public static final int TCP_INSTRUCTION = 509;

    public static final int TCP_INSTRUCTION_VERIFY = 510;

    // TCP离线消息，表示有增量更新，收到指令后进行HTTP请求
    public static final int TCP_OFFLINE_INSTRUCTION = 511;

    public static final int TCP_OFFLINE_INSTRUCTION_VERIFY = 512;

    // 被踢下线
    public static final int TCP_KICK_OUT = 998;

    // HEART BEAT
    public static final int TCP_HEART_BEAT = 999;
}
