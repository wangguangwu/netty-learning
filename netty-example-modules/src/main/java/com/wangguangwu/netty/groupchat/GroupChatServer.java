package com.wangguangwu.netty.groupchat;

import com.wangguangwu.netty.groupchat.handler.GroupChatServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty 群聊服务器，支持多客户端消息转发。
 * 提供心跳检测和优化的连接参数。
 *
 * @author wangguangwu
 */
public class GroupChatServer {

    /**
     * 默认端口
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * 监听端口
     */
    private final int port;

    /**
     * 是否正在运行
     */
    private boolean running = false;

    /**
     * Boss线程组
     */
    private NioEventLoopGroup bossGroup;

    /**
     * Worker线程组
     */
    private NioEventLoopGroup workerGroup;

    /**
     * 使用默认端口构造服务器
     */
    public GroupChatServer() {
        this(DEFAULT_PORT);
    }

    /**
     * 使用指定端口构造服务器
     */
    public GroupChatServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务器。
     * 配置了连接参数和心跳检测。
     */
    public void start() throws Exception {
        if (running) {
            System.out.println("[WARN] 服务器已在运行中");
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 服务端连接队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // TCP保活，避免死连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用Nagle算法，减少延迟
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    // 心跳检测：30秒没有读写操作就触发事件
                                    .addLast(new IdleStateHandler(30, 30, 60, TimeUnit.SECONDS))
                                    // 字符串编解码器
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    // 业务处理器
                                    .addLast(new GroupChatServerHandler());
                        }
                    });

            // 绑定端口并启动
            System.out.println("[INFO] 群聊服务器启动中，端口: " + port);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            running = true;
            System.out.println("[INFO] 群聊服务器启动成功，等待客户端连接...");

            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("[ERROR] 服务器启动异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] 服务器运行异常: " + e.getMessage());
            throw e;
        } finally {
            shutdown();
        }
    }

    /**
     * 优雅关闭服务器
     */
    public void shutdown() {
        System.out.println("[INFO] 正在关闭服务器...");

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        running = false;
        System.out.println("[INFO] 服务器已关闭");
    }

    public static void main(String[] args) throws Exception {
        // 支持命令行参数指定端口
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("[WARN] 无效端口参数，使用默认端口: " + DEFAULT_PORT);
            }
        }

        // 启动服务器
        new GroupChatServer(port).start();
    }
}
