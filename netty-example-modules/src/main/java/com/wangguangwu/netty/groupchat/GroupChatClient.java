package com.wangguangwu.netty.groupchat;

import com.wangguangwu.netty.groupchat.handler.GroupChatClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Netty 群聊客户端。
 * 支持用户名注册，消息发送和接收。
 *
 * @author wangguangwu
 */
public class GroupChatClient {

    private final String host;
    private final int port;
    private Channel channel;

    public GroupChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 启动客户端，连接服务器并处理用户输入。
     * 第一条消息作为用户名发送，后续消息作为聊天内容。
     */
    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            channel = createChannel(group);
            // 用户名注册流程
            registerUsername();
            // 处理用户输入
            handleUserInput();
        } finally {
            group.shutdownGracefully();
            System.out.println("[INFO] 客户端已关闭");
        }
    }

    /**
     * 处理用户名注册流程
     */
    private void registerUsername() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[系统] 请输入用户名: ");
        String username = scanner.nextLine().trim();
        // 用户名不能为空
        while (username.isEmpty()) {
            System.out.print("[系统] 用户名不能为空，请重新输入: ");
            username = scanner.nextLine().trim();
        }
        // 发送用户名作为第一条消息
        channel.writeAndFlush(username);
        System.out.println("[系统] 用户名注册成功，开始聊天吧！");
    }

    /**
     * 处理用户输入的聊天消息
     */
    private void handleUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[系统] 输入消息开始聊天，输入'exit'退出");
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            if ("exit".equalsIgnoreCase(msg.trim())) {
                System.out.println("[系统] 正在退出...");
                break;
            }
            if (!msg.trim().isEmpty()) {
                channel.writeAndFlush(msg);
            }
        }
    }

    /**
     * 创建并连接 Channel。
     * 包含重连逻辑，确保稳定连接。
     */
    private Channel createChannel(EventLoopGroup group) throws Exception {
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                // 连接超时5秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                // 保持连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 添加心跳检测，20秒没有写操作就发送心跳
                        pipeline.addLast(new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS));
                        // 添加字符串编解码器
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        // 添加客户端消息处理器
                        pipeline.addLast(new GroupChatClientHandler());
                    }
                });

        System.out.println("[INFO] 正在连接服务器 " + host + ":" + port + "...");
        return bootstrap.connect(host, port).sync().channel();
    }

    public static void main(String[] args) throws Exception {
        // 正常模式：启动单个交互式客户端
        new GroupChatClient("127.0.0.1", 8080).run();
    }
}
