package com.wangguangwu.netty.groupchat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Netty 群聊客户端处理器，负责消息接收和心跳维护。
 *
 * @author wangguangwu
 */
public class GroupChatClientHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 处理服务器发来的消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 直接输出服务器消息，保持格式
        System.out.println(msg);
    }
    
    /**
     * 连接建立时触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[系统] 已连接到服务器");
    }
    
    /**
     * 连接断开时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[系统] 与服务器的连接已断开");
    }
    
    /**
     * 心跳事件处理
     * 当触发写空闲时，发送心跳包保持连接
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.WRITER_IDLE) {
                // 发送心跳消息
                System.out.println("[心跳] 发送心跳消息");
                ctx.writeAndFlush("PING");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("[错误] " + cause.getMessage());
        ctx.close();
    }
}
