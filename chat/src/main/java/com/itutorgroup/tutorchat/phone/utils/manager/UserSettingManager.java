package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.Bundle;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.dao.SettingsDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.UserSettingsEvent;
import com.itutorgroup.tutorchat.phone.domain.request.UpdateChatSettingRequest;
import com.itutorgroup.tutorchat.phone.domain.request.UpdateGroupSettingRequest;
import com.itutorgroup.tutorchat.phone.domain.request.UpdateUserSettingRequest;
import com.itutorgroup.tutorchat.phone.domain.request.v2.GetUserSettingRequest;
import com.itutorgroup.tutorchat.phone.domain.request.v2.SetNewsNoticeDisturbRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.domain.response.v2.GetUserSettingResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.utils.Preconditions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tom_zxzhang on 2016/9/6.
 */
public class UserSettingManager {

    // 勿扰模式
    public static final String USER_RIGHT_DISTURB = "R000007";

    // 创建群聊
    public static final String USER_RIGHT_CREATE_GROUP = "R000003";

    // 语音
    public static final String USER_RIGHT_VOICE = "R000008";

    // 发送图片
    public static final String USER_RIGHT_SEND_PIC = "R000006";

    // 延迟消息
    public static final String USER_RIGHT_DELAY_MESSAGE = "R000011";

    private static UserSettingManager userSettingManager;

    public static UserSettingManager getInstance() {
        if (userSettingManager == null) {
            synchronized (UserSettingManager.class) {
                if (userSettingManager == null) {
                    userSettingManager = new UserSettingManager();
                }
            }
        }
        return userSettingManager;
    }

    /*查询是否拥有勿扰权限*/
    public boolean queryIsHaveDisturbRight() {
        List<String> list = AccountManager.getInstance().getRightList();
        if (Preconditions.isNotBlank(list)) {
            for (String s : list) {
                if (USER_RIGHT_DISTURB.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isHaveCreateGroupRight() {
        List<String> list = AccountManager.getInstance().getRightList();
        if (Preconditions.isNotBlank(list)) {
            for (String s : list) {
                if (USER_RIGHT_CREATE_GROUP.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkHasRight(String rightText) {
        List<String> list = AccountManager.getInstance().getRightList();
        if (Preconditions.isNotBlank(list)) {
            for (String s : list) {
                if (rightText.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateChatSetting(final String UserID, final boolean IsEnable, final int type) {

        UpdateChatSettingRequest request = new UpdateChatSettingRequest();
        request.init();
        request.ContractUserID = UserID;
        request.IsEnable = IsEnable;
        request.Type = type;
        new RequestHandler<>()
                .operation(Operation.UPDATE_CHAT_SETTING)
                .request(request)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        if (type == 1) {
                            setTargetIsDisturb(UserID, null, IsEnable);
                        } else {
                            setTargetIsShield(UserID, IsEnable);
                        }
                    }
                });

    }

    public void updateGroupSetting(final String groupId, final boolean IsEnable, int type) {
        UpdateGroupSettingRequest request = new UpdateGroupSettingRequest();
        request.init();
        request.GroupID = groupId;
        request.IsEnable = IsEnable;
        request.Type = type;
        new RequestHandler()
                .operation(Operation.UPDATE_GROUP_SETTING)
                .request(request)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        if (response != null) {
//                            new GroupUserInfoDao().updateGroupSetting(groupId, IsEnable);
                            setTargetIsDisturb(groupId, groupId, IsEnable);
                        }
                    }
                });

    }

    public void setTargetIsDisturb(final String targetId, final String groupId, final boolean isDisturb) {
        Observable.just(targetId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, SettingsModel>() {
                    @Override
                    public SettingsModel call(String targetId) {
                        SettingsModel model = getMySettings();
                        ArrayList<String> list = null;
                        if (TextUtils.isEmpty(groupId)) {
                            list = model.DisturbUsers;
                        } else {
                            list = model.DisturbGroups;
                        }
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        if (isDisturb && !list.contains(targetId)) {
                            list.add(targetId);
                        } else if (!isDisturb && list.contains(targetId)) {
                            list.remove(targetId);
                        }
                        if (TextUtils.isEmpty(groupId)) {
                            model.DisturbUsers = (list.size() > 0 ? list : null);
                        } else {
                            model.DisturbGroups = (list.size() > 0 ? list : null);
                        }
                        return model;
                    }
                })
                .map(new Func1<SettingsModel, Void>() {
                    @Override
                    public Void call(SettingsModel model) {
                        SettingsDao.getInstance().add(model);
                        return null;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBusManager.getInstance().post(new ConversationEvent(ConversationEvent.STATE_REFRESH));
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public void setTargetIsShield(final String targetId, final boolean isShield) {
        Observable.just(targetId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, SettingsModel>() {
                    @Override
                    public SettingsModel call(String targetId) {
                        SettingsModel model = getMySettings();
                        ArrayList<String> list = model.BlackIDs;
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        if (isShield && !list.contains(targetId)) {
                            list.add(targetId);
                        } else if (!isShield && list.contains(targetId)) {
                            list.remove(targetId);
                        }
                        model.BlackIDs = (list.size() > 0 ? list : null);
                        return model;
                    }
                })
                .map(new Func1<SettingsModel, Void>() {
                    @Override
                    public Void call(SettingsModel model) {
                        SettingsDao.getInstance().add(model);
                        return null;
                    }
                })
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        EventBusManager.getInstance().post(new ConversationEvent(ConversationEvent.STATE_REFRESH));
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    public boolean isTargetIsDisturb(String targetId) {
        SettingsModel model = getMySettings();
        if (model.DisturbUsers != null && model.DisturbUsers.size() > 0 && model.DisturbUsers.contains(targetId)) {
            return true;
        } else if (model.DisturbGroups != null && model.DisturbGroups.size() > 0 && model.DisturbGroups.contains(targetId)) {
            return true;
        }
        return false;
    }

    public boolean isTargetIsShield(String targetId) {
        SettingsModel model = getMySettings();
        if (model.BlackIDs != null && model.BlackIDs.size() > 0 && model.BlackIDs.contains(targetId)) {
            return true;
        }
        return false;
    }

    public SettingsModel getMySettings() {
        SettingsModel model = SettingsDao.getInstance().query();
        if (model == null) {
            model = new SettingsModel();
            model.UserId = AccountManager.getInstance().getCurrentUserId();
            model.NewsNoticed = 1;
            model.IsDisturb = 0;
        }
        return model;
    }


    public void setNewsNoticeDisturb(final int flag) {
        SetNewsNoticeDisturbRequest request = new SetNewsNoticeDisturbRequest();
        request.init();
        request.flag = flag;
        new RequestHandler<>()
                .operation(Operation.SET_NEWS_NOTICE_DISTURB)
                .request(request)
                .exec(CommonResponse.class, new RequestHandler.RequestListener() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        SettingsDao.getInstance().setNewsNoticeDisturb(flag);
                    }
                });
    }

    public void setDisturbSate(final boolean isEnable) {
        UpdateUserSettingRequest request = new UpdateUserSettingRequest();
        request.init();
        request.IsEnable = isEnable;
        request.Type = 1;//勿扰模式

        new RequestHandler<>()
                .operation(Operation.UPDATE_USER_SETTING)
                .request(request)
                .exec(CommonResponse.class, new RequestHandler.RequestListener<CommonResponse>() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        SettingsDao.getInstance().setIsDisturb(isEnable ? 1 : 0);
                    }
                });
    }

    public void setSoundEnable(boolean enable) {
        AccountManager.getInstance().setSoundEnabled(enable);
    }

    public void setVibrateEnable(boolean enable) {
        AccountManager.getInstance().setVibrateEnabled(enable);
    }

    public void loadUserSettings() {
        final GetUserSettingRequest request = new GetUserSettingRequest();
        request.init();
        new RequestHandler<>()
                .request(request)
                .operation(Operation.GET_USER_SETTINGS)
                .exec(GetUserSettingResponse.class, new RequestHandler.RequestListener<GetUserSettingResponse>() {
                    @Override
                    public void onResponse(GetUserSettingResponse response, Bundle bundle) {
                        Observable.just(response)
                                .subscribeOn(Schedulers.io())
                                .filter(new Func1<GetUserSettingResponse, Boolean>() {
                                    @Override
                                    public Boolean call(GetUserSettingResponse response) {
                                        return response.Setting != null;
                                    }
                                })
                                .map(new Func1<GetUserSettingResponse, SettingsModel>() {
                                    @Override
                                    public SettingsModel call(GetUserSettingResponse response) {
                                        return response.Setting;
                                    }
                                })
                                .filter(new Func1<SettingsModel, Boolean>() {
                                    @Override
                                    public Boolean call(SettingsModel settingsModel) {
                                        return settingsModel != null;
                                    }
                                })
                                .filter(new Func1<SettingsModel, Boolean>() {
                                    @Override
                                    public Boolean call(SettingsModel model) {
                                        return model.LastModifyTime != request.Ticks;
                                    }
                                })
                                .map(new Func1<SettingsModel, List<Void>>() {
                                    @Override
                                    public List<Void> call(SettingsModel model) {
                                        TopChatManager.getInstance().saveTopSort(model.Tops);
                                        saveUserSettings(model);
                                        return null;
                                    }
                                })
                                .subscribe();
                    }
                });
    }

    private void saveUserSettings(SettingsModel settings) {
        Observable.just(settings)
                .subscribeOn(Schedulers.io())
                .map(new Func1<SettingsModel, SettingsModel>() {
                    @Override
                    public SettingsModel call(SettingsModel model) {
                        SettingsDao.getInstance().add(model);
                        return model;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SettingsModel>() {
                    @Override
                    public void call(SettingsModel settingsModel) {
                        EventBusManager.getInstance().post(new UserSettingsEvent(settingsModel));
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }
}
