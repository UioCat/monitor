package com.uio.monitor.controller.base;

import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author han xun
 * Date 2021/10/12 22:24
 * Description:
 */
@Component
public class BaseController {

    @Value("${config.secretKey:}")
    private String secretKey;

    /**
     * 验证身份
     * @param key
     */
    protected void verifyKey(String key) {
        if (!secretKey.equals(key)) {
            throw new CustomException(BackEnum.PWD_ERROR);
        }
    }

    /**
     * 获取用户ID
     * @return
     */
    public Long getUserId() {
        UserToken currentUser = ThreadLocalUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomException(BackEnum.UNAUTHORIZED);
        }
        return currentUser.getId();
    }
}
