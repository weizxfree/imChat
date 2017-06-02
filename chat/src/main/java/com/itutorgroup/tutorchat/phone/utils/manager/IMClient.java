package com.itutorgroup.tutorchat.phone.utils.manager;

/**
 * Created by tom_zxzhang on 2017/1/9.
 */
public class IMClient  {
    private static IMClient sInstance;
    private MessageManager chatManager;
    public static IMClient getInstance() {
        if (sInstance == null) {
            synchronized (MessageManager.class) {
                if (sInstance == null) {
                    sInstance = new IMClient();
                }
            }
        }
        return sInstance;
    }
    private IMClient(){
        chatManager = MessageManager.getInstance();
    }




}
