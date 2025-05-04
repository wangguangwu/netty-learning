package com.wangguangwu.netty.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

/**
 * TCP 客户端处理器
 * 负责发送消息到服务器并处理服务器响应
 *
 * @author wangguangwu
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * 接收到的消息计数器
     */
    private int count;

    /**
     * 当通道就绪时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端连接到服务器: " + ctx.channel().remoteAddress());
        System.out.println("开始发送消息...");

        // 使用客户端发送10条数据 hello,server 编号
        for (int i = 0; i < 10; ++i) {
            String message = "hello,server " + i;
            ByteBuf buffer = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
            ctx.writeAndFlush(buffer);
            System.out.println("客户端发送数据: " + message);
        }
    }

    /**
     * 当通道有读取事件时触发
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        // 读取服务器发送的数据
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        // 将字节数组转换为字符串
        String message = new String(buffer, StandardCharsets.UTF_8);

        // 打印接收到的消息
        System.out.println("客户端接收到消息: " + message);
        System.out.println("客户端接收消息计数: " + (++this.count));
    }

    /**
     * 当通道不活跃时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("与服务器断开连接: " + ctx.channel().remoteAddress());
    }

    /**
     * 当出现异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 打印异常信息
        System.err.println("客户端异常: " + cause.getMessage());
        // 关闭连接
        ctx.close();
    }
}
