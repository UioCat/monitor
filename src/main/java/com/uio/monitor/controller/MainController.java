package com.uio.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author han xun
 * Date 2021-10-14 09:56
 * Description: 健康监测
 */
@Controller
public class MainController {

    @GetMapping("/check")
    public @ResponseBody String check() {
        return "success";
    }
}
