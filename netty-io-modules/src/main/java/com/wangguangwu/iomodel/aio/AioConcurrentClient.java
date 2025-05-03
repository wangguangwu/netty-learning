package com.wangguangwu.iomodel.aio;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO 并发客户端，用于测试 AIO 并发服务器
 *
 * @author wangguangwu
 */
public class AioConcurrentClient {

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
     * 并发启动多个客户端连接 AIO 服务器。
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
        LoggerUtil.info("所有 AIO 并发客户端请求完成");
    }

    /**
     * 单个客户端向服务器发送消息并接收响应。
     *
     * @param idx   客户端编号
     * @param latch 并发同步器
     */
    private static void sendMessage(int idx, CountDownLatch latch) {
        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            CountDownLatch innerLatch = new CountDownLatch(1);
            client.connect(new InetSocketAddress("localhost", 8083), null, new CompletionHandler<Void, Void>() {
                @Override
                public void completed(Void result, Void attachment) {
                    ByteBuffer buffer = ByteBuffer.wrap(("你好，AIO 服务器！我是客户端 " + idx).getBytes());
                    client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer buf) {
                            // 构造接收响应的缓冲区
                            ByteBuffer respBuffer = ByteBuffer.allocate(1024);
                            // 从服务器读取响应
                            client.read(respBuffer, respBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                                @Override
                                public void completed(Integer result, ByteBuffer buf) {
                                    // 读操作后，buffer 的 position 在写入末尾，limit 在容量末尾，需要 flip 切换到读模式
                                    // flip() 让 buffer 从“写模式”切换到“读模式”，position 归零，limit 设为写入末尾，便于读取数据
                                    buf.flip();
                                    LoggerUtil.info("客户端 " + idx + " 收到响应: " + new String(buf.array(), 0, result));
                                    try {
                                        client.close();
                                    } catch (Exception ignored) {
                                    }
                                    innerLatch.countDown();
                                }

                                @Override
                                public void failed(Throwable exc, ByteBuffer buf) {
                                    LoggerUtil.error("客户端 " + idx + " 读取失败: " + exc.getMessage());
                                    innerLatch.countDown();
                                }
                            });
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer buf) {
                            System.out.println("客户端 " + idx + " 写入失败: " + exc.getMessage());
                            innerLatch.countDown();
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("客户端 " + idx + " 连接失败: " + exc.getMessage());
                    innerLatch.countDown();
                }
            });
            innerLatch.await();
        } catch (Exception e) {
            System.err.println("客户端 " + idx + " 异常: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}
