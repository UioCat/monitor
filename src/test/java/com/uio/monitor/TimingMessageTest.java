package com.uio.monitor;

import com.uio.monitor.common.CycleUnitEnum;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.service.TimingMessageService;
import com.uio.monitor.timer.TimingMessageScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/16 23:05
 * Description:
 */
@SpringBootTest
@Slf4j
public class TimingMessageTest {

    @Autowired
    private TimingMessageService timingMessageService;
    @Autowired
    private TimingMessageScanner timingMessageScanner;

    /**
     * 插入一条定时消息 and 开启定时任务测试
     */
    @Test
    public void insertTimingMessage() {
        AddTimingMessageReq addTimingMessageReq = new AddTimingMessageReq();
        addTimingMessageReq.setPushDateTime(new Date());
        addTimingMessageReq.setPushWay(PushWayEnum.WECHAT.name());
        addTimingMessageReq.setReceiver("自己");
        addTimingMessageReq.setMessage("{\n" +
                "\t\"message\":\"test in 1min for twice java unit test\",\n" +
                "\t\"verifyCode\":\"\"\n" +
                "}");
        addTimingMessageReq.setCycleUnit(CycleUnitEnum.MINUTE.name());
        addTimingMessageReq.setPushCycle(2);
        addTimingMessageReq.setEffective(true);

        Boolean flag = timingMessageService.addTimingMessage(0L, addTimingMessageReq);
        log.info("timingMessageService.addTimingMessage result:{}", flag);
        timingMessageScanner.scannerMessage();
    }

    @Test
    public void sendMessageTest() {
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setId(1L);
        timingMessageDO.setGmtCreate(new Date());
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setCreator("hanxun");
        timingMessageDO.setModifier("hanxun");
        timingMessageDO.setDeleted(false);
        timingMessageDO.setPushDateTime(new Date());
        timingMessageDO.setState(PushStateEnum.INIT.name());
        timingMessageDO.setPushWay(PushWayEnum.WECHAT.name());
        timingMessageDO.setReceiver("自己");
        timingMessageDO.setMessage("{\n" +
                "\t\"message\":\"test in java unit test\",\n" +
                "\t\"verifyCode\":\"\"\n" +
                "}");
        timingMessageDO.setPushCycle(0);
        timingMessageDO.setCycleUnit(CycleUnitEnum.NONE.name());
        timingMessageDO.setEffective(true);

        Boolean flag = timingMessageService.sendMessage(PushWayEnum.WECHAT, timingMessageDO);
        log.info("timingMessageService.sendMessage result:{}", flag);
    }
}
