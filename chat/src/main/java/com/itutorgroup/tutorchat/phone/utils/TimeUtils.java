package com.itutorgroup.tutorchat.phone.utils;


import android.content.res.Configuration;
import android.text.format.Time;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * @ClassName TimeUtils
 */

public class TimeUtils {

    public static final SimpleDateFormat TIME_CONTRAST_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static final SimpleDateFormat DATE_CONTRAST_SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);


    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DETAIL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat TCP_SEND_FORMAT_DATE = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final SimpleDateFormat CHINESE_CUSTOM_DATE = new SimpleDateFormat("yyyy年MM月dd日");
    public static final SimpleDateFormat TEST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm");

    public static final SimpleDateFormat HH_MM_DATE = new SimpleDateFormat("HH:mm");

    public static final SimpleDateFormat WAYBILL_FORMAT_DATE = new SimpleDateFormat("yyyy/MM/dd");

    public static final SimpleDateFormat ORDER_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日HH:mm～MM月dd日HH:mm");

    public static final SimpleDateFormat ORDER_DATE_FORMAT_HEAD = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
    public static final SimpleDateFormat ORDER_DATE_FORMAT_END = new SimpleDateFormat("MM月dd日HH:mm");

    public static final SimpleDateFormat ORDER_DATE_FORMAT_HEAD2 = new SimpleDateFormat("yyyy年MM月dd日HH:mm 到");
    public static final SimpleDateFormat ORDER_DATE_FORMAT_END2 = new SimpleDateFormat("yyyy年MM月dd日HH:mm 截止");

    public static final SimpleDateFormat PIC_NAME_FORMAT_DATE = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");

    /**
     * 当前年份第一个月
     */
    public static String getDataFistMonth() {
        Calendar cale = null;
        cale = Calendar.getInstance();

        String year = cale.get((Calendar.YEAR)) + "";
        String firstmonth = year + "-01";
        return firstmonth;
    }

    /**
     * 获取当前月份
     *
     * @return "格式 yyyy-MM"
     */
    public static String getMonth() {
        Date date = new Date();
        String dateTime = DATE_FORMAT_MONTH.format(date);
        return dateTime;
    }

    /**
     * @param gapDay
     * @return
     * @获取当前日期的差距日期
     */
    public static String getNextDay(int gapDay) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, gapDay);
        date = calendar.getTime();
        return DATE_FORMAT_DATE.format(date);
    }

    /**
     * 获取当前时间
     *
     * @param dateFormat
     * @return
     */
    public static String getTime(SimpleDateFormat dateFormat) {
        Date date = new Date();
        return dateFormat.format(date);
    }


    /**
     * 日期格式化
     *
     * @param dateTime
     * @return
     */
    public static String formatTime(String dateTime, SimpleDateFormat dateFormat1, SimpleDateFormat dateFormat) {
        try {
            Date parse = dateFormat1.parse(dateTime);
            dateTime = dateFormat.format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static String formatTime(String dateTime) {
        try {
            Date parse = DEFAULT_DATE_FORMAT.parse(dateTime);
            dateTime = CHINESE_CUSTOM_DATE.format(parse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    /**
     * 时间格式化
     * HH:mm-HH:mm
     *
     * @param startTime
     * @param endTime
     * @return 实际格式
     */
    public static String formatTime(String startTime, String endTime) {
        if (isDateEquality(startTime, endTime)) {
            Date date;
            try {
                date = DEFAULT_DATE_FORMAT.parse(startTime);
                startTime = HH_MM_DATE.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                startTime = null;
            }

            try {
                date = DEFAULT_DATE_FORMAT.parse(endTime);
                endTime = HH_MM_DATE.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                endTime = null;
            }
            return (startTime != null && endTime != null ? startTime + "-" + endTime : startTime != null ? startTime : endTime != null ? endTime : "");

        }
        return "";
    }


    /**
     * 日期格式化
     * 这个工具类注意，没什么意义
     *
     * @param dateTime
     * @return
     */
    public static String formatTime(String dateTime, SimpleDateFormat dateFormat) {
        try {
            Date parse = dateFormat.parse(dateTime);
            dateTime = dateFormat.format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            dateTime = null;
        }
        return dateTime;
    }


    /**
     * 根据当前<yyyy-MM-dd> 计算距离1970-1-1的天数
     *
     * @param mDay
     * @return
     */
    public static int currentDateToNumberDay(String mDay) {
        long day = 0l;
        try {

            Date date = DATE_FORMAT_DATE.parse(mDay);
            Date mydate = null;
            mydate = DATE_FORMAT_DATE.parse("1970-01-01");
            day = (date.getTime() - mydate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) (day / 86400000);
    }

    /**
     * 根据距离1970-1-1的天数计算当前<yyyy-MM-dd>
     *
     * @param day
     * @param dateFormat
     * @return 当前<yyyy-MM-dd>
     */
    public static String numberDayToCurrentDate(int day, SimpleDateFormat dateFormat) {
        try {
            Calendar calendar = new GregorianCalendar();
            Date date = null;
            try {
                date = dateFormat.parse("1970-01-01");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setTime(date);
            calendar.add(calendar.DATE, day);
            date = calendar.getTime();
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 将指定的长整形时间转换成指定格式日期字符串
     *
     * @param timeInMillis
     * @param dateFormat   要转换成的格式
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(timeInMillis);
    }

    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DATE_FORMAT_DATE);
    }

    public static String getTime(Date date, SimpleDateFormat dateFormat) {
        return dateFormat.format(date);
    }

    public static String getTime(Date date) {
        return getTime(date, DEFAULT_DATE_FORMAT);
    }

    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }

    /**
     * @return获取前月的第一天
     */
    public static String getDataFistDayOfmonth(SimpleDateFormat dateFormat) {
        Calendar cale = null;
        String firstday;
        // 获取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        firstday = dateFormat.format(cale.getTime());
        return firstday;
    }

    /**
     * @return获取前月的最后一天
     */
    public static String getDataLastDayOfmonth() {
        Calendar cale = null;
        String lastday;
        // 获取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        lastday = DATE_FORMAT_DATE.format(cale.getTime());
        return lastday;
    }


    /**
     * 获取和当前日期差值的日期
     *
     * @param diff 负数n天前,正数n天后
     * @return
     */
    public static String getDataDiff(int diff, SimpleDateFormat dateforamt) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.DAY_OF_MONTH, diff);
        String ret = dateforamt.format(rightNow.getTime());
        return ret;

    }

    /**
     * long时间转换成日期字符串
     *
     * @param time
     * @param dateformat
     * @return
     */
    public static String getDateString(long time, SimpleDateFormat dateformat) {
        if (dateformat == null)
            return getDateString(time);
        else
            return dateformat.format(new Date(time));
    }

    /**
     * long时间转换成日期字符串
     *
     * @param time
     * @return
     */
    public static String getDateString(long time) {
        return TimeUtils.DEFAULT_DATE_FORMAT.format(new Date(time));
    }


    /**
     * 对比日期大小
     *
     * @param startTime
     * @param endTime
     * @return
     */

    public static boolean isDateSize(String startTime, String endTime, SimpleDateFormat sdf) {
        if (startTime.equals(endTime)) {
            return true;
        }
        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(startTime);
            end = sdf.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return start.before(end);
    }


    private static boolean isDateEquality(String startTime, String endTime) {
        try {
            Date parse = DEFAULT_DATE_FORMAT.parse(startTime);
            startTime = DATE_FORMAT_DATE.format(parse);
            parse = DEFAULT_DATE_FORMAT.parse(endTime);
            endTime = DATE_FORMAT_DATE.format(parse);
            if (startTime.equals(endTime)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 显示时间格式为今天、昨天、yyyy/MM/dd hh:mm
     *
     * @return String
     */
    public static String formatTimeString(long when) {

        // 10->13 时间转换
        if (10 == Long.toString(when).length()) {
            when = when * 1000;
        }
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        String formatStr;
        StringBuilder sb = new StringBuilder();
        if (then.year != now.year) {
            formatStr = "yyyy/MM/dd";
        } else if (then.yearDay == now.yearDay - 1) {
            formatStr = LPApp.getInstance().getString(R.string.date_time_yesterday);
            sb.append(LPApp.getInstance().getString(R.string.yesterday)).append(" ");
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            formatStr = "MM/dd";
        } else {
            formatStr = "HH:mm";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sb.append(sdf.format(when)).toString();
    }

    /**
     * 判断大于5分钟显示时间
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean needShowTime(long time1, long time2) {
        int time = (int) (((time1 - time2)) / 60) / 1000;
        if (time >= 5) {
            return true;
        } else {
            return false;
        }
    }

    /*判断2分钟范围内*/
    public static boolean isIn2minute(long time1, long time2){
        int time = (int) (((time1 - time2)) / 60) / 1000;
        if (time <= 2) {
            return true;
        } else {
            return false;
        }
    }


    public static String ConvertUiSendTime(String time) {
        StringBuilder result = new StringBuilder();
        result.append(LPApp.getInstance().getString(R.string.delay_time_select)+" ");
        result.append(getWeek(time)+" ");
        try {
            int hour = Integer.parseInt(time.split(" ")[1].split(":")[0]);
            if (hour >= 0 && hour <= 11) {
                result.append(LPApp.getInstance().getString(R.string.date_am));
            } else {
                result.append(LPApp.getInstance().getString(R.string.date_pm));
            }
            try {
                Configuration configuration = LPApp.getInstance().getResources().getConfiguration();
                SimpleDateFormat sdf = new SimpleDateFormat(result.toString(),configuration.locale);
                Date date = DETAIL_DATE_FORMAT.parse(time);
                return sdf.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e){
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }


    private static  String getWeek(String pTime) {

        String Week = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(format.parse(pTime));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 1) {
            Week += LPApp.getInstance().getString(R.string.week_sunday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 2) {
            Week += LPApp.getInstance().getString(R.string.week_monday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 3) {
            Week += LPApp.getInstance().getString(R.string.week_tuesday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 4) {
            Week += LPApp.getInstance().getString(R.string.week_wednesday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 5) {
            Week += LPApp.getInstance().getString(R.string.week_thursday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 6) {
            Week += LPApp.getInstance().getString(R.string.week_friday);
        }
        if (c.get(Calendar.DAY_OF_WEEK) == 7) {
            Week += LPApp.getInstance().getString(R.string.week_saturday);
        }
        return Week;
    }

    public static boolean ComPareTimeIsOverDay(String targetTime){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date d1 = df.parse(targetTime+":00");
            long diff = d1.getTime() - System.currentTimeMillis();
            if(diff <= 0)
                return true;
            long days = diff / (1000 * 60 * 60 * 24);
            if (days >= 1)  {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }


}
