package com.wangguangwu.iomodel.common;

/**
 * 简单日志工具类，统一格式输出。
 *
 * @author wangguangwu
 */
public class LoggerUtil {

    public static void info(String msg) {
        System.out.println("[INFO] " + msg);
    }

    public static void error(String msg) {
        System.err.println("[ERROR] " + msg);
    }

    public static void warn(String msg) {
        System.out.println("[WARN] " + msg);
    }
}
