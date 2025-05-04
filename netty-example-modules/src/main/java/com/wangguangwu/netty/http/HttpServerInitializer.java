package com.wangguangwu.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Http 服务端通道初始化器，配置 pipeline 处理器链。
 *
 * @author wangguangwu
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 初始化每个新连接的 pipeline
     *
     * @param ch 新建的 SocketChannel
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        // 获取 pipeline
        ChannelPipeline pipeline = ch.pipeline();
        // 添加 HTTP 编解码器
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        // 添加自定义 HTTP 业务处理器
        pipeline.addLast("HttpServerHandler", new HttpServerHandler());
        System.out.println("HTTP pipeline 初始化完成");
    }
}
