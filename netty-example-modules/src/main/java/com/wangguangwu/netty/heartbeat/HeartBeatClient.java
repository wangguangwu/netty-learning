package com.wangguangwu.netty.heartbeat;

import com.wangguangwu.netty.heartbeat.handler.HeartBeatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty 心跳检测客户端
 * 定期发送心跳包保持连接活跃，发送3次后停止心跳
 *
 * @author wangguangwu
 */
public class HeartBeatClient {

    /**
     * 服务器地址
     */
    private static final String HOST = "127.0.0.1";

    /**
     * 服务器端口
     */
    private static final int PORT = 8080;

    /**
     * 心跳间隔（秒）
     */
    private static final int HEARTBEAT_INTERVAL = 3;

    /**
     * 启动心跳检测客户端
     */
    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 添加编解码器
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            
                            // 添加心跳检测处理器
                            // 设置写空闲时间，当超过这个时间没有写操作时，会触发写空闲事件
                            pipeline.addLast(new IdleStateHandler(0, HEARTBEAT_INTERVAL, 0, TimeUnit.SECONDS));
                            
                            // 添加自定义处理器
                            pipeline.addLast(new HeartBeatClientHandler());
                        }
                    });

            // 连接服务器
            System.out.println("[INFO] 正在连接服务器 " + HOST + ":" + PORT + "...");
            Channel channel = bootstrap.connect(HOST, PORT).sync().channel();
            System.out.println("[INFO] 连接服务器成功，开始发送心跳...");
            System.out.println("[INFO] 客户端将发送3次心跳后停止，等待服务端踢出");
            
            // 等待连接关闭
            channel.closeFuture().sync();
            
        } finally {
            // 优雅关闭线程组
            group.shutdownGracefully();
            System.out.println("[INFO] 客户端已关闭");
        }
    }
}
