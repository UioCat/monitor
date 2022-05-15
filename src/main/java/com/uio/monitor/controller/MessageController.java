package com.uio.monitor.controller;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.ProcessMessageContentStateEnum;
import com.uio.monitor.controller.req.ReceiveMessageReq;
import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.manager.MessageContentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2022/5/5 13:35
 * Description:
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @Autowired
    private MessageContentManager messageContentManager;

    @PostMapping("/receiveMessage")
    public BackMessage<Boolean> receiveMessage(@RequestBody List<ReceiveMessageReq> receiveMessageReqList) {
//        MessageContentDO messageContentDOInDB = messageContentManager.queryBySourceId(
//                receiveMessageReq.getSource(), receiveMessageReq.getSourceId());
//        if (messageContentDOInDB != null) {
//            log.warn("receive message is exist, sourceId:{}, source:{}",
//                    receiveMessageReq.getSourceId(), receiveMessageReq.getSource());
//            return BackMessage.success(false);
//        }
        List<MessageContentDO> messageContentDOS = this.convert(receiveMessageReqList);
        messageContentDOS.forEach(item -> {
            messageContentManager.insert(item);
        });
        return BackMessage.success(true);
    }

    private List<MessageContentDO> convert(List<ReceiveMessageReq> receiveMessageReqList) {
        return Optional.ofNullable(receiveMessageReqList).orElse(Collections.emptyList())
                .stream().map(receiveMessageReq -> {
            MessageContentDO messageContentDO = new MessageContentDO();
            messageContentDO.setCreator(receiveMessageReq.getSource());
            messageContentDO.setModifier(receiveMessageReq.getSource());
            messageContentDO.setSource(receiveMessageReq.getSource());
            messageContentDO.setSourceId(receiveMessageReq.getSourceId());
            messageContentDO.setMessageContent(receiveMessageReq.getMessageContent());
            messageContentDO.setProcessState(ProcessMessageContentStateEnum.INIT.name());
            return messageContentDO;
        }).collect(Collectors.toList());
    }
}
