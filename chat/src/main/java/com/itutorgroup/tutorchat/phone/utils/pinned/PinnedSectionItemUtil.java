package com.itutorgroup.tutorchat.phone.utils.pinned;

import com.itutorgroup.tutorchat.phone.domain.beans.UserInfoVo;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.ContactsPinnedSectionItem;
import com.itutorgroup.tutorchat.phone.domain.beans.pinned.PinnedSectionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class PinnedSectionItemUtil {
    public static ArrayList<PinnedSectionItem<UserInfoVo>> parseUserVO(List<UserInfoVo> userInfoVoList) {
        ArrayList<PinnedSectionItem<UserInfoVo>> pinnedList = new ArrayList<>();

        if (userInfoVoList != null) {
            for (UserInfoVo vo : userInfoVoList) {
                PinnedSectionItem section = new ContactsPinnedSectionItem(PinnedSectionItem.SECTION, vo.firstLetter);
                PinnedSectionItem item = new PinnedSectionItem<>(PinnedSectionItem.ITEM, vo);

                if (!pinnedList.contains(section)) {
                    pinnedList.add(section);
                }
                pinnedList.add(item);
            }
        }
        return pinnedList;
    }
}
