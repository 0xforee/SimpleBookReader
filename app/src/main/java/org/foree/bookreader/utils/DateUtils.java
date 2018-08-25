package org.foree.bookreader.utils;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by foree on 17-2-25.
 * 时间处理相关工具类
 */

public class DateUtils {
    private static final String TAG = DateUtils.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final String NORMAL_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String JS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String OLD_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * 时间更新帮助类
     *
     * @param currentTime 当前的时间
     * @param newTime     新的时间
     * @return 更新返回true, 否则返回false
     */
    public static boolean isNewer(String currentTime, String newTime) {
        SimpleDateFormat simpleFormat = getNormalDateFormat();
        try {
            if (DEBUG) Log.d(TAG, "currentTime = " + currentTime);
            if (DEBUG) Log.d(TAG, "newTime = " + newTime);
            Date currentDate = simpleFormat.parse(currentTime);
            Date newDate = simpleFormat.parse(newTime);

            if (newDate.after(currentDate)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static SimpleDateFormat getNormalDateFormat() {
        return new SimpleDateFormat(NORMAL_FORMAT, Locale.CHINA);
    }

    /**
     * 解析String时间为Normal，兼容JS和OLD格式
     *
     * @param time
     * @return
     */
    public static Date parseNormal(String time) {
        try {
            return getNormalDateFormat().parse(time);
        } catch (ParseException e) {
            Log.d(TAG, "[foree] parseNormal: error, try old fromat " + time);
            // 兼容旧版本格式: 2018-11-11 23:11 和 JS格式
            SimpleDateFormat old = new SimpleDateFormat(OLD_FORMAT, Locale.CHINA);
            try {
                return old.parse(time);
            } catch (ParseException e1) {
                SimpleDateFormat js = new SimpleDateFormat(JS_FORMAT, Locale.CHINA);
                try {
                    return js.parse(time);
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取当前时间的String格式
     *
     * @return
     */
    public static String getCurrentTime() {
        return getNormalDateFormat().format(new Date());
    }

    /**
     * 解析JS的String格式为Date
     *
     * @param time js时间
     * @return Date格式
     */
    public static Date formatJSDate(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(JS_FORMAT, Locale.US);
        try {
            Date date = simpleDateFormat.parse(time);
            if (DEBUG) Log.d(TAG, date.toString());
            // 转换为通用格式
            return parseNormal(formatDateToString(date));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将Date格式转换为Normal格式，数据库中存储这种String格式
     *
     * @param date Date对象
     * @return 转换之后的格式
     */
    public static String formatDateToString(Date date) {
        SimpleDateFormat simpleDateFormat = getNormalDateFormat();
        String dateString = simpleDateFormat.format(date);
        if (DEBUG) Log.d(TAG, date.toString());
        return dateString;
    }

    /**
     * 计算给定的时间与当前时间的相对表示法
     *
     * @param context
     * @param date    给定的时间
     * @return String格式的表达
     */
    public static String relativeDate(Context context, Date date) {
        if (DEBUG) Log.d(TAG, "[foree] relativeDate: ");
        if(date == null){
            return "未知";
        }
        Date current = new Date();
        if (current.getYear() - date.getYear() > 0) {
            // 年前
            return current.getYear() - date.getYear() + "年前";
        } else if (current.getMonth() - date.getMonth() > 0) {
            // 月前
            return current.getMonth() - date.getMonth() + "月前";
        } else if (current.getDay() - date.getDay() > 0) {
            // 天前
            return current.getDay() - date.getDay() + "天前";
        } else if (current.getHours() - date.getHours() > 0) {
            // 小时前
            return current.getHours() - date.getHours() + "小时前";
        } else {
            return 1 + "小时前";
        }
    }
}
