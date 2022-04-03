package com.uio.monitor.utils;

import com.uio.monitor.controller.base.UserToken;

/**
 * @author han xun
 * Date 2021/10/14 22:52
 * Description: ThreadLocal工具类
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<UserToken> userThreadLocal = new ThreadLocal<UserToken>();

    /**
     * 添加当前登录用户方法  在拦截器方法执行前调用设置获取用户
     * @param user
     */
    public static void addCurrentUser(UserToken user) {
        userThreadLocal.set(user);
    }

    /**
     * 获取当前登录用户方法
     */
    public static UserToken getCurrentUser() {
        return userThreadLocal.get();
    }

    /**
     * 删除当前登录用户方法  在拦截器方法执行后 移除当前用户对象
     */
    public static void removeCurrentUser() {
        userThreadLocal.remove();
    }
}