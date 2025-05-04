package com.wangguangwu.netty.protocoltcp;

import com.wangguangwu.netty.protocoltcp.initializer.ServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 自定义协议 TCP 服务器
 * 演示如何使用自定义协议和编解码器解决 TCP 粘包/拆包问题
 *
 * @author wangguangwu
 */
public class ProtocolServer {

    /**
     * 服务器端口
     */
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // 创建 boss 线程组，用于接收客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 创建 worker 线程组，用于处理客户端业务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建服务器启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 配置服务器
            serverBootstrap.group(bossGroup, workerGroup)
                    // 使用 NioServerSocketChannel 作为服务器的通道实现
                    .channel(NioServerSocketChannel.class)
                    // 添加处理器
                    .childHandler(new ServerInitializer());

            System.out.println("自定义协议 TCP 服务器启动中...");

            // 绑定端口并启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("自定义协议 TCP 服务器启动成功，监听端口: " + PORT);

            // 等待服务器关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("自定义协议 TCP 服务器已关闭");
        }
    }
}
