package com.uio.monitor.controller;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.entity.GetHomeDO;
import com.uio.monitor.manager.GetHomeManager;
import com.uio.monitor.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author han xun
 * Date 2021/10/12 22:26
 * Description: 家庭智能化数据检测
 */
@RestController
@RequestMapping("/home")
@Slf4j
public class HomeController extends BaseController {

    @Autowired
    private GetHomeManager getHomeManager;
    @Autowired
    private EmailService emailService;

    /**
     * 到家时间记录
     * @param secretKey
     * @return
     */
    @GetMapping("/arriveHome")
    public BackMessage<Void> arriveHome(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        GetHomeDO getHomeDO = new GetHomeDO();
        Calendar cal = Calendar.getInstance();
        getHomeDO.setArriveTime(new Date());
        // 判断是否为晚上
        getHomeDO.setNight(cal.get(Calendar.HOUR_OF_DAY) >= MonitorConstant.NIGHT_HOUR);
        getHomeManager.insertOrUpdate(getHomeDO);
        return BackMessage.success();
    }

    /**
     * 门锁打开邮箱通知
     * 防止homekit通知出问题
     * @param secretKey
     * @return
     */
    @GetMapping("/unlocking")
    public BackMessage<Void> unlocking(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 由于苹果快捷指令触发URL会重复触发两次，所以设置1s内幂等
        emailService.sendNonRepeatMessage("406453373@qq.com", "门锁打开通知",
                "门锁已经打开，时间：" + df.format(System.currentTimeMillis()), 1L);
        return BackMessage.success();
    }
}
