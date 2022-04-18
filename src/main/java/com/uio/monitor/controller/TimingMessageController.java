package com.uio.monitor.controller;

import com.github.pagehelper.PageInfo;
import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.controller.req.UpdateTimingMessageReq;
import com.uio.monitor.controller.resp.TimingMessageDTO;
import com.uio.monitor.service.TimingMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author han xun
 * Date 2022/4/3 22:31
 * Description: 定时消息
 */
@RestController
@RequestMapping("/message")
public class TimingMessageController extends BaseController {

    @Autowired
    private TimingMessageService timingMessageService;

    /**
     * 新增一个定时消息
     * @return
     */
    @PostMapping("/addTimingMessage")
    public BackMessage<Boolean> addTimingMessage(@RequestBody @Valid AddTimingMessageReq addTimingMessageReq) {
        Boolean res = timingMessageService.addTimingMessage(super.getUserId(), addTimingMessageReq);
        return BackMessage.success(res);
    }

    /**
     * 修改/删除定时任务消息
     * @return
     */
    @PostMapping("/updateTimingMessage")
    public BackMessage<Boolean> updateTimingMessage(@RequestBody @Valid UpdateTimingMessageReq updateTimingMessageReq) {
        Boolean res = timingMessageService.updateTimingMessage(super.getUserId(), updateTimingMessageReq);
        return BackMessage.success(res);
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
    public BackMessage<PageInfo<TimingMessageDTO>> getTimingMessageList(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "pushWay", required = false) String pushWay,
            @RequestParam(value = "effective", required = false) Boolean effective) {
        // 入参处理
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        PushStateEnum pushStateEnum = StringUtils.isEmpty(state) ? null : PushStateEnum.getByName(state);
        PushWayEnum pushWayEnum = StringUtils.isEmpty(pushWay) ? null : PushWayEnum.getByName(pushWay);
        PageInfo<TimingMessageDTO> timingMessageList = timingMessageService.getTimingMessageList
                (super.getUserId(), pageNum, pageSize, pushStateEnum, pushWayEnum, effective);
        return BackMessage.success(timingMessageList);
    }
}