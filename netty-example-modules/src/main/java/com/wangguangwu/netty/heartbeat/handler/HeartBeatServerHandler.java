package com.wangguangwu.netty.heartbeat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳检测服务器处理器
 * 负责处理 IdleStateHandler 触发的空闲状态事件
 *
 * @author wangguangwu
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 每个连接的空闲次数计数器
     */
    private final AtomicInteger idleCounter = new AtomicInteger(0);

    /**
     * 最大允许的空闲次数，超过将断开连接
     */
    private static final int MAX_IDLE_COUNT = 3;

    /**
     * 心跳消息内容
     */
    private static final String HEARTBEAT_MSG = "PING";

    /**
     * 日期格式化
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理用户事件，主要是处理空闲状态事件
     *
     * @param ctx 上下文
     * @param evt 事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断是否是空闲状态事件
        if (evt instanceof IdleStateEvent event) {
            String eventType = getEventTypeString(event.state());
            int count = idleCounter.incrementAndGet();

            String currentTime = sdf.format(new Date());
            System.out.printf("[IDLE] %s - 客户端 %s 发生%s (第%d次/%d)%n",
                    currentTime, ctx.channel().remoteAddress(), eventType, count, MAX_IDLE_COUNT);

            // 如果超过最大空闲次数，关闭连接
            if (count >= MAX_IDLE_COUNT) {
                System.out.printf("[IDLE] 客户端 %s 空闲次数过多，关闭连接%n",
                        ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        } else {
            // 如果不是空闲事件，传递给下一个处理器
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 获取事件类型的字符串描述
     */
    private String getEventTypeString(IdleState state) {
        return switch (state) {
            case READER_IDLE -> "读空闲";
            case WRITER_IDLE -> "写空闲";
            case ALL_IDLE -> "读写空闲";
            default -> "未知空闲";
        };
    }

    /**
     * 处理客户端发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;

        // 检查是否是心跳消息
        if (HEARTBEAT_MSG.equals(message)) {
            // 收到心跳消息，重置空闲计数器
            idleCounter.set(0);
            String currentTime = sdf.format(new Date());
            System.out.printf("[HEARTBEAT] %s - 收到客户端 %s 的心跳消息%n",
                    currentTime, ctx.channel().remoteAddress());
        } else {
            // 如果不是心跳消息，传递给下一个处理器
            System.out.printf("[MESSAGE] 收到客户端 %s 的消息: %s%n",
                    ctx.channel().remoteAddress(), message);
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 连接建立时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("[INFO] 客户端 %s 连接成功%n", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * 连接断开时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.printf("[INFO] 客户端 %s 断开连接%n", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.printf("[ERROR] 客户端 %s 发生异常: %s%n",
                ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }
}
