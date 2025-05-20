package com.wangguangwu.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Http 服务端业务处理器，处理 Http 请求并返回响应。
 * 继承自 SimpleChannelInboundHandler，专门用于处理 FullHttpRequest 类型的消息。
 *
 * @author wangguangwu
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 日期时间格式化器，用于在响应中显示当前时间
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 读取并处理客户端 Http 请求
     * 当客户端发送 HTTP 请求到服务器时，此方法会被调用
     *
     * @param ctx 通道处理上下文，提供对通道和处理链的访问
     * @param request HTTP 请求对象，包含请求的所有信息
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 构建响应内容，包含请求的 URI、方法和请求体内容
        String responseContent = String.format(
                "收到 HTTP 请求:\n" +
                "URI: %s\n" +
                "方法: %s\n" +
                "内容: %s\n" +
                "时间: %s\n",
                request.uri(),
                request.method(),
                request.content().toString(CharsetUtil.UTF_8),
                LocalDateTime.now().format(FORMATTER)
        );

        // 创建 HTTP 响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8)
        );

        // 设置响应头信息
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        // 写出响应并添加监听器，响应发送完成后关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 处理异常事件
     * 当处理过程中发生异常时，此方法会被调用
     *
     * @param ctx 通道处理上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 打印异常信息
        cause.printStackTrace();
        // 关闭连接
        ctx.close();
    }
}
