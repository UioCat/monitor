package com.uio.monitor.service;

import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private CacheService cacheService;

    /**
     * 默认重复时间 1h
     */
    private static final Long DEFAULT_REPEAT_TIME = 60 * 60L;

    /**
     * 发送安静消息redis前缀
     */
    private static final String SEND_QUIT_MESSAGE_PREFIX = "send_quit_message";

    /**
     * 发送短时间不重复的mail 1小时
     */
    public void sendNonRepeatMessage(String to, String subject, String text) {
        this.sendNonRepeatMessage(to, subject, text, DEFAULT_REPEAT_TIME);
    }

    /**
     * 发送短时间不重复的mail
     *
     * @param to      邮件发送去向
     * @param subject 主题
     * @param text    正文
     * @param time    不重复发送设置时间/秒
     */
    public void sendNonRepeatMessage(String to, String subject, String text, Long time) {
        String md5Str = CommonUtils.md5Utils(to + subject + text);

        if (!cacheService.hasKey(md5Str)) {
            log.info("send mail success, to:{}, subject:{}, text:{}, time:{}, md5Str:{}", to, subject, text, time, md5Str);
            cacheService.put(md5Str, 0, time);
            sendSimpleMessage(to, subject, text);
            return;
        }
        log.warn("send mail failed, request need non repeat, to:{}, subject:{}, text:{}, time:{}, md5Str:{}", to, subject, text, time, md5Str);
    }

    /**
     * 发送消息，一段时间不打扰
     * @param to
     * @param subject
     * @param text
     * @param time s
     */
    public void sendQuietMessage(String to, String subject, String text, Long time) {
        String redisKey = SEND_QUIT_MESSAGE_PREFIX + "_" + to;
        if (!cacheService.hasKey(redisKey)) {
            log.info("send mail success, to:{}, subject:{}, text:{}", to, subject, text);
            cacheService.put(redisKey, 0, time);
            sendSimpleMessage(to, subject, text);
            return;
        }
        log.warn("send mail failed, request need non repeat in {}s, to:{}, subject:{}, text:{}, key:{}",
                time, to, subject, text, redisKey);
    }

    /**
     * 发送邮件方法
     *
     * @param to      邮件发送去向
     * @param subject 主题
     * @param text    正文
     */
    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(MonitorConstant.MY_EMAIL);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}