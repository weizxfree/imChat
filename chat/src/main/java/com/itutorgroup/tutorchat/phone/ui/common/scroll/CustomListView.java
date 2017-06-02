package com.itutorgroup.tutorchat.phone.ui.common.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 * Created by joyinzhao on 2017/1/6.
 */
public class CustomListView extends ListView implements MyVerticalScrollLinearLayout.IVerticalChildView {
    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;

    private MyVerticalScrollLinearLayout mParent;

    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        mLastX = x;
        mLastY = y;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (Math.abs(deltaY) > Math.abs(deltaX)) {
                    if (deltaY > 0 && getCurrentScrollY() == 0) {
                        int oldAction = ev.getAction();
                        ev.setAction(MotionEvent.ACTION_DOWN);
                        mParent.postTouchEvent(ev);
                        ev.setAction(oldAction);
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(ev);
    }

    public int getCurrentScrollY() {
        View c = getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = getFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    @Override
    public void setVerticalParent(MyVerticalScrollLinearLayout parent) {
        mParent = parent;
    }

    @Override
    public View getIChildAt(int index) {
        return getChildAt(index);
    }

    @Override
    public int getIFirstVisiblePosition() {
        return getFirstVisiblePosition();
    }

    @Override
    public int getIListCount() {
        return getCount();
    }

    @Override
    public void setISelection(int position) {
        setSelection(position);
    }
}
