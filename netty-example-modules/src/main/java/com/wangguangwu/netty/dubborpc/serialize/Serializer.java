package com.wangguangwu.netty.dubborpc.serialize;

/**
 * 序列化器接口
 * 定义了对象序列化和反序列化的方法
 *
 * @author wangguangwu
 */
public interface Serializer {
    
    /**
     * 将对象序列化为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 要反序列化的字节数组
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
