package com.wangguangwu.netty.dubborpc.transport.client;

import com.wangguangwu.netty.dubborpc.codec.RpcDecoder;
import com.wangguangwu.netty.dubborpc.codec.RpcEncoder;
import com.wangguangwu.netty.dubborpc.protocol.RpcRequest;
import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import com.wangguangwu.netty.dubborpc.serialize.JSONSerializer;
import com.wangguangwu.netty.dubborpc.serialize.Serializer;
import com.wangguangwu.netty.dubborpc.transport.RpcFuture;
import com.wangguangwu.netty.dubborpc.transport.RpcRequestManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 * 负责与RPC服务器建立连接并发送请求
 *
 * @author wangguangwu
 */
public class NettyClient {

    /**
     * 服务器地址
     */
    private final String hostname;

    /**
     * 服务器端口
     */
    private final int port;

    /**
     * 客户端通道
     */
    private Channel channel;

    /**
     * 事件循环组
     */
    private EventLoopGroup group;

    /**
     * 客户端处理器
     */
    private RpcClientHandler clientHandler;

    /**
     * 请求管理器
     */
    private final RpcRequestManager requestManager;

    /**
     * 序列化器
     */
    private final Serializer serializer;

    /**
     * 构造函数
     *
     * @param hostname 服务器地址
     * @param port     服务器端口
     */
    public NettyClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        this.requestManager = RpcRequestManager.getINSTANCE();
        this.serializer = new JSONSerializer();
    }

    /**
     * 启动客户端
     *
     * @throws Exception 启动异常
     */
    public void start() throws Exception {
        group = new NioEventLoopGroup();
        clientHandler = new RpcClientHandler();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                // 添加RPC响应解码器
                                .addLast(new RpcDecoder(RpcResponse.class, serializer))
                                // 添加RPC请求编码器
                                .addLast(new RpcEncoder(RpcRequest.class, serializer))
                                // 添加RPC客户端处理器
                                .addLast(clientHandler);
                    }
                });

        // 连接服务器
        ChannelFuture future = bootstrap.connect(hostname, port).sync();
        channel = future.channel();

        System.out.println("RPC客户端已连接到服务器: " + hostname + ":" + port);
    }

    /**
     * 关闭客户端
     */
    public void shutdown() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        System.out.println("RPC客户端已关闭");
    }

    /**
     * 发送请求
     *
     * @param request 请求对象
     * @return RPC调用的Future对象
     */
    public RpcFuture sendRequest(RpcRequest request) {
        // 创建RPC Future
        RpcFuture future = new RpcFuture(request);
        // 注册请求
        requestManager.registerRequest(request.getRequestId(), future);

        // 发送请求
        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("请求发送成功: " + request.getRequestId());
            } else {
                System.err.println("请求发送失败: " + request.getRequestId());
                future.setFailure(channelFuture.cause());
            }
        });

        return future;
    }

    /**
     * 同步调用
     *
     * @param request      请求对象
     * @param timeoutMillis 超时时间(毫秒)
     * @return 响应结果
     * @throws Exception 调用异常
     */
    public Object syncCall(RpcRequest request, long timeoutMillis) throws Exception {
        RpcFuture future = sendRequest(request);
        return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
    }
}
