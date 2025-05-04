package com.wangguangwu.netty.simple;

import com.wangguangwu.netty.simple.handler.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Netty 客户端启动类，负责连接服务器并处理事件
 *
 * @author wangguangwu
 */
public class NettyClient {

    /**
     * 启动 Netty 客户端
     *
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        // 创建客户端事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建客户端启动对象
            Bootstrap bootstrap = new Bootstrap();

            /*
             * 设置相关参数
             */
            // 设置线程组
            bootstrap.group(group)
                    // 设置客户端通道的实现类(反射)
                    .channel(NioSocketChannel.class)
                    // 设置自定义通道初始化器
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 添加自定义处理器
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });

            System.out.println("===========客户端启动成功===========");

            // 启动客户端去连接服务器端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();

            // 监听关闭通道事件
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组
            group.shutdownGracefully();
        }
    }
}
