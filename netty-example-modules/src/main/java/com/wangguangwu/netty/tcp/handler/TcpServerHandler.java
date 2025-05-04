package com.wangguangwu.netty.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * TCP 服务器处理器
 * 处理客户端发送的消息并返回响应
 * 
 * @author wangguangwu
 */
public class TcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 接收到的消息计数器
     */
    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // 读取消息内容
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        // 将字节数组转换为字符串
        String message = new String(buffer, StandardCharsets.UTF_8);

        // 打印接收到的消息
        System.out.println("服务器接收到数据: " + message);
        System.out.println("服务器接收到消息计数: " + (++this.count));

        // 服务器回送数据给客户端，回送一个随机 UUID
        ByteBuf responseByteBuf = Unpooled.copiedBuffer(
                UUID.randomUUID() + "\r\n", 
                StandardCharsets.UTF_8
        );
        ctx.writeAndFlush(responseByteBuf);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端 " + ctx.channel().remoteAddress() + " 已连接");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("客户端 " + ctx.channel().remoteAddress() + " 已断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 打印异常信息
        System.err.println("服务器异常: " + cause.getMessage());
        // 关闭连接
        ctx.close();
    }
}
