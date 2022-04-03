package com.uio.monitor.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provide interface to execute command on remote Linux.
 */
@Slf4j
public class SSHCommandUtils {

    public static final int DEFAULT_SSH_PORT = 22;

    public static Vector<String> execute(String host, String username, String password, final String command) {
        int returnCode = 0;
        JSch jsch = new JSch();
        MyUserInfo userInfo = new MyUserInfo();

        Vector<String> stdout = new Vector<String>();
        try {
            // Create and connect session.
            Session session = jsch.getSession(username, host, DEFAULT_SSH_PORT);
            session.setPassword(password);
            session.setUserInfo(userInfo);
            session.connect();
            // Create and connect channel.
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader(channel
                    .getInputStream()));
            channel.connect();
            log.info("The remote command is: " + command);

            // Get the output of remote command.
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            log.info("ssh execute result: " + stdout.toString());
            input.close();

            // Get the return code only after the channel is closed.
            if (channel.isClosed()) {
                returnCode = channel.getExitStatus();
            }

            // Disconnect the channel and session.
            channel.disconnect();
            session.disconnect();
        } catch (JSchException e) {
            log.warn("host:{}, password:{}, username:{}, command:{}, JSchException,",
                    host, password, username, command, e);
        } catch (Throwable t) {
            log.error("host:{}, password:{}, username:{}, command:{}, execute ssh command error, ",
                    host, password, username, command, t);
        }
        return stdout;
    }
}

/**
 * 用于存储SSH登录态
 */
class MyUserInfo implements UserInfo {

    private String password;

    private String passphrase;

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean promptPassphrase(final String arg0) {
        return false;
    }

    @Override
    public boolean promptPassword(final String arg0) {
        return false;
    }

    @Override
    public boolean promptYesNo(final String arg0) {
        if (arg0.contains("The authenticity of host")) {
            return true;
        }
        return false;
    }

    @Override
    public void showMessage(final String arg0) {
    }
}