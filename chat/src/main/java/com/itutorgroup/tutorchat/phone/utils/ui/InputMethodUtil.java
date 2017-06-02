package com.itutorgroup.tutorchat.phone.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.itutorgroup.tutorchat.phone.utils.PixelUtil;

import java.lang.ref.WeakReference;

/**
 * Created by joyinzhao on 2016/8/24.
 */
public class InputMethodUtil {

    private InputMethodUtil() {
    }

    public static final int TYPE_SCROLL = 1;
    public static final int TYPE_VISIBLE = 2;

    /**
     * @param root 最外层布局，需要调整的布局
     * @param view 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
     */
    public static void controlKeyboardLayout(final int type, final View root, final View view) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > PixelUtil.dp2px(80)) {
                    if (type == TYPE_SCROLL) {
                        int[] location = new int[2];
                        //获取scrollToView在窗体的坐标
                        view.getLocationInWindow(location);
                        //计算root滚动高度，使scrollToView在可见区域
                        int srollHeight = (location[1] + view.getHeight()) - rect.bottom;
                        root.scrollTo(0, srollHeight);
                    } else if (type == TYPE_VISIBLE) {
                        view.setVisibility(View.GONE);
                    }
                } else {
                    //键盘隐藏
                    if (type == TYPE_SCROLL) {
                        root.scrollTo(0, 0);
                    } else if (type == TYPE_VISIBLE) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public static void registerResize(final View root, final View view) {
        final int height = view.getHeight();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                root.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = root.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > PixelUtil.dp2px(80)) {
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.height = height - rootInvisibleHeight;
                    view.setLayoutParams(lp);
                } else {
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.height = height;
                    view.setLayoutParams(lp);
                }
            }
        });
    }

    public static void registerScroll(final View root, final View scrollToView) {
        controlKeyboardLayout(TYPE_SCROLL, root, scrollToView);
    }

    public static void registerVisible(final View root, final View visibleView) {
        controlKeyboardLayout(TYPE_VISIBLE, root, visibleView);
    }


    public static void showSoftKeyBoard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftKeyBoard(Context context, EditText edt) {
        hideSoftKeyBoard(context, edt.getWindowToken());
    }

    public static void hideSoftKeyBoard(Context context, IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(windowToken, 0);
        } catch (Exception e) {
        }
    }

    public static void hideInputMethod(Activity activity) {
        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
            InputMethodUtil.hideSoftKeyBoard(activity, activity.getCurrentFocus().getWindowToken());
        }
    }

    public static class CancelListener implements View.OnClickListener {
        WeakReference<Activity> mActivity;

        public CancelListener(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onClick(View v) {
            Activity activity = mActivity.get();
            if (activity != null) {
                InputMethodUtil.hideInputMethod(activity);
                activity.finish();
            }
        }
    }
}
