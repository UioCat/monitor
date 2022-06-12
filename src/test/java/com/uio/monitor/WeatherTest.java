package com.uio.monitor;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.service.WeatherService;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author han xun
 * Date 2022/6/13 03:31
 * Description:
 */
@SpringBootTest
@Slf4j
public class WeatherTest {

    @Autowired
    private WeatherService weatherService;

    @Test
    public void weatherService() {
        WeatherInfoVO weather = weatherService.getWeather("330100");
        log.info(JSON.toJSONString(weather));
    }
}
