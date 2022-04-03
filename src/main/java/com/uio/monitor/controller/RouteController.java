package com.uio.monitor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author han xun
 * Date 2021/10/15 22:47
 * Description:
 */
@RestController
public class RouteController {

    /**
     * 转发内网请求
     *
     * @return
     */
    @RequestMapping("/route")
    public Object route() {
        return null;
    }
}
