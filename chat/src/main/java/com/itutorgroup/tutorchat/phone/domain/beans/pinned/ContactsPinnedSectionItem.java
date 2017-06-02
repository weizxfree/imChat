package com.itutorgroup.tutorchat.phone.domain.beans.pinned;

import com.itutorgroup.tutorchat.phone.domain.beans.UserInfoVo;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class ContactsPinnedSectionItem extends PinnedSectionItem<UserInfoVo> {

    public String mSection;

    public ContactsPinnedSectionItem(int type, String section) {
        super(type, null);
        this.mSection = section;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactsPinnedSectionItem that = (ContactsPinnedSectionItem) o;

        return !(mSection != null ? !mSection.equals(that.mSection) : that.mSection != null);

    }

    @Override
    public int hashCode() {
        return mSection != null ? mSection.hashCode() : 0;
    }
}
