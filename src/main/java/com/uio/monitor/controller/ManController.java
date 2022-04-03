package com.uio.monitor.controller;

import com.uio.monitor.common.BackMessage;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.timer.PushCardTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author han xun
 * Date 2021/8/7 00:58
 * Description: 手工控制器
 */
@RestController
public class ManController extends BaseController {

    @Autowired
    private PushCardTimer pushCardTimer;

    @GetMapping("/punchCard")
    public BackMessage<Void> punchCard(@RequestParam("secretKey") String secretKey) {
        super.verifyKey(secretKey);
        pushCardTimer.pushCard();
        return BackMessage.success();
    }
}
