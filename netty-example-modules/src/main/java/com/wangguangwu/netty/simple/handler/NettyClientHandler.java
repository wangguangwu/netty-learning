package com.wangguangwu.netty.simple.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 客户端业务处理器，处理通道事件和消息收发
 *
 * @author wangguangwu
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 通道就绪时触发，向服务器发送消息
     *
     * @param ctx 上下文对象
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 打印当前客户端连接信息
        System.out.println("client " + ctx);
        // 发送消息到服务器
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, server, I'm client.", CharsetUtil.UTF_8));
    }

    /**
     * 通道有读取事件时触发，处理服务器返回的数据
     *
     * @param ctx 上下文对象
     * @param msg 消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将消息转换为 ByteBuf
        ByteBuf buf = (ByteBuf) msg;
        // 打印服务器返回的消息
        System.out.println("服务器回复的消息:" + buf.toString(CharsetUtil.UTF_8));
        // 打印服务器地址
        System.out.println("服务器的地址： " + ctx.channel().remoteAddress());
    }

    /**
     * 处理异常，关闭通道
     *
     * @param ctx   上下文对象
     * @param cause 异常信息
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 关闭通道
        ctx.close();
    }
}
