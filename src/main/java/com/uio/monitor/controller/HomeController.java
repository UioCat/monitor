package com.uio.monitor.controller;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.entity.GetHomeDO;
import com.uio.monitor.manager.ConfigManager;
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
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private TuyaIotService tuyaIotService;


    @GetMapping("/powerOff")
    public BackMessage<Void> powerOff(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        tuyaIotService.powerOff();
        return BackMessage.success();
    }

    @GetMapping("/powerOn")
    public BackMessage<Void> powerOn(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        tuyaIotService.powerOff();
        return BackMessage.success();
    }

    /**
     * 到家时间记录
     * @param secretKey
     * @return
     */
    @GetMapping("/arriveHome")
    public BackMessage<Void> arriveHome(@RequestParam String secretKey) {
        if (filterRequest()) {
            super.verifyKey(secretKey);
            GetHomeDO getHomeDO = new GetHomeDO();
            Calendar cal = Calendar.getInstance();
            getHomeDO.setArriveTime(new Date());
            // 判断是否为晚上
            getHomeDO.setNight(cal.get(Calendar.HOUR_OF_DAY) >= MonitorConstant.NIGHT_HOUR);
            getHomeManager.insertOrUpdate(getHomeDO);
            return BackMessage.success();
        }
        return BackMessage.success();
    }

    /**
     * 获取锁成功则表示可以进行操作，否则不操作
     * 30s幂等
     * 等待锁自己过期即可
     * @return
     */
    private Boolean filterRequest() {
        String key = RedisConstant.getArriveHomeFilterLock();
        return cacheService.lock(key, "", "30000", 0);
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
        // 由于苹果快捷指令触发URL会重复触发两次，所以设置30s内幂等
        emailService.sendQuietMessage("406453373@qq.com", "门锁打开通知",
                "门锁已经打开，时间：" + df.format(System.currentTimeMillis()), 30L);
        emailService.sendQuietMessage("478633420@qq.com", "门锁打开通知",
                "门锁已经打开，时间：" + df.format(System.currentTimeMillis()), 30L);
        return BackMessage.success();
    }

    @GetMapping("/getScreen")
    public String getScreen(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        return configManager.queryAndUpdateMacScreenBrightness();
    }

    @GetMapping("/getWifiConnectStatus")
    public String getWifiConnectStatus(@RequestParam String secretKey) {
        super.verifyKey(secretKey);
        return null;
    }
}
