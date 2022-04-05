package com.uio.monitor.controller;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.controller.req.UpdateTimingMessageReq;
import com.uio.monitor.service.TimingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author han xun
 * Date 2022/4/3 22:31
 * Description: 定时消息
 */
@RestController
public class TimingMessageController {

    @Autowired
    private TimingMessageService timingMessageService;

    /**
     * 新增一个定时消息
     * @return
     */
    @PostMapping("/addTimingMessage")
    public BackMessage<Boolean> addTimingMessage(@RequestBody @Valid AddTimingMessageReq addTimingMessageReq) {

        return null;
    }

    /**
     * 修改/删除定时任务消息
     * @return
     */
    @PostMapping("/updateTimingMessage")
    public BackMessage<Boolean> updateTimingMessage(@RequestBody @Valid UpdateTimingMessageReq updateTimingMessageReq) {
        return null;
    }

    /**
     * 查询用户的所有定时任务消息
     * @return
     */
    @GetMapping("/getTimingMessageList")
    public BackMessage<Boolean> getTimingMessageList(@RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return null;
    }
}
