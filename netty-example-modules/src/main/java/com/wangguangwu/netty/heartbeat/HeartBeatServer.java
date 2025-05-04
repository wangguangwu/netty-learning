package com.wangguangwu.netty.heartbeat;

import com.wangguangwu.netty.heartbeat.handler.HeartBeatServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty 心跳检测服务器
 * 用于检测客户端连接是否存活，并处理空闲连接
 *
 * @author wangguangwu
 */
public class HeartBeatServer {

    /**
     * 服务器端口
     */
    private static final int PORT = 8080;

    /**
     * 读空闲超时时间（秒）
     */
    private static final int READER_IDLE_TIME = 5;

    /**
     * 写空闲超时时间（秒）
     */
    private static final int WRITER_IDLE_TIME = 0;

    /**
     * 读写空闲超时时间（秒）
     */
    private static final int ALL_IDLE_TIME = 10;

    /**
     * 启动心跳检测服务器
     */
    public static void main(String[] args) throws Exception {
        // 创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 为 bossGroup 添加日志处理器
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // TCP参数配置
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());

                            // 添加 IdleStateHandler 心跳检测处理器
                            // 当 IdleStateEvent 触发后，会传递给管道的下一个 handler 处理
                            // 下一个 handler 的 userEventTriggered 方法会被调用
                            pipeline.addLast(new IdleStateHandler(
                                    READER_IDLE_TIME,  // 读空闲时间
                                    WRITER_IDLE_TIME,  // 写空闲时间
                                    ALL_IDLE_TIME,     // 读写空闲时间
                                    TimeUnit.SECONDS));

                            // 添加自定义的心跳处理器
                            pipeline.addLast(new HeartBeatServerHandler());
                        }
                    });

            // 启动服务器
            System.out.println("[INFO] 心跳检测服务器启动中，端口: " + PORT);
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("[INFO] 心跳检测服务器启动成功，等待客户端连接...");

            // 等待服务器关闭
            channelFuture.channel().closeFuture().sync();

        } finally {
            // 优雅关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("[INFO] 心跳检测服务器已关闭");
        }
    }
}
