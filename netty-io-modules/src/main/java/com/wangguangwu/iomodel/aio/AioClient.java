package com.wangguangwu.iomodel.aio;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * AIO 客户端示例
 *
 * @author wangguangwu
 */
public class AioClient {

    /**
     * 客户端主入口，负责异常捕获与参数准备。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            sendMessage();
        } catch (Exception e) {
            LoggerUtil.error("客户端异常: " + e.getMessage());
        }
    }

    /**
     * 向指定 AIO 服务器发送消息并接收响应。
     *
     * @throws Exception 网络异常
     */
    private static void sendMessage() throws Exception {
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        CountDownLatch latch = new CountDownLatch(1);
        client.connect(new InetSocketAddress("localhost", 8083), null, new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                ByteBuffer buffer = ByteBuffer.wrap("你好，AIO 服务器！".getBytes());
                client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer buf) {
                        // 分配一个 1024 字节的缓冲区，用于存储服务端响应
                        ByteBuffer respBuffer = ByteBuffer.allocate(1024);
                        client.read(respBuffer, respBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer buf) {
                                // 读操作后，buffer 的 position 在写入末尾，limit 在容量末尾，需要 flip 切换到读模式
                                // flip() 让 buffer 从“写模式”切换到“读模式”，position 归零，limit 设为写入末尾，便于读取数据
                                buf.flip();
                                LoggerUtil.info("收到服务端响应: " + new String(buf.array(), 0, result));
                                try {
                                    client.close();
                                } catch (Exception ignored) {
                                }
                                latch.countDown();
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer buf) {
                                LoggerUtil.error("读取失败: " + exc.getMessage());
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer buf) {
                        LoggerUtil.error("写入失败: " + exc.getMessage());
                        System.out.println("写入失败: " + exc.getMessage());
                        latch.countDown();
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("连接失败: " + exc.getMessage());
                latch.countDown();
            }
        });
        latch.await();
    }
}

