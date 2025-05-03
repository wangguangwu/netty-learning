package com.wangguangwu.iomodel.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.wangguangwu.iomodel.common.LoggerUtil;

/**
 * AIO 示例
 * 基于 CompletionHandler 的异步 IO 服务器。
 * 注意：Java AIO 在 Linux 下底层实现并非真正的异步 IO。
 *
 * @author wangguangwu
 */
public class AioServer {

    /**
     * 服务器主入口，负责启动和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            System.err.println("AIO 服务器启动失败: " + e.getMessage());
        }
    }

    /**
     * 启动 AIO 服务器，监听指定端口并异步接收客户端连接。
     *
     * @throws IOException 启动或接收连接时发生的 IO 异常
     */
    private static void startServer() throws IOException {
        AsynchronousServerSocketChannel serverChannel =
                AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(8083));
        LoggerUtil.info("AIO 服务器启动，监听端口 " + 8083 + " ...");
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void attachment) {
                handleAccept(serverChannel, this, client);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                LoggerUtil.error("连接失败: " + exc.getMessage());
            }
        });
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 处理客户端连接接入并注册异步读。
     *
     * @param serverChannel 服务器通道
     * @param handler       CompletionHandler 实例
     * @param client        客户端通道
     */
    private static void handleAccept(AsynchronousServerSocketChannel serverChannel, CompletionHandler<AsynchronousSocketChannel, Void> handler, AsynchronousSocketChannel client) {
        // 继续接收下一个连接
        serverChannel.accept(null, handler);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer buf) {
                handleRead(client, result, buf);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                LoggerUtil.error("读取失败: " + exc.getMessage());
                try {
                    client.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    /**
     * 读取客户端数据并响应。
     *
     * @param client 客户端通道
     * @param result 读取到的数据长度
     * @param buf    数据缓冲区
     */
    private static void handleRead(AsynchronousSocketChannel client, Integer result, ByteBuffer buf) {
        // 读操作后，buffer 的 position 在写入末尾，limit 在容量末尾，需要 flip 切换到读模式
        buf.flip(); // 切换为读模式，position 归零，limit 设为写入末尾
        String msg = new String(buf.array(), 0, result);
        LoggerUtil.info("收到客户端消息: " + msg);
        // 写响应前需将数据包装成新的 ByteBuffer
        client.write(ByteBuffer.wrap(("服务端已收到: " + msg).getBytes()));
        try {
            client.close();
        } catch (IOException ignored) {
        }
    }
}

