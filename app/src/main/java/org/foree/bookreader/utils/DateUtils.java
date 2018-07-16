package org.foree.bookreader.utils;

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

    /**
     * 时间更新帮助类
     *
     * @param currentTime 当前的时间
     * @param newTime     新的时间
     * @return 更新返回true, 否则返回false
     */
    public static boolean isNewer(String currentTime, String newTime) {
        SimpleDateFormat simpleFormat = getUpdateTimeFormat();
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

    public static SimpleDateFormat getUpdateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    }

    public static String getCurrentTime() {
        SimpleDateFormat simpleFormat = getUpdateTimeFormat();
        return simpleFormat.format(new Date());
    }

    public static Date formatJSDate(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        try {
            Date date = simpleDateFormat.parse(time);
            Log.d(TAG, date.toString());
            return date;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String fromatDateToString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA);
        String dateString = simpleDateFormat.format(date);
        Log.d(TAG, date.toString());
        return dateString;
    }
}
