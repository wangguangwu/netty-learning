package com.wangguangwu.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

/**
 * 基于 Netty 实现的 HTTP 服务器，支持请求处理、内容压缩和消息聚合。
 *
 * @author wangguangwu
 */
public class HttpServer {

    /**
     * 服务端默认监听端口
     */
    private static final int PORT = 8088;

    /**
     * 启动 HTTP 服务器
     *
     * @param port 监听端口
     * @throws Exception 启动过程中可能发生的异常
     */
    public void start(int port) throws Exception {
        // 创建 Boss 线程组，用于接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 创建 Worker 线程组，用于处理客户端连接的读写操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建服务端启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置服务端参数
            serverBootstrap.group(bossGroup, workerGroup)
                    // 指定 Channel 类型为 NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置服务端监听地址和端口
                    .localAddress(new InetSocketAddress(port))
                    // 配置子通道（客户端连接）的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 配置通道处理链（Pipeline）
                            ch.pipeline()
                                    // HTTP 编解码器，处理 HTTP 请求和响应的编码解码
                                    .addLast("codec", new HttpServerCodec())
                                    // HTTP 内容压缩器，减小响应体积提高传输效率
                                    .addLast("compressor", new HttpContentCompressor())
                                    // HTTP 消息聚合器，将 HTTP 消息的多个部分合并为完整的 FullHttpRequest 或 FullHttpResponse
                                    .addLast("aggregator", new HttpObjectAggregator(65536))
                                    // 自定义业务逻辑处理器，处理 HTTP 请求并生成响应
                                    .addLast("handler", new HttpServerHandler());
                        }
                    })
                    // 设置 TCP 保活机制，保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            // 绑定端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("HTTP 服务器已启动，监听端口: " + port);
            
            // 等待服务器关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭线程组，释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 主方法，启动 HTTP 服务器
     *
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    public static void main(String[] args) throws Exception {
        new HttpServer().start(PORT);
    }
}
