package com.itutorgroup.tutorchat.phone.domain.beans.pinned;

import java.util.ArrayList;

/**
 * Created by joyinzhao on 2016/8/31.
 */
public class PinnedSectionItem<T> {
    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public final int type;
    public T data;

    public PinnedSectionItem(int type, T data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "PinnedSectionItem{" +
                "type=" + type +
                '}';
    }

    public static <T extends PinnedSectionItem> ArrayList<T> addPinnedSectionItem(ArrayList<T> target, T item) {
        int len = target.size();
        boolean exist = false;
        for (int i = 0; i < len; i++) {
            if (target.get(i).equals(item)) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            target.add(item);
        }
        return target;
    }
}
