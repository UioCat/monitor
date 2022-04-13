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
    public void sendMessage() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "你好 -- from Java");
        jsonObject.put("verifyCode", "");

        weChatMessageService.sendMessage("test_hanxun", "读书角 - 微信分角", PushWayEnum.WECHAT,
                jsonObject.toJSONString());
    }
}
