package com.wangguangwu.netty.dubborpc.codec;

import com.wangguangwu.netty.dubborpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
 * 将字节流解码为对象，用于网络接收
 * 解码格式：长度(4字节) + 内容(N字节)
 *
 * @author wangguangwu
 */
public class RpcDecoder extends ByteToMessageDecoder {

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
     * @param targetClass 目标类型
     * @param serializer  序列化器
     */
    public RpcDecoder(Class<?> targetClass, Serializer serializer) {
        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读字节数小于4，则不足以读取长度字段
        if (in.readableBytes() < 4) {
            return;
        }

        // 标记当前读取位置
        in.markReaderIndex();

        // 读取消息长度
        int dataLength = in.readInt();

        // 如果数据长度小于0，则关闭连接
        if (dataLength < 0) {
            ctx.close();
            return;
        }

        // 如果可读字节数小于消息长度，则重置读取位置，等待更多数据
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息内容
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 反序列化为对象
        Object obj = serializer.deserialize(data, targetClass);
        out.add(obj);
    }
}
