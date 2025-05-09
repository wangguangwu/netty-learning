package com.wangguangwu.netty.dubborpc.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID生成器
 * 用于生成唯一的请求ID
 *
 * @author wangguangwu
 */
@SuppressWarnings("unused")
public class IdGenerator {

    /**
     * 计数器，用于生成自增ID
     */
    private static final AtomicLong COUNTER = new AtomicLong(0);

    /**
     * 生成基于UUID的唯一ID
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成基于自增的唯一ID
     *
     * @return 自增ID
     */
    public static long generateSequenceId() {
        return COUNTER.incrementAndGet();
    }

    /**
     * 生成基于时间戳的唯一ID
     *
     * @return 时间戳ID
     */
    public static String generateTimestampId() {
        return System.currentTimeMillis() + "-" + generateSequenceId();
    }

    /**
     * 生成默认的唯一ID（当前使用时间戳ID）
     *
     * @return 唯一ID
     */
    public static String generateId() {
        return generateTimestampId();
    }
}
