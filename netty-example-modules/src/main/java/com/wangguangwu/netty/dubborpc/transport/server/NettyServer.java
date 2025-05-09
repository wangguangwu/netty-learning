package com.wangguangwu.netty.dubborpc.transport.server;

import com.wangguangwu.netty.dubborpc.codec.RpcDecoder;
import com.wangguangwu.netty.dubborpc.codec.RpcEncoder;
import com.wangguangwu.netty.dubborpc.protocol.RpcRequest;
import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import com.wangguangwu.netty.dubborpc.registry.ServiceRegistry;
import com.wangguangwu.netty.dubborpc.serialize.JSONSerializer;
import com.wangguangwu.netty.dubborpc.serialize.Serializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty服务端
 * 负责启动服务器并接收客户端请求
 *
 * @author wangguangwu
 */
public class NettyServer {

    /**
     * 服务器端口
     */
    private final int port;

    /**
     * 服务注册表
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * 序列化器
     */
    private final Serializer serializer;

    /**
     * Boss线程组，用于接收连接
     */
    private EventLoopGroup bossGroup;

    /**
     * Worker线程组，用于处理连接
     */
    private EventLoopGroup workerGroup;

    /**
     * 构造函数
     *
     * @param port 服务器端口
     */
    public NettyServer(int port) {
        this.port = port;
        this.serviceRegistry = ServiceRegistry.getINSTANCE();
        this.serializer = new JSONSerializer();
    }

    /**
     * 启动服务器
     */
    public void start() {
        try {
            // 创建线程组
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            // 创建服务器启动器
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 添加编解码器和处理器
                            ch.pipeline()
                                    // 添加RPC请求解码器
                                    .addLast(new RpcDecoder(RpcRequest.class, serializer))
                                    // 添加RPC响应编码器
                                    .addLast(new RpcEncoder(RpcResponse.class, serializer))
                                    // 添加RPC服务端处理器
                                    .addLast(new RpcServerHandler(serviceRegistry));
                        }
                    });

            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("RPC服务器已启动，监听端口: " + port);

            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * 关闭服务器
     */
    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        System.out.println("RPC服务器已关闭");
    }

    /**
     * 注册服务
     *
     * @param interfaceClass  服务接口类
     * @param serviceInstance 服务实例
     */
    public void registerService(Class<?> interfaceClass, Object serviceInstance) {
        serviceRegistry.registerService(interfaceClass, serviceInstance);
    }
}
