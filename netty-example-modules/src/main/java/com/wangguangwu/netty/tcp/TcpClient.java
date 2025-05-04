package com.wangguangwu.netty.tcp;

import com.wangguangwu.netty.tcp.initializer.TcpClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * TCP 客户端
 * 用于演示与 TCP 服务器的通信，展示粘包/拆包问题
 *
 * @author wangguangwu
 */
public class TcpClient {

    /**
     * 服务器地址
     */
    private static final String HOST = "localhost";

    /**
     * 服务器端口
     */
    private static final int PORT = 8080;

    public static void main(String[] args) throws InterruptedException {
        // 创建事件循环组
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            // 创建客户端启动对象
            Bootstrap bootstrap = new Bootstrap();
            // 配置客户端
            bootstrap.group(eventLoopGroup)
                    // 设置客户端通道的实现类
                    .channel(NioSocketChannel.class)
                    // 添加处理器
                    .handler(new TcpClientInitializer());

            System.out.println("TCP 客户端启动中...");

            // 连接服务器
            ChannelFuture channelFuture = bootstrap.connect(HOST, PORT).sync();
            System.out.println("TCP 客户端启动成功，连接到服务器: " + HOST + ":" + PORT);

            // 等待连接关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭线程组
            eventLoopGroup.shutdownGracefully();
            System.out.println("TCP 客户端已关闭");
        }
    }
}
