package com.uio.monitor.pushMessage;

import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.service.impl.WeChatMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author han xun
 * Date 2022/4/5 22:22
 * Description:
 */
@SpringBootTest
public class WechatMessageServiceTest {

    @Autowired
    private WeChatMessageService weChatMessageService;

    @Test
    public void sendMessageToWechat() {

        weChatMessageService.sendMessage("test_sourceId","ziji", "自己",
                PushWayEnum.WECHAT, "即时测试2\\r\\n" +
                        "即时测试2");
    }
}
