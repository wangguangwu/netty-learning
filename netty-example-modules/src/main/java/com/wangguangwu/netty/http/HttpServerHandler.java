package com.wangguangwu.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * Http 服务端业务处理器，处理 Http 请求并返回响应。
 *
 * @author wangguangwu
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    /**
     * 读取并处理客户端 Http 请求
     * @param ctx 上下文对象
     * @param msg HTTP 消息对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        // 打印当前通道和 pipeline 信息，便于调试
        System.out.println("channel=" + ctx.channel() + " pipeline=" + ctx.pipeline());
        // 判断是否为 HTTP 请求
        if (msg instanceof HttpRequest httpRequest) {
            // 过滤浏览器自动请求的 favicon.ico
            URI uri = new URI(httpRequest.uri());
            if ("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了 favicon.ico, 不做响应");
                return;
            }
            // 构造响应内容
            ByteBuf content = Unpooled.copiedBuffer("hello, 我是 Http 服务器", CharsetUtil.UTF_8);
            // 构造 HTTP 响应对象
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    content
            );
            // 设置响应头
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            // 发送响应
            ctx.writeAndFlush(response);
        }
    }
}
