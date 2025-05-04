package com.wangguangwu.netty.protocoltcp.codec;

import com.wangguangwu.netty.protocoltcp.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息编码器
 * 将自定义消息对象编码为字节流，用于网络传输
 *
 * @author wangguangwu
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        System.out.println("MessageEncoder: 编码消息 - " + msg);
        
        // 1. 写入消息长度
        out.writeInt(msg.getLength());
        
        // 2. 写入消息内容
        out.writeBytes(msg.getContent());
    }
}
