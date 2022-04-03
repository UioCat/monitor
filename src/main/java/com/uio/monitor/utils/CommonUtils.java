package com.uio.monitor.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author han xun
 * Date 2021/11/21 19:33
 * Description:
 */
@Slf4j
public class CommonUtils {

    private static String hexStr = "0123456789ABCDEF";

    public static String md5Utils(String text) {
        return bin2HexStr(DigestUtils.md5Digest(text.getBytes(StandardCharsets.UTF_8)));
    }


    /**
     * @param bytes
     * @return 将二进制数组转换为十六进制字符串  2-16
     */
    public static String bin2HexStr(byte[] bytes) {

        StringBuilder result = new StringBuilder();
        String hex = "";
        for (byte aByte : bytes) {
            //字节高4位
            hex = String.valueOf(hexStr.charAt((aByte & 0xF0) >> 4));
            //字节低4位
            hex += String.valueOf(hexStr.charAt(aByte & 0x0F));
            result.append(hex);
        }
        return result.toString();
    }
}
