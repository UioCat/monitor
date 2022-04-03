package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.vo.ServerMessage;
import com.uio.monitor.utils.SSHCommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.Vector;

/**
 * 检测器
 */
@Service
@Slf4j
public class MonitorService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private EmailService emailService;


    private final static String RECEIVE_ADDRESS = "406453373@qq.com";
    private final static String EMAIL_SUBJECT_FOR_MONITOR = "服务器健康检测程序";

    /**
     * 等待Tomcat重启时间
     */
    private final static Long RESTART_WAIT_TIME = 5 * 60 * 1000L;
    /**
     * 操作服务器重启获取锁时间
     */
    private final static Long RESTART_LOCK_TIME = 6 * 60 * 1000L;

    @Async
    public void keepTomcatAlive(ServerMessage serverMessage) {

        if (this.connectServer(serverMessage)) {
            // 服务可响应
            return;
        }
        // tomcat已宕机，需要重启服务
        String requestId = "";
        String lockName = RedisConstant.getServerMonitorLock(serverMessage.getIp());
        try {
            //获取redis同步锁
            requestId = UUID.randomUUID().toString();
            boolean lock = cacheService.lock(lockName, requestId,
                    String.valueOf(RESTART_LOCK_TIME), 1);
            if (lock) {
                log.info("get lock success, lockName:{}, requestId:{}", lockName, requestId);
                // 重启tomcat服务
                this.restart(serverMessage);
            } else {
                log.warn("get lock failed, lockName:{}", lockName);
                return;
            }
        } catch (Throwable t) {
            log.info("restart tomcat failed, happen exception, serverMessage:{}", JSON.toJSONString(serverMessage), t);
        } finally {
            cacheService.unLock(lockName, requestId);
            log.info("unlock:{}, requestId:{}", lockName, requestId);
        }
    }

    /**
     * 连接服务器，并查看是否服务器是否响应
     * @param serverMessage
     * @return
     */
    private boolean connectServer(ServerMessage serverMessage) {
        try {
            String s;
            BufferedReader in;
            System.setProperty("sun.net.client.defaultConnectTimeout", "8000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");
            URL url = new URL("http://" + serverMessage.getIp() + ":" + serverMessage.getPort());
            java.net.URLConnection con = url.openConnection();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            con.setConnectTimeout(1000);
            con.setReadTimeout(4000);
            while ((s = in.readLine()) != null) {
                if (s.length() > 0) {
                    //accessed page successful.
                    log.info("server healthy, serverMessage:{}", JSON.toJSONString(serverMessage));
                    in.close();
                    return true;
                }
            }
            in.close();
            return false;
        } catch (Throwable t) {
            log.warn("server down, serviceMessage:{}, ", JSON.toJSONString(serverMessage), t);
            return false;
        }
    }

    /**
     * 重启tomcat
     *
     * @param serverMessage
     * @throws Exception
     */
    private void restart(ServerMessage serverMessage) throws Exception {
        log.info("try to restart server");
        StringBuilder sendMessage = new StringBuilder("检测到服务器异常，尝试重启\n");
        Vector<String> execute = SSHCommandUtils.execute(serverMessage.getIp(), serverMessage.getUserName(),
                serverMessage.getPassword(), "ps -ef | grep tomcat |grep -v 'grep' | awk '{print $2}'");
        if (execute.size() > 0) {
            //tomcat已宕机但进程任存在，需先杀死进程
            this.stopTomcat(serverMessage, execute.get(0));
        }
        // 启动tomcat
        if (this.startTomcat(serverMessage)) {
            // 执行tomcat重启命令成功
            // 等待tomcat启动
            Thread.sleep(RESTART_WAIT_TIME);
            // 再校验是否重启成功
            if (this.connectServer(serverMessage)) {
                log.info("restart tomcat success, serverMessage:{}", JSON.toJSONString(serverMessage));
                sendMessage.append("restart server success");
            } else {
                log.info("restart tomcat fail, serverMessage:{}", JSON.toJSONString(serverMessage));
                sendMessage.append("restart server failed");
            }
            sendMessage.append("serverMessage:").append(JSON.toJSONString(serverMessage));
            emailService.sendNonRepeatMessage(RECEIVE_ADDRESS, EMAIL_SUBJECT_FOR_MONITOR, sendMessage.toString());
        }
    }

    /**
     * 停止tomcat
     * @param serverMessage
     * @param pid
     */
    private void stopTomcat(ServerMessage serverMessage, String pid) {
        try {
            log.info("<" + new Date() + "> Tomcat is alive but not response!");
            // kill tomcat dead progress
            SSHCommandUtils.execute(serverMessage.getIp(), serverMessage.getUserName(),
                    serverMessage.getPassword(), "kill -9 " + pid);
            log.info("stop tomcat success, serverMessage:{}", JSON.toJSONString(serverMessage));
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("stop tomcat fail");
        }
    }

    /**
     * 启动tomcat
     * @param serverMessage
     * @return
     */
    private boolean startTomcat(ServerMessage serverMessage) {
        try {
            // 执行 启动tomcat 命令
            SSHCommandUtils.execute(serverMessage.getIp(), serverMessage.getUserName(),
                    serverMessage.getPassword(), serverMessage.getRoute());
            log.info("execute start tomcat, serverMessage:{}", JSON.toJSONString(serverMessage));
        } catch (Throwable t) {
            log.info("start tomcat failed, ", t);
            emailService.sendNonRepeatMessage(RECEIVE_ADDRESS, EMAIL_SUBJECT_FOR_MONITOR,
                    "SSH连接执行命令时异常, serverMessage" + JSON.toJSONString(serverMessage) + t);
            return false;
        }
        return true;
    }

}
