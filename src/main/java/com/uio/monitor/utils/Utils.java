package com.uio.monitor.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    
    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest  = md5.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        return new BigInteger(1, digest).toString(16);
    }

    /**
     * 获得两个时间相间隔的月份数量
     * @param earliestTime
     * @param latestTime
     * @return
     */
    public static int getApartMonths(Date earliestTime, Date latestTime) {
        Calendar earliestCalendar = Calendar.getInstance();
        earliestCalendar.setTime(earliestTime);
        Calendar latestCalendar = Calendar.getInstance();
        latestCalendar.setTime(latestTime);
        return latestCalendar.get(Calendar.YEAR) * 12 + latestCalendar.get(Calendar.MONTH)
                - earliestCalendar.get(Calendar.YEAR) * 12 - earliestCalendar.get(Calendar.MONTH);
    }

    /**
     * 获取两个时间间隔的天数
     * @param earliestTime
     * @param latestTime
     * @return
     */
    public static int getApartDays(Date earliestTime, Date latestTime) {
        long days = (latestTime.getTime() - earliestTime.getTime()) / (1000 * 60 * 60 * 24);
        return (int) days;
    }
}