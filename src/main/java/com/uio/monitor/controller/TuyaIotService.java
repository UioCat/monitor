package com.uio.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.CustomException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 涂鸦智能空调红外遥控接入
 * https://developer.tuya.com/cn/docs/iot/singnature?id=Ka43a5mtx1gsc#title-16-%E5%A6%82%E4%BD%95%E9%AA%8C%E8%AF%81%E5%8A%A0%E5%AF%86%E5%90%8E%E7%9A%84%E7%AD%BE%E5%90%8D%EF%BC%9F
 */
@Service
@Slf4j
public class TuyaIotService {
    // 开发者accessId
    @Value("${config.tuyaAccessId}")
    private String tuyaAccessId;

    // 开发者accessKey
    @Value("${config.tuyaAccessKey}")
    private String tuyaAccessKey;

    // Tuya云endpoint
    @Value("${config.tuyaEndpoint}")
    private String tuyaEndpoint;

    @Value("${config.tuyaDeviceId}")
    private String deviceId;

    @Value("${config.tokenPath}")
    private String tokenPath;

    private static final String powerOff = "{\"commands\":[{\"code\":\"PowerOff\",\"value\":\"PowerOff\"}]}";
    private static final String powerOn = "{\"commands\":[{\"code\":\"PowerOn\",\"value\":\"PowerOn\"}]}";

    public void powerOff() {
        powerControll(powerOff);
    }

    public void powerOn() {
        powerControll(powerOn);
    }

    private void powerControll(String powerOn) {
        // todo 优化结构和日志
        Object result = this.getToken(tokenPath, "GET", "", new HashMap<>());
        log.info("get token success, result: {}", JSON.toJSONString(result));

        String accessToken = "";
        try {
            JSONObject tokenRepJson = JSONObject.parseObject(JSON.toJSONString(result));
            JSONObject tokenResultJson = JSONObject.parseObject(tokenRepJson.get("result").toString());
            accessToken = tokenResultJson.get("access_token").toString();
        } catch (Exception e){
            log.error("error parsing json data!", e);
            throw new CustomException(BackEnum.UNKNOWN_ERROR);
        }

        String commandPath = "/v1.0/devices/" + deviceId + "/commands";
        result = this.execute(accessToken, commandPath, "POST", powerOn, new HashMap<>());
        log.info("powerControll send success, result: {}", JSON.toJSONString(result));
    }

    private static final MediaType CONTENT_TYPE = MediaType.parse("application/json");
    private static final String EMPTY_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String SING_HEADER_NAME = "Signature-Headers";
    private static final String NONE_STRING = "";

    /**
     * 用于获取令牌、刷新令牌：无Token请求
     */
    public Object getToken(String path, String method, String body, Map<String, String> customHeaders) {
        return this.execute("", path, method, body, customHeaders);
    }

    /**
     * 用于业务接口：携带Token请求
     */
    public Object execute(String accessToken, String path, String method, String body, Map<String, String> customHeaders) {
        try {
            String url = tuyaEndpoint + path;

            Request.Builder request;
            request = createRequest(url, body, method);
            if (customHeaders.isEmpty()) {
                customHeaders = new HashMap<>();
            }
            Headers headers = getHeader(accessToken, request.build(), body, customHeaders);
            request.headers(headers);
            request.url(tuyaEndpoint + getPathAndSortParam(new URL(url)));
            Response response = doRequest(request.build());
            return JSON.parseObject(response.body().string(), Object.class);
        } catch (Exception e) {
            throw new TuyaCloudSDKException(e.getMessage());
        }
    }

    /**
     * 生成header
     *
     * @param accessToken 是否需要携带token
     * @param headerMap   自定义header
     */
    public Headers getHeader(String accessToken, Request request, String body, Map<String, String> headerMap) throws Exception {
        Headers.Builder hb = new Headers.Builder();

        Map<String, String> flattenHeaders = flattenHeaders(headerMap);
        String t = flattenHeaders.get("t");
        if (StringUtils.isBlank(t)) {
            t = System.currentTimeMillis() + "";
        }

        hb.add("client_id", tuyaAccessId);
        hb.add("t", t);
        hb.add("sign_method", "HMAC-SHA256");
        hb.add("lang", "zh");
        hb.add(SING_HEADER_NAME, flattenHeaders.getOrDefault(SING_HEADER_NAME, ""));
        String nonceStr = flattenHeaders.getOrDefault("nonce", "");
        hb.add("nonce", flattenHeaders.getOrDefault("nonce", ""));
        String stringToSign = stringToSign(request, body, flattenHeaders);
        if (StringUtils.isNotBlank(accessToken)) {
            hb.add("access_token", accessToken);
            hb.add("sign", sign(tuyaAccessId, tuyaAccessKey, t, accessToken, nonceStr, stringToSign));
        } else {
            hb.add("sign", sign(tuyaAccessId, tuyaAccessKey, t, nonceStr, stringToSign));
        }
        return hb.build();
    }

    public static String getPathAndSortParam(URL url) {
        try {
            // supported the query contains zh-Han char
            String query = URLDecoder.decode(url.getQuery(), "UTF-8");
            String path = url.getPath();
            if (StringUtils.isBlank(query)) {
                return path;
            }
            Map<String, String> kvMap = new TreeMap<>();
            String[] kvs = query.split("\\&");
            for (String kv : kvs) {
                String[] kvArr = kv.split("=");
                if (kvArr.length > 1) {
                    kvMap.put(kvArr[0], kvArr[1]);
                } else {
                    kvMap.put(kvArr[0], "");
                }
            }
            return path + "?" + kvMap.entrySet().stream().map(it -> it.getKey() + "=" + it.getValue())
                    .collect(Collectors.joining("&"));
        } catch (Exception e) {
            return url.getPath();
        }
    }

    private static String stringToSign(Request request, String body, Map<String, String> headers) throws Exception {
        List<String> lines = new ArrayList<>(16);
        lines.add(request.method().toUpperCase());
        String bodyHash = EMPTY_HASH;
        if (request.body() != null && request.body().contentLength() > 0) {
            bodyHash = Sha256Util.encryption(body);
        }
        String signHeaders = headers.get(SING_HEADER_NAME);
        String headerLine = "";
        if (signHeaders != null) {
            String[] sighHeaderNames = signHeaders.split("\\s*:\\s*");
            headerLine = Arrays.stream(sighHeaderNames).map(String::trim)
                    .filter(it -> it.length() > 0)
                    .map(it -> it + ":" + headers.get(it))
                    .collect(Collectors.joining("\n"));
        }
        lines.add(bodyHash);
        lines.add(headerLine);
        String paramSortedPath = getPathAndSortParam(request.url().url());
        lines.add(paramSortedPath);
        return String.join("\n", lines);
    }

    private static Map<String, String> flattenHeaders(Map<String, String> headers) {
        Map<String, String> newHeaders = new HashMap<>();
        headers.forEach((name, values) -> {
            if (values == null || values.isEmpty()) {
                newHeaders.put(name, "");
            } else {
                newHeaders.put(name, values);
            }
        });
        return newHeaders;
    }

    /**
     * 计算sign
     */
    private static String sign(String accessId, String secret, String t, String accessToken, String nonce, String stringToSign) {
        StringBuilder sb = new StringBuilder();
        sb.append(accessId);
        if (StringUtils.isNotBlank(accessToken)) {
            sb.append(accessToken);
        }
        sb.append(t);
        if (StringUtils.isNotBlank(nonce)) {
            sb.append(nonce);
        }
        sb.append(stringToSign);
        System.out.println(sb.toString());
        return Sha256Util.sha256HMAC(sb.toString(), secret);
    }

    private static String sign(String accessId, String secret, String t, String nonce, String stringToSign) {
        return sign(accessId, secret, t, NONE_STRING, nonce, stringToSign);
    }

    /**
     * 创建请求
     */
    public static Request.Builder createRequest(String url, String body, String method) {
        Request.Builder request;
        try {
            request = new Request.Builder()
                    .url(url);
            if ("GET".equals(method)) {
                request = request.get();
            } else if ("POST".equals(method)) {
                request = request.post(RequestBody.create(CONTENT_TYPE, body));
            } else if ("PUT".equals(method)) {
                request = request.put(RequestBody.create(CONTENT_TYPE, body));
            } else if ("DELETE".equals(method)) {
                request = request.delete(RequestBody.create(CONTENT_TYPE, body));
            } else {
                throw new TuyaCloudSDKException("Method only support GET, POST, PUT, DELETE");
            }
        } catch (IllegalArgumentException e) {
            throw new TuyaCloudSDKException(e.getMessage());
        }
        return request;
    }

    /**
     * 执行请求
     */
    public static Response doRequest(Request request) {
        Response response;
        try {
            response = new OkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            throw new TuyaCloudSDKException(e.getMessage());
        }
        return response;
    }


    static class Sha256Util {

        public static String encryption(String str) throws Exception {
            return encryption(str.getBytes(StandardCharsets.UTF_8));
        }

        public static String encryption(byte[] buf) throws Exception {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(buf);
            return byte2Hex(messageDigest.digest());
        }

        private static String byte2Hex(byte[] bytes) {
            StringBuilder stringBuffer = new StringBuilder();
            String temp;
            for (byte aByte : bytes) {
                temp = Integer.toHexString(aByte & 0xFF);
                if (temp.length() == 1) {
                    stringBuffer.append("0");
                }
                stringBuffer.append(temp);
            }
            return stringBuffer.toString();
        }

        public static String sha256HMAC(String content, String secret) {
            Mac sha256HMAC = null;
            try {
                sha256HMAC = Mac.getInstance("HmacSHA256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            try {
                sha256HMAC.init(secretKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            byte[] digest = sha256HMAC.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return new HexBinaryAdapter().marshal(digest).toUpperCase();
        }
    }


    static class TuyaCloudSDKException extends RuntimeException {

        private Integer code;

        public TuyaCloudSDKException(String message) {
            super(message);
        }

        public TuyaCloudSDKException(Integer code, String message) {
            super(message);
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        @Override
        public String toString() {
            if (code != null) {
                return "TuyaCloudSDKException: " +
                        "[" + code + "] " + getMessage();
            }

            return "TuyaCloudSDKException: " + getMessage();
        }
    }

}

