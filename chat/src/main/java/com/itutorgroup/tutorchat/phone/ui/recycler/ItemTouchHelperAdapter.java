package com.itutorgroup.tutorchat.phone.ui.recycler;

/**
 * Created by joyinzhao on 2016/10/26.
 */
public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
