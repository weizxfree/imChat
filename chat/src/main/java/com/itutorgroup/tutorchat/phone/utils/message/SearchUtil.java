package com.itutorgroup.tutorchat.phone.utils.message;

import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.GroupInfo;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.ui.common.SearchResultGroup;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.GroupManager;
import com.itutorgroup.tutorchat.phone.utils.manager.SearchManager;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/9/7.
 */
public class SearchUtil {

    public static SpannableString formatSearchStr(String key, String str) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(str)) {
            return new SpannableString(str == null ? "" : str);
        }
        SpannableString ss = new SpannableString(str);
        str = str.toLowerCase();
        key = key.toLowerCase();
        int index = str.indexOf(key);
        if (index != -1) {
            ss.setSpan(new ForegroundColorSpan(LPApp.getInstance().getResources().getColor(R.color.bg_actionbar))
                    , index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    public static void loadUser(SearchResultGroup.ViewHolder holder, int position, UserInfo user) {
        holder.avatarView.setUserHead(user.Image);
        holder.tvName.setText(formatSearchStr(holder.mKey, user.Name));
        holder.tvDepartment.setText(user.ChineseName);
        holder.tvLastMessage.setText(LPApp.getInstance().getString(R.string.personal_department_position, user.DepartmentGroup, user.Department));
    }

    public static void loadGroup(final SearchResultGroup.ViewHolder holder, int position, SearchManager.SearchGroupBean group) {
        holder.avatarView.setGroupId(group.id);
        if (TextUtils.isEmpty(group.name) || "群聊".equals(group.name)) {
            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + group.id, "");
            holder.tvName.setText(cacheName);
            GroupManager.getInstance().getDefaultGroupName(group.id, new CommonLoadingListener<String>() {
                @Override
                public void onResponse(String s) {
                    holder.tvName.setText(s);
                }
            });
        } else {
            holder.tvName.setText(formatSearchStr(holder.mKey, group.name));
            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + group.id);
        }
        holder.tvDepartment.setText("(" + group.count + ")");
        holder.tvLastMessage.setText(formatSearchStr(holder.mKey, LPApp.getInstance().getString(R.string.search_result_contain, holder.mKey)));
    }

    public static void loadMessage(final SearchResultGroup.ViewHolder holder, int position, MessageModel message) {
        holder.tvName.setText("");
        holder.tvDepartment.setText("");
        holder.tvLastMessage.setText(formatSearchStr(holder.mKey, message.Content));

        Observable.just(message.targetId)
                .subscribeOn(Schedulers.io())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return holder.mComposeMode;
                    }
                })
                .map(new Func1<String, List<MessageModel>>() {
                    @Override
                    public List<MessageModel> call(String targetId) {
                        return MessageDao.getInstance().searchTargetMessageByKey(targetId, holder.mKey);
                    }
                })
                .filter(new Func1<List<MessageModel>, Boolean>() {
                    @Override
                    public Boolean call(List<MessageModel> list) {
                        return list != null && list.size() > 1;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MessageModel>>() {
                    @Override
                    public void call(List<MessageModel> list) {
                        holder.mResultData = list;
                        holder.tvLastMessage.setText(LPApp.getInstance().getString(R.string.chat_history_search_count, list.size()));
                    }
                }, CommonUtil.ACTION_EXCEPTION);

        new AsyncTask<MessageModel, Void, Object>() {
            @Override
            protected Object doInBackground(MessageModel... params) {
                MessageModel message = params[0];
                String targetId = message.targetId;
                GroupInfo group = GroupInfoDao.getInstance().selectWithId(targetId);
                if (group != null) {
                    return group;
                } else {
                    UserInfo user = UserInfoDao.getInstance().selectWithId(targetId);
                    if (user != null) {
                        return user;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object obj) {
                if (obj != null) {
                    if (obj instanceof UserInfo) {
                        UserInfo user = (UserInfo) obj;
                        holder.avatarView.setUserHead(user.Image);
                        holder.tvName.setText(user.Name);
                        holder.tvDepartment.setText(user.DepartmentGroup);
                    } else if (obj instanceof GroupInfo) {
                        GroupInfo group = (GroupInfo) obj;
                        holder.avatarView.setGroupId(group.GroupID);
                        holder.tvDepartment.setText("");
                        if (TextUtils.isEmpty(group.GroupName) || "群聊".equals(group.GroupName)) {
                            String cacheName = AppPrefs.get(LPApp.getInstance()).getString("cache_group_name_" + group.GroupID, "");
                            holder.tvName.setText(cacheName);
                            GroupManager.getInstance().getDefaultGroupName(group.GroupID, new CommonLoadingListener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    holder.tvName.setText(s);
                                }
                            });
                        } else {
                            holder.tvName.setText(formatSearchStr(holder.mKey, group.GroupName));
                            AppPrefs.get(LPApp.getInstance()).remove("cache_group_name_" + group.GroupID);
                        }
                    }
                }
            }
        }.execute(message);
    }


}
