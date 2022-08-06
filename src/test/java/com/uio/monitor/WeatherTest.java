package com.uio.monitor;

import com.alibaba.fastjson.JSON;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.uio.monitor.controller.MessageController;
import com.uio.monitor.timer.MessageProcessTimer;
import com.uio.monitor.utils.Utils;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author han xun
 * Date 2022/6/13 03:31
 * Description:
 */
@SpringBootTest
@Slf4j
public class WeatherTest {

    @Value("${api.weatherApiKey:}")
    private String weatherApiKey;
    @Value("${config.secretKey:}")
    private String secretKey;
    @Autowired
    MessageProcessTimer messageProcessTimer;

    @Test
    public void weatherService() {
        WeatherInfoVO weather = Utils.getWeatherFromRemote("330100", secretKey);
        log.info("get weather from remote, weather:{}", JSON.toJSONString(weather));
        String res = Utils.composeWeatherResult(weather);
        log.info("compose weather:{}", res);
    }

    @Test
    public void weatherProcessTest() {
        String res = messageProcessTimer.weatherProcess("今天越城区天气怎么样");
        log.info("messageProcessTimer.weatherProcess res:{}", res);
    }

    @Test
    public void jiebaDemo() {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        String[] sentences =
                new String[] {"余杭区天气", "我不喜欢日本和服。", "雷猴回归人间。",
                        "越城区天气怎么样", "结果婚的和尚未结过婚的"};
        for (String sentence : sentences) {
            List<SegToken> segTokenList = segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX);
            // 排序
            segTokenList.sort(Comparator.comparingInt(item -> (item.startOffset - item.endOffset)));
            System.out.println(segTokenList.toString());
        }
    }
}
