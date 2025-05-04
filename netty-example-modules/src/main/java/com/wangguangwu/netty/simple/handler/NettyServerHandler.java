package com.wangguangwu.netty.simple.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

/**
 * 服务端业务处理器，处理客户端消息和通道事件
 *
 * @author wangguangwu
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取客户端发送的数据
     *
     * @param ctx 上下文对象
     * @param msg 消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 打印当前线程和通道信息
        System.out.println("服务器读取线程 " + Thread.currentThread().getName() + " channel =" + ctx.channel());
        System.out.println("server ctx =" + ctx);
        System.out.println("查看 channel 和 pipeline 的关系");
        // 获取当前通道
        Channel channel = ctx.channel();
        // 获取当前 pipeline
        ChannelPipeline pipeline = ctx.pipeline();
        System.out.println("Channel = " + channel);
        System.out.println("Pipeline = " + pipeline);
        System.out.println("Channel From Pipeline = " + pipeline.channel());
        System.out.println("pipeline.channel() == ctx.channel() ? " + (pipeline.channel() == channel));
        // 将消息转换为 ByteBuf
        ByteBuf buf = (ByteBuf) msg;
        // 打印客户端发送的消息
        System.out.println("客户端发送消息是:" + buf.toString(CharsetUtil.UTF_8));
        // 打印客户端地址
        System.out.println("客户端地址:" + channel.remoteAddress());
    }

    /**
     * 数据读取完毕后触发，向客户端发送响应
     *
     * @param ctx 上下文对象
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 写入并刷新响应数据
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, client, I'm server.", CharsetUtil.UTF_8));
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
