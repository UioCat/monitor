package com.uio.monitor.controller;
import java.util.Calendar;
import java.util.Date;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.entity.GetHomeDO;
import com.uio.monitor.manager.GetHomeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author han xun
 * Date 2021/10/12 22:26
 * Description:
 */
@RestController
@RequestMapping("/home")
@Slf4j
public class HomeController extends BaseController {

    @Autowired
    private GetHomeManager getHomeManager;

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
}
