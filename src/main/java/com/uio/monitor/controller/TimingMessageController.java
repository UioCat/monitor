package com.uio.monitor.controller;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.controller.req.UpdateTimingMessageReq;
import com.uio.monitor.controller.resp.TimingMessageDTO;
import com.uio.monitor.service.TimingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
     * @param pageNum 页码可为空
     * @param pageSize 分页大小
     * @param state 推送状态，默认空表示查询所有状态消息
     * @param pushWay 推送途径，默认空，表示查询所有消息
     * @param effective 是否生效，默认空，标识查询生效&不生效的推送消息
     * @return
     */
    @GetMapping("/getTimingMessageList")
    public BackMessage<List<TimingMessageDTO>> getTimingMessageList(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                                                                    @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                    @RequestParam(value = "state", required = false) String state,
                                                                    @RequestParam(value = "pushWay", required = false) String pushWay,
                                                                    @RequestParam(value = "effective", required = false) Boolean effective) {
        return null;
    }
}
