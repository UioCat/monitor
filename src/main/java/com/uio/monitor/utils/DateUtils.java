package com.uio.monitor.utils;


import java.util.Calendar;
import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/5 17:19
 * Description: 日期工具类
 */
public class DateUtils {

    private final static long MINUTE = 1000 * 60;
    private final static long HOUR = 1000 * 60 * 60;
    private final static long DAY = 1000 * 60 * 60 * 24;
    private final static long WEEK = 1000 * 60 * 60 * 24 * 7;

    public static Date getOffMinutes(Date oldDate, int counts) {
        return new Date(oldDate.getTime() + counts * MINUTE);
    }

    public static Date getOffHours(Date oldDate, int counts) {
        return new Date(oldDate.getTime() + counts * HOUR);
    }

    public static Date getOffDays(Date oldDate, int counts) {
        return new Date(oldDate.getTime() + counts * DAY);
    }

    public static Date getOffWeeks(Date oldDate, int counts) {
        return new Date(oldDate.getTime() + counts * WEEK);
    }

    public static Date getOffMonths(Date oldDate, int counts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.add(Calendar.MONTH, counts);
        return calendar.getTime();
    }

    public static Date getOffYears(Date oldDate, int counts) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.add(Calendar.YEAR, counts);
        return calendar.getTime();
    }
}
