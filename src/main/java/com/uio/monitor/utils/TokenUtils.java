package com.uio.monitor.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author han xun
 * Date 2021-10-08 14:42
 * Description token工具类
 */
@Slf4j
public class TokenUtils {


    /**
     * 设置过期时间
     */
    private static final long EXPIRE_DATE = 24 * 60 * 100000;
    /**
     * 字段名
     */
    private static final String FIELD_NAME = "ID";

    public static String getToken(Long id, String tokenSecret) {
        String token = "";
        try {
            //过期时间
            Date date = new Date(System.currentTimeMillis() + EXPIRE_DATE);
            //秘钥及加密算法
            Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
            //设置头部信息
            Map<String, Object> header = new HashMap<>();
            header.put("typ", "JWT");
            header.put("alg", "HS256");
            //携带username，password信息，生成签名
            token = JWT.create()
                    .withHeader(header)
                    .withClaim(FIELD_NAME, id)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            log.warn("get token fail, id:{}", id, e);
            return null;
        }
        return token;
    }

    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public static Long getIdAndVerify(String token, String tokenSecret) {
        if (StringUtils.isEmpty(token)) {
            throw new CustomException(BackEnum.UNAUTHORIZED);
        }
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = null;
        try {
            jwt = verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("verify token fail, token:{}", token, e);
            throw new CustomException(BackEnum.UNAUTHORIZED);
        }
        return jwt.getClaim(FIELD_NAME).asLong();
    }
}
