package com.wangguangwu.netty.heartbeat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 心跳检测客户端处理器
 * 负责发送心跳包和处理服务器响应
 * 只发送3次心跳后停止，等待服务端踢出
 *
 * @author wangguangwu
 */
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 心跳消息内容
     */
    private static final String HEARTBEAT_MSG = "PING";

    /**
     * 心跳次数计数器
     */
    private int heartbeatCount = 0;

    /**
     * 最大心跳次数，达到后停止发送
     */
    private static final int MAX_HEARTBEAT_COUNT = 3;

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
        if (evt instanceof IdleStateEvent event) {
            // 只处理写空闲事件
            if (event.state() == IdleState.WRITER_IDLE) {
                heartbeatCount++;
                String currentTime = sdf.format(new Date());

                // 检查是否达到最大心跳次数
                if (heartbeatCount <= MAX_HEARTBEAT_COUNT) {
                    // 发送心跳消息
                    ctx.writeAndFlush(HEARTBEAT_MSG);

                    System.out.println(String.format("[HEARTBEAT] %s - 发送第 %d/%d 次心跳",
                            currentTime, heartbeatCount, MAX_HEARTBEAT_COUNT));

                    // 如果是最后一次心跳，提示用户
                    if (heartbeatCount == MAX_HEARTBEAT_COUNT) {
                        System.out.println("[INFO] 已达到最大心跳次数，停止发送心跳，等待服务端踢出...");
                    }
                }
                // 超过最大次数不再发送心跳
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 处理接收到的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = (String) msg;
        System.out.println(String.format("[RECV] %s - 收到服务器消息: %s",
                sdf.format(new Date()), message));
    }

    /**
     * 连接建立时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[INFO] 连接到服务器 " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * 连接断开时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[INFO] 与服务器断开连接");
        super.channelInactive(ctx);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.printf("[ERROR] 发生异常: %s%n", cause.getMessage());
        ctx.close();
    }
}
