/**
 * 
 */
package com.itutorgroup.tutorchat.phone.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.inputmethod.InputMethodManager;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;

import cn.salesuite.saf.app.SAFFragment;
import cn.salesuite.saf.eventbus.EventBus;
import cn.salesuite.saf.log.L;

/**
 * @author Tony Shen
 *
 */
public class BaseFragment extends SAFFragment {

    public EventBus eventBus;
    public LPApp app;

    protected Handler mHandler = new Handler();
    
	public BaseFragment() {
    }
	
    //后退按键强制隐藏软键盘
    public void popBackStack() {

        FragmentManager fmgr = getFragmentManager();
        if (fmgr.getBackStackEntryCount()==0 )
        {
            mContext.finish();
            mContext.overridePendingTransition(R.anim.push_default, R.anim.push_right_out);
            // 当前窗口消费了back，那么就不再传递
            return;
        }else{
        	if (mContext.getCurrentFocus()!=null) {
            	((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow(mContext.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        	}

            fmgr.popBackStack();
        }
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        app = LPApp.getInstance();
        eventBus = EventBusManager.getInstance();
		eventBus.register(this);
		L.init(this);
	}
    
    

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
        LPApp.getInstance().getRefWatcher().watch(this);
    }
}
