package com.itutorgroup.tutorchat.phone.utils.manager;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.domain.db.dao.GroupInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.dao.UserInfoDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by joyinzhao on 2016/9/2.
 */
public class SearchManager {
    private static SearchManager sInstance;

    public static final int SEARCH_TYPE_ALL = 3;
    public static final int SEARCH_TYPE_CONTACTS = 2;
    public static final int SEARCH_TYPE_GROUP = 1;
    public static final int SEARCH_TYPE_CHAT_MESSAGE = 0;

    public static class SearchGroupBean implements Serializable {
        public String name;
        public String id;
        public String count;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SearchGroupBean that = (SearchGroupBean) o;

            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return count != null ? count.equals(that.count) : that.count == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + (count != null ? count.hashCode() : 0);
            return result;
        }
    }

    public static SearchManager getInstance() {
        if (sInstance == null) {
            synchronized (SearchManager.class) {
                if (sInstance == null) {
                    sInstance = new SearchManager();
                }
            }
        }
        return sInstance;
    }

    public interface OnSearchListener {
        void onResponse(String key, List[] listArray);
    }

    public void search(final int type, final String key, final OnSearchListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (TextUtils.isEmpty(key)) {
            return;
        }
        new AsyncTask<Void, Void, List[]>() {
            @Override
            protected List[] doInBackground(Void... params) {
                List<UserInfo> userList = null;
                List<SearchGroupBean> groupList = null;
                List<MessageModel> messageList = null;
                switch (type) {
                    case SEARCH_TYPE_CONTACTS:
                        userList = searchContacts(key);
                        break;
                    case SEARCH_TYPE_GROUP:
                        groupList = searchGroup(key);
                        break;
                    case SEARCH_TYPE_CHAT_MESSAGE:
                        messageList = searchMessage(key);
                        break;
                    case SEARCH_TYPE_ALL:
                        userList = searchContacts(key);
                        groupList = searchGroup(key);
                        messageList = searchMessage(key);
                        break;
                }
                return new List[]{userList, groupList, messageList};
            }

            @Override
            protected void onPostExecute(List[] listArray) {
                listener.onResponse(key, listArray);
            }
        }.execute();

    }

    private List<MessageModel> searchMessage(String key) {
        return MessageDao.getInstance().searchByKey(key);
    }

    private List<SearchGroupBean> searchGroup(String key) {
        return GroupInfoDao.getInstance().searchByKey(key);
    }

    private List<UserInfo> searchContacts(String key) {
        return UserInfoDao.getInstance().searchByKey(key);
    }
}
