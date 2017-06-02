package com.itutorgroup.tutorchat.phone.utils.manager;

import android.content.Context;
import android.os.Bundle;

import com.itutorgroup.tutorchat.phone.domain.db.dao.ConversationDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.TopDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.TopModel;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.request.v2.SetChatOrderRequest;
import com.itutorgroup.tutorchat.phone.domain.response.CommonResponse;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.network.RequestHandler;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/11/14.
 */
public class TopChatManager {
    private static TopChatManager sInstance;

    public static TopChatManager getInstance() {
        if (sInstance == null) {
            synchronized (TopChatManager.class) {
                if (sInstance == null) {
                    sInstance = new TopChatManager();
                }
            }
        }
        return sInstance;
    }

    private TopChatManager() {
    }

    private List<TopModel> autoSortNum(List<TopModel> list) {
        if (list != null && list.size() > 0) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                list.get(i).Order = len - i;
            }
        }
        return list;
    }

    public void removeTop(String targetId) {
        TopDao.getInstance().remove(targetId);
        List<TopModel> list = getTopModelList();
        list = autoSortNum(list);
        if (list != null) {
            TopDao.getInstance().add(list);
        }
    }

    public void addTop(String targetId, int targetType) {
        int top = TopDao.getInstance().queryTopIndex();

        TopModel model = new TopModel();
        model.TID = targetId;
        model.IdType = targetType;
        model.Cate = 1;
        model.Order = top + 1;
        TopDao.getInstance().add(model);
        if (ConversationDao.getInstance().queryConversation(targetId) == null) {
            String groupId = targetType == TopModel.ID_TYPE_USER ? null : targetId;
            ConversationDao.getInstance().createConversation(targetId, groupId, System.currentTimeMillis());
        }
    }

    public void saveTopSort(List<TopModel> list) {
        if (list == null || list.size() == 0) {
            TopDao.getInstance().remove(null);
            EventBusManager.getInstance().post(ConversationEvent.getInstance());
            return;
        }
        Observable.just(list)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<TopModel>, List<TopModel>>() {
                    @Override
                    public List<TopModel> call(List<TopModel> list) {
                        return autoSortNum(list);
                    }
                })
                .flatMap(new Func1<List<TopModel>, Observable<TopModel>>() {
                    @Override
                    public Observable<TopModel> call(List<TopModel> list) {
                        TopDao.getInstance().reset(list);
                        return Observable.from(list);
                    }
                })
                .subscribe(new Action1<TopModel>() {
                    @Override
                    public void call(TopModel topModel) {
                        if (ConversationDao.getInstance().queryConversation(topModel.TID) == null) {
                            String groupId = topModel.IdType == TopModel.ID_TYPE_USER ? null : topModel.TID;
                            ConversationDao.getInstance().createConversation(topModel.TID, groupId, System.currentTimeMillis());
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION, new Action0() {
                    @Override
                    public void call() {
                        EventBusManager.getInstance().post(ConversationEvent.getInstance());
                    }
                });
    }

    public List<TopModel> getTopModelList() {
        return TopDao.getInstance().query();
    }

    public boolean isTop(String targetId) {
        return TopDao.getInstance().isTop(targetId);
    }

    public long getTopCount() {
        return TopDao.getInstance().getTopCount();
    }

    public boolean showSortTop(String targetId) {
        return TopDao.getInstance().isTop(targetId) && TopDao.getInstance().getTopCount() > 1;
    }

    public void requestSetChatOrder(Context context, List<TopModel> list, final RequestHandler.RequestListener listener) {
        SetChatOrderRequest request = new SetChatOrderRequest();
        request.init();
        request.Tops = list;
        new RequestHandler<>()
                .operation(Operation.SET_ORDERS)
                .request(request)
                .dialog(context)
                .exec(CommonResponse.class, new RequestHandler.RequestListener() {
                    @Override
                    public void onResponse(CommonResponse response, Bundle bundle) {
                        EventBusManager.getInstance().post(ConversationEvent.getInstance());
                        if (listener != null) {
                            listener.onResponse(response, bundle);
                        }
                    }
                });
    }
}
