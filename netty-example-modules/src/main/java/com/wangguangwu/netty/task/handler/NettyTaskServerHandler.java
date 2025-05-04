package com.wangguangwu.netty.task.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Task 项目专用服务端业务处理器，演示任务队列三大典型场景
 *
 * @author wangguangwu
 */
public class NettyTaskServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 模拟业务线程保存的用户Channel映射
     */
    private static final ConcurrentHashMap<String, Channel> USER_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 读取客户端发送的数据
     * <p>
     * 任务队列三大典型场景演示
     *
     * @param ctx 上下文对象
     * @param msg 消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 1. 用户程序自定义的普通任务（提交到 taskQueue）
        ctx.channel().eventLoop().execute(() -> {
            ctx.writeAndFlush(Unpooled.copiedBuffer("我是普通任务", CharsetUtil.UTF_8));
            System.out.println("普通任务执行");
        });

        // 2. 用户自定义定时任务（提交到 scheduleTaskQueue）
        ctx.channel().eventLoop().schedule(() -> {
            ctx.writeAndFlush(Unpooled.copiedBuffer("我是定时任务", CharsetUtil.UTF_8));
            System.out.println("定时任务执行");
        }, 3, TimeUnit.SECONDS);

        // 3. 非当前 Reactor 线程调用 Channel 的方法（业务线程异步推送消息）
        new Thread(() -> {
            Channel userChannel = USER_CHANNEL_MAP.get("user1");
            if (userChannel != null) {
                userChannel.writeAndFlush(
                        Unpooled.copiedBuffer("我是自定义任务", CharsetUtil.UTF_8)
                );
                System.out.println("自定义任务执行");
            }
        }).start();
    }

    /**
     * 数据读取完毕后触发，向客户端发送响应
     *
     * @param ctx 上下文对象
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // 写入并刷新响应数据
        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello, client, I'm task server.", CharsetUtil.UTF_8));
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

    /**
     * 连接建立时，将用户 Channel 保存到映射表
     *
     * @param ctx 上下文对象
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        USER_CHANNEL_MAP.put("user1", ctx.channel());
    }
}
