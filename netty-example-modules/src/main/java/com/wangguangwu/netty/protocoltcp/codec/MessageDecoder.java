package com.wangguangwu.netty.protocoltcp.codec;

import com.wangguangwu.netty.protocoltcp.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 消息解码器
 * 将字节流解码为自定义消息对象，解决TCP粘包/拆包问题
 * 使用 ReplayingDecoder 简化解码逻辑
 *
 * @author wangguangwu
 */
public class MessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MessageDecoder: 解码消息");

        // 1. 读取消息长度
        int length = in.readInt();

        // 2. 读取消息内容
        byte[] content = new byte[length];
        in.readBytes(content);

        // 3. 封装成 Message 对象
        Message message = new Message();
        message.setLength(length);
        message.setContent(content);

        // 4. 将解码后的消息添加到输出列表，传递给下一个处理器
        out.add(message);

        System.out.println("MessageDecoder: 解码完成 - " + message);
    }
}
