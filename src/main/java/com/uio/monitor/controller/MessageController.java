package com.uio.monitor.controller;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.ProcessMessageContentStateEnum;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.controller.req.ReceiveMessageDTO;
import com.uio.monitor.controller.req.ReceiveMessageReq;
import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.manager.MessageContentManager;
import com.uio.monitor.utils.Utils;
import com.uio.monitor.vo.WeatherInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2022/5/5 13:35
 * Description:
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController extends BaseController {

    @Autowired
    private MessageContentManager messageContentManager;
    @Value("${api.weatherApiKey:}")
    private String wechatSecretKey;

    @PostMapping("/receiveMessage")
    public BackMessage<Boolean> receiveMessage(@RequestBody ReceiveMessageReq receiveMessageReq) {
        super.verifyKey(receiveMessageReq.getKey());
        List<MessageContentDO> messageContentDOS = this.convert(receiveMessageReq.getSource(),
                receiveMessageReq.getReceiveMessageDTOList());
        messageContentDOS.forEach(item -> {
            messageContentManager.insert(item);
        });
        return BackMessage.success(true);
    }

    @GetMapping("/getWeatherForEdgeServer")
    public BackMessage<WeatherInfoVO> getWeatherForEdgeServer(@RequestParam("adCode") String adCode,
                                                       @RequestParam("secretKey") String secretKey) {
        super.verifyKey(secretKey);
        WeatherInfoVO weather = Utils.getWeather(wechatSecretKey, adCode);
        return BackMessage.success(weather);
    }

    private List<MessageContentDO> convert(String source, List<ReceiveMessageDTO> receiveMessageDTOList) {
        return Optional.ofNullable(receiveMessageDTOList).orElse(Collections.emptyList())
                .stream().map(item -> {
            MessageContentDO messageContentDO = new MessageContentDO();
            messageContentDO.setCreator(source);
            messageContentDO.setModifier(source);
            messageContentDO.setSource(source);
            messageContentDO.setSourceId(item.getSourceId() == null ? UUID.randomUUID().toString() : item.getSourceId());
            messageContentDO.setSender(item.getSender());
            messageContentDO.setMessageContent(item.getMessageContent());
            messageContentDO.setProcessState(ProcessMessageContentStateEnum.INIT.name());
            return messageContentDO;
        }).collect(Collectors.toList());
    }
}
