package com.itutorgroup.tutorchat.phone.ui.common.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by joyinzhao on 2017/1/6.
 */
public class MyVerticalScrollLinearLayout extends LinearLayout {
    private int mChildrenSize;
    private int[] mChildHeight;
    private int mChildIndex;

    // 分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    // 分别记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private IVerticalChildView mChildListView;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public MyVerticalScrollLinearLayout(Context context) {
        super(context);
        init();
    }

    public MyVerticalScrollLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyVerticalScrollLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (getChildListCount() == 0) {
            return false;
        }
        boolean intercepted = false;
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (Math.abs(deltaY) > Math.abs(deltaX)) {
                    if (getScrollY() == 0 || getScrollY() != getChildAt(0).getHeight() || (getScrollY() != 0 && deltaY > 0 && getChildScrollY() == 0)) {
                        intercepted = true;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                break;
            }
            default:
                break;
        }

        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;

        return intercepted;
    }

    public int getChildScrollY() {
        View c = mChildListView.getIChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = mChildListView.getIFirstVisiblePosition();
        int top = c.getTop();
        return -top + firstVisiblePosition * c.getHeight();
    }

    public void scrollToChildPosition(int index) {
        if (mChildHeight != null) {
            int y = index * mChildHeight[0];
            scrollTo(0, y);
        }
        if (mChildListView != null && index == 0) {
            mChildListView.setISelection(0);
        }
    }

    private int getChildListCount() {
        if (mChildListView != null) {
            return mChildListView.getIListCount();
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildListCount() == 0) {
            return false;
        }
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                if (Math.abs(deltaY) > Math.abs(deltaX)) {
//                    if (getScrollY() - deltaY < 0) {
//                        scrollTo(0, 0);
//                } else if (getScrollY() >= getChildAt(0).getHeight() && deltaY < 0) {
//                    scrollTo(0, getChildAt(0).getHeight());
//                } else {
                    scrollBy(0, -deltaY);
//                }
                    if (getScrollY() >= getChildAt(0).getHeight() && deltaY < 0) {
                        scrollTo(0, getChildAt(0).getHeight());
                        int oldAction = event.getAction();
                        event.setAction(MotionEvent.ACTION_DOWN);
                        dispatchTouchEvent(event);
                        event.setAction(oldAction);
                    } else if (getScrollY() < 0 && deltaY > 0) {
                        scrollTo(0, 0);
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                int scrollY = getScrollY();
                mVelocityTracker.computeCurrentVelocity(1000);
                float yVelocity = mVelocityTracker.getYVelocity();
//                if (Math.abs(yVelocity) >= 50) {
                mChildIndex = yVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
//                } else {
//                    mChildIndex = (scrollY + mChildHeight[0] / 2) / mChildHeight[0];
//                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, 1));
                int dy = mChildIndex * mChildHeight[0] - scrollY;
                if (dy != 0) {
                    smoothScrollBy(0, dy);
                }
                mVelocityTracker.clear();
                break;
            }
            default:
                break;
        }

        mLastX = x;
        mLastY = y;
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;

        mChildHeight = new int[childCount];
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView instanceof IVerticalChildView) {
                mChildListView = (IVerticalChildView) childView;
                mChildListView.setVerticalParent(MyVerticalScrollLinearLayout.this);
            }
            if (childView.getVisibility() != View.GONE) {
                int childHeight = childView.getMeasuredHeight();
                if (i == 1) {
                    childHeight += mChildHeight[0];
                }
                mChildHeight[i] = childHeight;
                childView.layout(0, childTop, childView.getMeasuredWidth(),
                        childTop + childHeight);
                childTop += childHeight;
            }
        }
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(0, getScrollY(), 0, dy, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }

    public void onResume() {
        if (getChildListCount() == 0) {
            scrollToChildPosition(0);
        }
    }

    public interface IVerticalChildView {
        void setVerticalParent(MyVerticalScrollLinearLayout parent);

        View getIChildAt(int index);

        int getIFirstVisiblePosition();

        int getIListCount();

        void setISelection(int position);
    }

    public void postTouchEvent(MotionEvent event) {
        dispatchTouchEvent(event);
    }
}
