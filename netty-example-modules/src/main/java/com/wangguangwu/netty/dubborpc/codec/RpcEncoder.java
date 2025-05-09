package com.wangguangwu.netty.dubborpc.codec;

import com.wangguangwu.netty.dubborpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC编码器
 * 将对象编码为字节流，用于网络传输
 * 编码格式：长度(4字节) + 内容(N字节)
 *
 * @author wangguangwu
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    /**
     * 目标类型
     */
    private final Class<?> targetClass;

    /**
     * 序列化器
     */
    private final Serializer serializer;

    /**
     * 构造函数
     *
     * @param targetClass 要编码的对象类型
     * @param serializer  使用的序列化器
     */
    public RpcEncoder(Class<?> targetClass, Serializer serializer) {
        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 检查消息类型是否匹配
        if (targetClass.isInstance(msg)) {
            // 序列化对象为字节数组
            byte[] data = serializer.serialize(msg);
            
            // 写入数据长度
            out.writeInt(data.length);
            
            // 写入数据内容
            out.writeBytes(data);
        }
    }
}
