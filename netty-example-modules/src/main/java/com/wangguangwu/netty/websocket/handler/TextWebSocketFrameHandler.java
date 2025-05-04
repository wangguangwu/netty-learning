package com.wangguangwu.netty.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket 文本帧处理器
 * 用于处理 WebSocket 的文本消息，实现服务器与客户端的双向通信
 * <p>
 * TextWebSocketFrame 类型表示一个文本帧，WebSocket 通信的基本单位
 *
 * @author wangguangwu
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理接收到的 WebSocket 文本帧
     *
     * @param ctx 通道处理器上下文
     * @param msg 接收到的 WebSocket 文本帧
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String receivedMessage = msg.text();
        System.out.printf("[RECV] 收到客户端 %s 的消息: %s%n",
                ctx.channel().remoteAddress(), receivedMessage);

        // 构建响应消息
        String responseMessage = String.format("服务器时间: %s%n收到消息: %s",
                LocalDateTime.now().format(FORMATTER), receivedMessage);

        // 回复消息给客户端
        ctx.channel().writeAndFlush(new TextWebSocketFrame(responseMessage));
    }

    /**
     * 当 WebSocket 客户端连接建立时调用
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // 获取通道ID
        // 完整的ID，全局唯一
        String longId = ctx.channel().id().asLongText();
        // 简短的ID，可能在集群中不唯一
        String shortId = ctx.channel().id().asShortText();

        System.out.printf("[CONN] 客户端 %s 连接建立, ID(长): %s%n", ctx.channel().remoteAddress(), longId);
        System.out.printf("[CONN] 客户端 %s 连接建立, ID(短): %s%n", ctx.channel().remoteAddress(), shortId);
    }

    /**
     * 当 WebSocket 客户端连接断开时调用
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.printf("[DISC] 客户端 %s 连接断开, ID: %s%n",
                ctx.channel().remoteAddress(), ctx.channel().id().asLongText());
    }

    /**
     * 当通道变为活跃状态时调用
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.printf("[ACTIVE] 客户端 %s 的通道已激活%n", ctx.channel().remoteAddress());
    }

    /**
     * 当通道变为非活跃状态时调用
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.printf("[INACTIVE] 客户端 %s 的通道已关闭%n", ctx.channel().remoteAddress());
    }

    /**
     * 异常处理
     *
     * @param ctx   通道处理器上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.printf("[ERROR] 客户端 %s 发生异常: %s%n",
                ctx.channel().remoteAddress(), cause.getMessage());
        // 关闭连接
        ctx.close();
    }
}
