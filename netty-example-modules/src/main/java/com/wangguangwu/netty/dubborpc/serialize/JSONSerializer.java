package com.wangguangwu.netty.dubborpc.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON序列化器
 * 使用FastJSON实现对象的序列化和反序列化
 *
 * @author wangguangwu
 */
public class JSONSerializer implements Serializer {

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    @Override
    public byte[] serialize(Object obj) {
        // 使用FastJSON将对象转换为JSON字符串，并处理循环引用
        return JSON.toJSONBytes(obj, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 要反序列化的字节数组
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化后的对象
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // 使用FastJSON将JSON字符串转换为对象
        return JSON.parseObject(bytes, clazz);
    }
}
