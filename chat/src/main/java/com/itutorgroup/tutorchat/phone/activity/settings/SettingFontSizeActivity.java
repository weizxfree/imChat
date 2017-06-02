package com.itutorgroup.tutorchat.phone.activity.settings;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.utils.AppPrefs;
import com.itutorgroup.tutorchat.phone.utils.manager.AppManager;

/**
 * Created by joyinzhao on 2016/12/28.
 */
public class SettingFontSizeActivity extends AppLocaleSettingActivity {

    @Override
    public String getHeaderTitle() {
        return getString(R.string.app_text_size);
    }

    @Override
    public int getCheckedItem() {
        String value = AppPrefs.get(LPApp.getInstance()).getString(AppManager.PK_APP_FONT_SCALE, null);
        return AppManager.getInstance().getIndexFromArray(value, R.array.app_font_size_value, 1);
    }

    @Override
    public int getArrayResId() {
        return R.array.app_font_size_entry;
    }

    @Override
    public void configAppSettings(int index) {
        AppManager.getInstance().setFontScale(index);
    }
}
