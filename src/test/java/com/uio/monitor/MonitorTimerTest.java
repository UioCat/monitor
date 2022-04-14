package com.uio.monitor;

import com.uio.monitor.timer.MonitorTimer;
//import com.uio.monitor.timer.PushCardTimer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author han xun
 * Date 2021/11/27 22:02
 * Description:
 */
@SpringBootTest
@Slf4j
public class MonitorTimerTest {

    @Autowired
    private MonitorTimer monitorTimer;
    @Autowired
    //private PushCardTimer pushCardTimer;

    /**
     * 服务器监控程序单测
     */
    @Test
    public void monitorTimerTest() throws InterruptedException {
        monitorTimer.monitorTimer();
        log.info("monitorTimerTest end");
    }

    @Test
    public void restartTomcatTest() {
//        Vector<String> execute = SSHCommandUtils.execute("192.168.31.102", "root", "waiwai", "/usr/java/apache-tomcat-9.0.33/bin/startup.sh");
       // pushCardTimer.pushCard();
    }
}
