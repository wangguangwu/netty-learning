package com.wangguangwu.netty.protocoltcp.initializer;

import com.wangguangwu.netty.protocoltcp.codec.MessageDecoder;
import com.wangguangwu.netty.protocoltcp.codec.MessageEncoder;
import com.wangguangwu.netty.protocoltcp.handler.ClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * 客户端通道初始化器
 * 负责设置客户端 ChannelPipeline 中的处理器
 *
 * @author wangguangwu
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 获取 pipeline
        ChannelPipeline pipeline = ch.pipeline();

        // 添加编解码器和业务处理器
        // 入站消息处理顺序: MessageDecoder -> ClientHandler
        // 出站消息处理顺序: MessageEncoder
        pipeline.addLast(new MessageEncoder());
        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new ClientHandler());
    }
}
