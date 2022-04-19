package com.uio.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.BackMessage;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.controller.req.LoginReq;
import com.uio.monitor.controller.req.RegisterReq;
import com.uio.monitor.controller.resp.UserInfoDTO;
import com.uio.monitor.entity.UserDO;
import com.uio.monitor.manager.UserManager;
import com.uio.monitor.utils.TokenUtils;
import com.uio.monitor.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author han xun
 * Date 2022/1/2 12:57
 * Description:
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserManager userManager;

    @Value("${token.secret}")
    private String tokenSecret;

    @PostMapping("/login")
    public BackMessage<String> login(@RequestBody @Valid LoginReq loginReq) {
        UserDO userDO = userManager.queryByAccount(loginReq.getAccount());
        if (userDO == null) {
            throw new CustomException(BackEnum.NO_USER);
        }
        if (userDO.getPassword().equals(Utils.getMD5Str(loginReq.getPassword()))) {
            return BackMessage.success(TokenUtils.getToken(userDO.getId(), tokenSecret));
        }
        throw new CustomException(BackEnum.PWD_ERROR);
    }

    @PostMapping("/register")
    public BackMessage<Boolean> register(@RequestBody @Valid RegisterReq registerReq) {
        if (userManager.queryByAccount(registerReq.getAccount()) != null) {
            log.warn("account exist, req:{}", JSON.toJSONString(registerReq));
            throw new CustomException(BackEnum.ACCOUNT_EXIST);
        }
        // FIXME 先写死，后期修改
//        if (!"406453373".equals(registerReq.getVerifyCode())) {
//            throw new CustomException(BackEnum.UNAUTHORIZED_REGISTER);
//        }
        UserDO userDO = new UserDO();
        userDO.setCreator(registerReq.getAccount());
        userDO.setModifier(registerReq.getAccount());
        userDO.setAccount(registerReq.getAccount());
        userDO.setPassword(Utils.getMD5Str(registerReq.getPassword()));
        userDO.setNickName(registerReq.getUsername());
        userManager.insert(userDO);
        return BackMessage.success(true);
    }

    @GetMapping("/getInfo")
    public BackMessage<UserInfoDTO> getInfo() {
        Long userId = super.getUserId();
        UserDO userDO = userManager.queryById(userId);
        if (userDO == null) {
            return BackMessage.success(null);
        }
        return BackMessage.success(this.convertUserInfoDTO(userDO));
    }

    private UserInfoDTO convertUserInfoDTO(UserDO userDO) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUsername(userDO.getNickName());
        userInfoDTO.setHeadImage(userDO.getHeadImageUrl());
        return userInfoDTO;
    }
}
