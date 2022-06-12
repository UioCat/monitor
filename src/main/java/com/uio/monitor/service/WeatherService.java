package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.utils.URLConnection;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author han xun
 * Date 2022/6/13 03:04
 * Description:
 */
@Service
@Slf4j
public class WeatherService {

    @Value("${api.weatherApiKey:}")
    private String weatherApiKey;

    /**
     * 获取天气实况，调用高德接口
     * @param adCode
     * @return
     */
    public WeatherInfoVO getWeather(String adCode)  {
        String response = null;
        try {
            response = URLConnection.getResponse("" +
                    "https://restapi.amap.com/v3/weather/weatherInfo",
                    "key=" + weatherApiKey
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
}
