package com.wangguangwu.iomodel.nio;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO 客户端示例
 *
 * @author wangguangwu
 */
public class NioClient {

    /**
     * 客户端主入口，负责异常捕获与参数准备。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            sendMessage();
        } catch (IOException e) {
            System.err.println("客户端异常: " + e.getMessage());
        }
    }

    /**
     * 向指定 NIO 服务器发送消息并接收响应。
     *
     * @throws IOException 网络异常
     */
    private static void sendMessage() throws IOException {
        try (SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 8082))) {
            client.configureBlocking(true);
            // 写消息到服务端，明确字符集
            ByteBuffer writeBuffer = ByteBuffer.wrap("你好，NIO 服务器！".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            client.write(writeBuffer);
            // 读响应，分配新 buffer
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int len = client.read(readBuffer);
            if (len > 0) {
                readBuffer.flip();
                LoggerUtil.info("收到服务端响应: " + new String(readBuffer.array(), 0, len, java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }
}
