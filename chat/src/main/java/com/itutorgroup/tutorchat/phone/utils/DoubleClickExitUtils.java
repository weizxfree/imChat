/**
 * 
 */
package com.itutorgroup.tutorchat.phone.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;

import com.itutorgroup.tutorchat.phone.R;


/**
 * @author Tony Shen
 *
 */
public class DoubleClickExitUtils {

	private final Activity mActivity;
	
	private boolean isOnKeyBacking;
	private Handler mHandler;
	private Toast mBackToast;
	
	public DoubleClickExitUtils(Activity activity) {
		mActivity = activity;
		mHandler = new Handler(Looper.getMainLooper());
	}
	
	/**
	 * Activity onKeyDown事件
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK) {
			return false;
		}
		
		if(isOnKeyBacking) {
			mHandler.removeCallbacks(onBackTimeRunnable);
			if(mBackToast != null){
				mBackToast.cancel();
			}
			mActivity.finish();
//			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		} else {
			isOnKeyBacking = true;
			if(mBackToast == null) {
				mBackToast = Toast.makeText(mActivity, R.string.finish_by_back_again, 2000);
			}
			mBackToast.show();
			mHandler.postDelayed(onBackTimeRunnable, 2000);
			return true;
		}
	}
	
	private Runnable onBackTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			isOnKeyBacking = false;
			if(mBackToast != null){
				mBackToast.cancel();
			}
		}
	};
}
