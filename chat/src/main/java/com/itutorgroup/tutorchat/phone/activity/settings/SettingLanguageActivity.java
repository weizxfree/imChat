package com.itutorgroup.tutorchat.phone.activity.settings;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;

/**
 * Created by joyinzhao on 2016/12/27.
 */
public class SettingLanguageActivity extends AppLocaleSettingActivity {

    @Override
    public String getHeaderTitle() {
        return getString(R.string.local_env);
    }

    @Override
    public int getCheckedItem() {
        String localeCode = AppPrefs.get(LPApp.getInstance()).getString(AppManager.PK_APP_LOCALE, null);
        int index = AppManager.getInstance().getIndexFromArray(localeCode, R.array.locale_list_value, -1);
        return index + 1;
    }

    @Override
    public int getArrayResId() {
        return R.array.locale_list_entry;
    }

    @Override
    public void configAppSettings(int index) {
        AppManager.getInstance().switchLanguage(index);
    }
}
