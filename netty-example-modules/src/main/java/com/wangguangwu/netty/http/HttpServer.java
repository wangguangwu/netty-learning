package com.wangguangwu.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Http 服务端启动类，负责监听端口并处理 Http 请求。
 *
 * @author wangguangwu
 */
public class HttpServer {

    /**
     * 服务端监听端口
     */
    private static final int PORT = 8080;

    /**
     * 启动 Netty Http 服务端
     *
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        // bossGroup 只处理连接请求
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // workerGroup 负责处理客户端业务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建服务器端的启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置参数
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpServerInitializer());
            // 绑定端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("Http 服务器启动，监听端口: " + PORT);
            // 阻塞，直到服务器通道关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
