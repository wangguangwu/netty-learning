package com.wangguangwu.netty.task;

import com.wangguangwu.netty.task.handler.NettyTaskServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty 服务端启动类，负责监听端口并处理客户端连接（Task项目专用）
 *
 * @author wangguangwu
 */
public class NettyServer {

    /**
     * 启动 Netty 服务端
     *
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        // 创建 bossGroup 线程组，只处理连接请求
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 创建 workerGroup 线程组，处理与客户端的数据读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建服务器端的启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 设置相关参数
            // 设置 bossGroup 和 workerGroup 线程组
            bootstrap.group(bossGroup, workerGroup)
                    // 设置通道类型为 NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置线程队列获取连接的个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 设置 workerGroup 的处理器为 TaskServerHandler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 添加 Task 项目专用自定义处理器
                            ch.pipeline().addLast(new NettyTaskServerHandler());
                        }
                    });

            System.out.println("===========服务器启动成功===========");

            // 绑定端口并同步，生成 ChannelFuture
            // 绑定端口，启动服务器
            ChannelFuture cf = bootstrap.bind(8080).sync();

            // 给 ChannelFuture 注册监听器，监控关心的事件
            // 注册监听器，监听端口绑定结果
            cf.addListener((ChannelFutureListener) future -> {
                if (cf.isSuccess()) {
                    System.out.println("监听端口 8080 成功");
                } else {
                    System.out.println("监听端口 8080 失败");
                }
            });

            // 监听关闭通道事件
            // 阻塞，直到服务器通道关闭
            cf.channel().closeFuture().sync();
        } finally {
            // 优雅关闭 bossGroup
            bossGroup.shutdownGracefully();
            // 优雅关闭 workerGroup
            workerGroup.shutdownGracefully();
        }
    }
}
