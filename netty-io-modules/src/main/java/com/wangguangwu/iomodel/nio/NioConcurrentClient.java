package com.wangguangwu.iomodel.nio;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * NIO 并发客户端，用于测试 NIO 并发服务器
 *
 * @author wangguangwu
 */
public class NioConcurrentClient {

    /**
     * 客户端主入口，负责并发测试和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            runConcurrentClients();
        } catch (InterruptedException e) {
            LoggerUtil.error("主线程中断: " + e.getMessage());
        }
    }

    /**
     * 并发启动多个客户端连接 NIO 服务器。
     *
     * @throws InterruptedException 线程中断异常
     */
    private static void runConcurrentClients() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            new Thread(() -> sendMessage(idx, latch)).start();
        }
        latch.await();
        LoggerUtil.info("所有 NIO 并发客户端请求完成");
    }

    /**
     * 单个客户端向服务器发送消息并接收响应。
     *
     * @param idx   客户端编号
     * @param latch 并发同步器
     */
    private static void sendMessage(int idx, CountDownLatch latch) {
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8082))) {
            client.configureBlocking(true);
            ByteBuffer buffer = ByteBuffer.wrap(("你好，NIO 服务器！我是客户端 " + idx).getBytes());
            // 写消息到服务端
            client.write(buffer);
            // 清空 buffer，准备读入服务端响应
            buffer.clear();
            int len = client.read(buffer);
            if (len > 0) {
                // 切换为读模式，position 归零，limit 设为写入末尾
                buffer.flip();
                LoggerUtil.info("客户端 " + idx + " 收到响应: " + new String(buffer.array(), 0, len));
            }
        } catch (IOException e) {
            LoggerUtil.error("客户端 " + idx + " 异常: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}

