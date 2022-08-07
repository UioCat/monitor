package com.uio.monitor.timer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.uio.monitor.common.BillProduceWayTypeEnum;
import com.uio.monitor.common.CacheService;
import com.uio.monitor.common.ProcessMessageContentStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.manager.MessageContentManager;
import com.uio.monitor.service.PushMessageService;
import com.uio.monitor.utils.URLConnection;
import com.uio.monitor.utils.Utils;
import com.uio.monitor.vo.CityAdCodeDTO;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
* @author han xun
* Date 2022/8/5 22:08
* Description:
*/
@Component
@EnableScheduling
@Slf4j
public class MessageProcessTimer {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private MessageContentManager messageContentManager;
    @Autowired
    private Map<String, PushMessageService> pushMessageServiceMap;
    @Autowired
    private ConfigManager configManager;

    @Value("${spring.profiles.active}")
    private String env;
    @Value("${config.secretKey:}")
    private String secretKey;

    private final static String ENV = "interior_prod";
    /**
     * 5分钟锁
     */
    private final static Long TIMING_MESSAGE_LOCK_TIME = 5 * 1000L;
    private final static String JOKE_URL = "https://api.xiaobaibk.com/api/wdz/";
    private final static String CRAZY_THURSDAY = "https://kfc-crazy-thursday.vercel.app/api/index";

    /**
     * 扫描定时消息
     * 1s扫描
     */
    @Scheduled(cron = "*/1 * * * * ?")
    public void scannerMessage() {
        if (!ENV.equals(env)) {
            return;
        }
        List<MessageContentDO> messageContentDOS = messageContentManager.queryUnProcessedMessage();
        if (!CollectionUtils.isEmpty(messageContentDOS)) {
            log.info("need process msg, size:{}", messageContentDOS.size());
            for (MessageContentDO messageContentDO : messageContentDOS) {
                this.messageProcess(messageContentDO);
            }
        }
        // 将处理失败的消息改状态
    }

    private void messageProcess(MessageContentDO messageContentDO) {
        int count = messageContentManager.updateState(messageContentDO.getId(), ProcessMessageContentStateEnum.INIT,
                ProcessMessageContentStateEnum.PROCESSING);
        if (count == 0) {
            return;
        }
        String sender = messageContentDO.getSender();
        String content = messageContentDO.getMessageContent();
        String sourceId = messageContentDO.getSourceId();
        String source = messageContentDO.getSource();

        String response = this.messageContentProcess(content);

        PushWayEnum pushWayEnum = PushWayEnum.getByName(source);
        PushMessageService pushMessageService = pushWayEnum == null ?
                null : pushMessageServiceMap.get(pushWayEnum.getServiceName());
        if (pushMessageService == null || StringUtils.isEmpty(response)) {
            log.error("pushMessageService is null or content is null, source:{}, content:{}", source, content);
            return;
        }
        Boolean sendResult = pushMessageService.sendMessage(UUID.randomUUID().toString(), source, sourceId,
                PushWayEnum.WECHAT, response);
        if (sendResult) {
            messageContentManager.updateState(messageContentDO.getId(), ProcessMessageContentStateEnum.PROCESSING,
                    ProcessMessageContentStateEnum.FINISH);
        }
    }

    public String messageContentProcess(String content) {
        String response = null;
        if (content.contains("天气")) {
            response = this.weatherProcess(content);
        } else if (content.contains("疯狂星期四")) {
            try {
                response = URLConnection.getResponse(CRAZY_THURSDAY);
            } catch (IOException e) {
                log.warn("http req ioe exception, url:{}, e,", CRAZY_THURSDAY, e);
            }
        } else if (content.equals("help")) {
            response = configManager.getWechatRobotHelpConfig();
        }
        else {
            try {
                response = URLConnection.getResponse(JOKE_URL);
            } catch (IOException e) {
                log.warn("JOKE_URL ioe exception, url:{}, e,", JOKE_URL, e);
            }
        }
        return response;
    }

    public String weatherProcess(String content) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        // 分词结果
        List<SegToken> segTokenList = segmenter.process(content, JiebaSegmenter.SegMode.INDEX);

        if (!CollectionUtils.isEmpty(segTokenList)) {
            // 根据词长排序
            segTokenList.sort(Comparator.comparingInt(item -> (item.startOffset - item.endOffset)));
        }
        List<CityAdCodeDTO> hotCityCode = configManager.getHotCityCode();
        String adcode = null;
        CityAdCodeDTO cityAdCodeDTO = this.findAdcode(hotCityCode, segTokenList);
        adcode = cityAdCodeDTO == null ? null : cityAdCodeDTO.getAdcode();
        if (adcode == null) {
            // 热点数据找不到就查找所以城市
            List<CityAdCodeDTO> allCityCode = configManager.getAllCityCode();
            cityAdCodeDTO = this.findAdcode(allCityCode, segTokenList);
            // 从所有城市中匹配到的城市，把该城市直接加到热点城市
            configManager.addHotCity(cityAdCodeDTO);
            // 如果所有城市都没有，则直接返回宇宙中心天气
            adcode = cityAdCodeDTO == null ? "330110" : cityAdCodeDTO.getAdcode();
        }
        WeatherInfoVO weatherInfoVO = Utils.getWeatherFromRemote(adcode, secretKey);
        return Utils.composeWeatherResult(weatherInfoVO);
    }


    private CityAdCodeDTO findAdcode(List<CityAdCodeDTO> cityCodeList, List<SegToken> segTokenList) {
        if (CollectionUtils.isEmpty(cityCodeList) || CollectionUtils.isEmpty(segTokenList)) {
            return null;
        }

        for (SegToken segToken : segTokenList) {
            for (CityAdCodeDTO cityAdCodeDTO : cityCodeList) {
                if (cityAdCodeDTO.getCityName().equals(segToken.word) ||
                        cityAdCodeDTO.getCityName().substring(0, cityAdCodeDTO.getCityName().length() - 1).equals(
                                segToken.word)) {
                    return cityAdCodeDTO;
                }
            }
        }
        return null;
    }
}
