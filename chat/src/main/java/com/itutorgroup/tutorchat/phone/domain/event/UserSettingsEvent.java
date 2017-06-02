package com.itutorgroup.tutorchat.phone.domain.event;

import com.itutorgroup.tutorchat.phone.domain.db.model.SettingsModel;

/**
 * Created by joyinzhao on 2016/11/15.
 */
public class UserSettingsEvent {

    public SettingsModel model;

    public UserSettingsEvent(SettingsModel settingsModel) {
        model = settingsModel;
    }
}
