package com.uio.monitor.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.BackMessage;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

@Slf4j
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

    public static WeatherInfoVO getWeatherFromRemote(String adCode, String secretKey) {
        String response = null;
        try {
            response = URLConnection.getResponse(MonitorConstant.SERVER_URL
                    + "/message/getWeatherForEdgeServer"
                    + "?secretKey=" + secretKey + "&adCode=" + adCode);
            log.info("from remote weather res is :{}", response);
            JSONObject res = JSON.parseObject(response);
            if (res == null || res.getInteger("code") != 200) {
                return null;
            }
            return JSON.parseObject(res.getString("info"), WeatherInfoVO.class);
        } catch (IOException e) {
            log.warn("request restapi for weatherInfo failed,", e);
        }
        return null;
    }

    /**
     * 获取天气实况，调用高德接口
     * @param adCode
     * @return
     */
    public static WeatherInfoVO getWeather(String weatherApiKey, String adCode) {
        String response = null;
        try {
            response = URLConnection.getResponse("https://restapi.amap.com/v3/weather/weatherInfo" +
                    "?key=" + weatherApiKey
                    + "&city=" + adCode
                    + "&extensions=base&output=json");
            log.info("weather is :{}", response);
            JSONObject resJsonObject = JSON.parseObject(response);
            String lives = resJsonObject.getString("lives");
            JSONArray jsonArray = JSON.parseArray(lives);
            return jsonArray.size() >= 1 ?
                    JSON.parseObject(jsonArray.getString(0), WeatherInfoVO.class) : null;
        } catch (IOException e) {
            log.warn("request restapi for weatherInfo failed,", e);
        }
        return null;
    }

    // {"adCode":"330100","city":"杭州市","humidity":"53","province":"浙江","reportTime":"2022-08-06 11:01:59","temperature":"37","weather":"阴","windDirection":"东南","windPower":"≤3"}
    public static String composeWeatherResult(WeatherInfoVO weatherInfoVO) {
        if (weatherInfoVO == null) {
            return null;
        }
        return weatherInfoVO.getCity() + "天气，" + "湿度：" + weatherInfoVO.getHumidity() + "，"
                + "气温：" + weatherInfoVO.getTemperature() + "，"
                + weatherInfoVO.getWeather() + "天，"
                + weatherInfoVO.getWindDirection() + "风" + weatherInfoVO.getWindPower() + "，"
                + "更新时间：" + weatherInfoVO.getReportTime();
    }

}