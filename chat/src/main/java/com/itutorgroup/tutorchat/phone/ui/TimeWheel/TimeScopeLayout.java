package com.itutorgroup.tutorchat.phone.ui.TimeWheel;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 时间选择器
 * Created by Administrator on 2016/1/7
 */

public class TimeScopeLayout extends LinearLayout {

	private Context context ;
	/** 年 */
	private WheelView whv_year ;
	/** 月 */
	private WheelView whv_month ;
	/** 日 */
	private WheelView whv_day ;
	/** 时 */
	private WheelView whv_hour ;
	/** 分 */
	private WheelView whv_minute ;
	private static List<String> hours ;
	private static List<String> minutes ;
	private String date;
	private String hourTime;
	private String minuteTime;
	private OnTimeChangedListener onTimeChangedListener ;
	private String finalTime;
	private int curYear;
	private String[] dateType ;
	private boolean tomorrowIsNextYear = false;

	public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
		this.onTimeChangedListener = onTimeChangedListener;
	}

	public String getDate() {
		return date;
	}

	public String getStartTime() {
		return hourTime;
	}

	public void setStartTime(String startTime) {
		this.hourTime = startTime;
		try {
			whv_hour.setCurrentItem(Integer.parseInt(startTime));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public String getEndTime() {
		return minuteTime;
	}

	public void setEndTime(String endTime) {
		this.minuteTime = endTime;
		try {
			whv_minute.setCurrentItem(Integer.parseInt(endTime));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

	}

	public TimeScopeLayout(Context context) {
		super(context);
		this.context = context ;
		TimeScopeLayout.this.init();
	}

	public TimeScopeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context ;
		TimeScopeLayout.this.init();
	}

	public TimeScopeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context ;
		TimeScopeLayout.this.init();
	}

	OnWheelChangedListener listener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			updateDays(whv_month, whv_day, whv_hour, whv_minute);
		}
	};

	private void init() {
		initData();

		LayoutInflater mInflater = LayoutInflater.from(context);
		View view = mInflater.inflate(R.layout.time_scope_layout,null);
		whv_year = (WheelView) view.findViewById(R.id.whv_year);
		whv_month = (WheelView) view.findViewById(R.id.whv_month);
		whv_day = (WheelView) view.findViewById(R.id.whv_day);
		whv_hour = (WheelView) view.findViewById(R.id.whv_startTime);
		whv_minute = (WheelView) view.findViewById(R.id.whv_endTime);

		dateType = context.getResources().getStringArray(R.array.date);
		tomorrowIsNextYear = checkNextDayIsNewYear();

		if(tomorrowIsNextYear){
			whv_year.setVisibility(VISIBLE);
			Calendar calendar = Calendar.getInstance();
			curYear = calendar.get(Calendar.YEAR);
			DateNumericAdapter yearAdapter = new DateNumericAdapter(context, curYear , curYear + 1, 0);
			yearAdapter.setTextType(dateType[0]);
			whv_year.setViewAdapter(yearAdapter);
			whv_year.addChangingListener(listener);
		}


		DateNumericAdapter monthAdapter = new DateNumericAdapter(context, 1, 12, 5);
		monthAdapter.setTextType(dateType[1]);
		whv_month.setViewAdapter(monthAdapter);
		whv_month.addChangingListener(listener);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, whv_month.getCurrentItem());
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		DateNumericAdapter dayAdapter = new DateNumericAdapter(context, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1);
		dayAdapter.setTextType(dateType[2]);
		whv_day.setViewAdapter(dayAdapter);
		whv_day.addChangingListener(listener);

		TimeWheelAdapter hourWheelAdapter = new TimeWheelAdapter(context, hours.toArray());
		hourWheelAdapter.setTextType(dateType[3]);
		whv_hour.setViewAdapter(hourWheelAdapter);
		whv_hour.addChangingListener(listener);
		TimeWheelAdapter minuteWheelAdapter = new TimeWheelAdapter(context, minutes.toArray());
		minuteWheelAdapter.setTextType(dateType[4]);
		whv_minute.setViewAdapter(minuteWheelAdapter);
		whv_minute.addChangingListener(listener);

		initTime(TimeUtils.getTime(TimeUtils.DETAIL_DATE_FORMAT));
		this.addView(view, LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
	}

	private void initData(){
		minutes = new ArrayList<>();
		hours = new ArrayList<>();
		for(int i=0;i<60;i++){
			String tmp = "";
			if((i+"").length()==1){
				tmp = "0"+i;
			}else{
				tmp = i+"";
			}
			minutes.add(tmp);
		}
		for(int i=0;i<24;i++){
			String tmp = "";
			if((i+"").length()==1){
				tmp = "0"+i;
			}else{
				tmp = i+"";
			}
			hours.add(tmp);
		}
	}


	public void initTime(String initTime){
		String[] split = initTime.split(" ");
		String[] date = split[0].split("-");
		int year = 0 ;
		int month = Integer.parseInt(date[1])-1;
		int day = Integer.parseInt(date[2])-1;
		String[] startTime = split[1].split(":");
		int start = Integer.parseInt(startTime[0]);
		int end = Integer.parseInt(startTime[1]);
		whv_year.setCurrentItem(year);
		whv_month.setCurrentItem(month);
		whv_day.setCurrentItem(day);
		whv_hour.setCurrentItem(start);
		whv_minute.setCurrentItem(end);
	}

	private void updateDays( WheelView whv_month, WheelView whv_day, WheelView whv_startTime, WheelView whv_endTime) {
		Calendar calendar = Calendar.getInstance();
		if(tomorrowIsNextYear) {
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + whv_year.getCurrentItem());
		}else{
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
		}
		calendar.set(Calendar.MONTH, whv_month.getCurrentItem());
		int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//		DateNumericAdapter dayAdapter = new DateNumericAdapter(context, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1);
//		dayAdapter.setTextType(dateType[2]);
//		whv_day.setViewAdapter(dayAdapter);
		int curDay = Math.min(maxDays, whv_day.getCurrentItem() + 1);
		whv_day.setCurrentItem(curDay - 1, true);
		int years = calendar.get(Calendar.YEAR);
		date = years + "-" + (whv_month.getCurrentItem() + 1) + "-" + (whv_day.getCurrentItem() + 1);
		date = TimeUtils.formatTime(date, TimeUtils.DATE_FORMAT_DATE);
		hourTime = whv_startTime.getCurrentItem() + "";
		minuteTime = whv_endTime.getCurrentItem() + "";
		finalTime = date + " " + ((hourTime.length()==1)?"0"+hourTime:hourTime) + ":" + ((minuteTime.length()==1)?"0"+minuteTime:minuteTime);
		if (onTimeChangedListener!=null) {
			onTimeChangedListener.onTime(finalTime);
		}
	}


	private class DateNumericAdapter extends NumericWheelAdapter {
		// Index of current item
		int currentItem;
		// Index of item to be highlighted
		int currentValue;

		/**
		 * Constructor
		 */
		public DateNumericAdapter(Context context, int minValue, int maxValue,
				int current) {
			super(context, minValue, maxValue);
			this.currentValue = current;
			setTextSize(20);
		}

		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			view.setTypeface(Typeface.SANS_SERIF);
		}

		public CharSequence getItemText(int index) {
			currentItem = index;
			return super.getItemText(index);
		}
	}

	private class TimeWheelAdapter extends ArrayWheelAdapter<Object> {

		public TimeWheelAdapter(Context context, Object[] items) {
			super(context, items);
			setTextSize(20);
		}

		@Override
		protected void configureTextView(TextView view) {
			super.configureTextView(view);
			view.setTypeface(Typeface.SANS_SERIF);
		}

		@Override
		public CharSequence getItemText(int index) {
			return super.getItemText(index);
		}
	}


	public boolean isShow(){
		return this.getVisibility()==View.VISIBLE?true:false;
	}

	public void show(){
        this.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(200);
        this.startAnimation(animation);
	}


	public void conceal(){
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				TimeScopeLayout.this.setVisibility(View.GONE);
			}
		});
        this.startAnimation(animation);
	}


	private boolean checkNextDayIsNewYear(){
		Calendar calendar = Calendar.getInstance();
		int oldYear = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		int newYear = calendar.get(Calendar.YEAR);
		return !(oldYear == newYear);
	}



}
