package com.wangguangwu.iomodel.nio;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NIO 示例
 * 基于 Selector 的多路复用服务器。
 * 非阻塞 IO，适合高并发场景。
 *
 * @author wangguangwu
 */
public class NioSelectorServer {

    /**
     * 服务器主入口，负责启动和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            LoggerUtil.error("NIO Selector 服务器启动失败: " + e.getMessage());
        }
    }

    /**
     * 启动 NIO Selector 服务器，监听指定端口并循环接收客户端连接。
     *
     * @throws IOException 启动或接收连接时发生的 IO 异常
     */
    private static void startServer() throws IOException {
        // 1. 初始化 Selector 和 ServerSocketChannel
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8082));
        // 配置为非阻塞模式，才能让 selector 进行多路复用，否则 accept/read 等操作会阻塞主线程
        serverChannel.configureBlocking(false);
        // 注册“接收连接”事件到 selector，只有这样 selector 才能感知新连接到来
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        LoggerUtil.info("NIO Selector 服务器启动，监听端口 " + 8082 + " ...");
        // 2. 主循环：轮询就绪事件，不断接收/处理客户端
        while (!Thread.currentThread().isInterrupted()) {
            // 阻塞直到有事件就绪
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 必须 remove，否则下次 select 时这些已处理的事件还会被重复处理
                iter.remove();
                // 3. 有新连接到来
                if (key.isAcceptable()) {
                    acceptClient(serverChannel, selector);
                }
                // 4. 有客户端可读
                else if (key.isReadable()) {
                    readClient(key);
                }
            }
        }
        // 5. 清理资源
        selector.close();
        serverChannel.close();
    }

    /**
     * 接收新的客户端连接，并注册读事件到 selector。
     *
     * @param serverChannel 服务器通道
     * @param selector      selector 实例
     * @throws IOException 网络异常
     */
    private static void acceptClient(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        // 目的：从 ServerSocketChannel 获取到一个新的客户端连接
        SocketChannel client = serverChannel.accept();
        // 配置为非阻塞，才能让 selector 统一管理所有客户端
        client.configureBlocking(false);
        // 注册“读事件”到 selector，只有这样 selector 才能感知客户端有数据可读
        client.register(selector, SelectionKey.OP_READ);
        LoggerUtil.info("客户端已连接: " + client.getRemoteAddress());
    }

    /**
     * 读取客户端数据并响应。
     *
     * @param key SelectionKey 对象
     */
    private static void readClient(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int len = client.read(buffer);
            if (len > 0) {
                // 读操作后，buffer 的 position 在写入末尾，limit 在容量末尾，需要 flip 切换到读模式
                // 切换为读模式，position 归零，limit 设为写入末尾
                buffer.flip();
                String msg = new String(buffer.array(), 0, len, java.nio.charset.StandardCharsets.UTF_8);
                LoggerUtil.info("收到客户端消息: " + msg);
                // 写响应，明确字符集
                client.write(ByteBuffer.wrap(("服务端已收到: " + msg).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            } else if (len == -1) {
                // read 返回 -1 表示客户端已关闭连接，需及时关闭 SocketChannel 释放资源
                client.close();
                LoggerUtil.info("客户端连接正常关闭");
            }
        } catch (IOException e) {
            if (e instanceof java.net.SocketException && e.getMessage().contains("Connection reset")) {
                LoggerUtil.warn("客户端强制关闭连接（Connection reset）: " + e.getMessage());
            } else {
                LoggerUtil.error("处理客户端异常: " + e.getMessage());
            }
            try {
                client.close();
                LoggerUtil.info("客户端连接已关闭");
            } catch (IOException ignored) {
            }
        }
    }
}
