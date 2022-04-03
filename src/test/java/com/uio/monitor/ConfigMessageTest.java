package com.uio.monitor;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.vo.ServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author han xun
 * Date 2021/11/22 09:51
 * Description: 配置数据测试
 */

@SpringBootTest
@Slf4j
public class ConfigMessageTest {

    @Autowired
    private ConfigManager configManager;

    /**
     * 服务器配置查看
     */
    @Test
    public void getServerListConfigTest() {
        List<ServerMessage> serverList = configManager.getServerList();
        log.info("serverMessage:{}", JSON.toJSONString(serverList));
    }
}
