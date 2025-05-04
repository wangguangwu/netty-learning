package com.wangguangwu.netty.tcp.initializer;

import com.wangguangwu.netty.tcp.handler.TcpClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * TCP 客户端初始化器
 * 负责设置客户端 ChannelPipeline 中的处理器
 *
 * @author wangguangwu
 */
public class TcpClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 获取 pipeline
        ChannelPipeline pipeline = socketChannel.pipeline();
        
        // 添加处理器到 pipeline
        // 注意：这里没有添加编解码器，用于演示TCP粘包/拆包问题
        pipeline.addLast(new TcpClientHandler());
    }
}
