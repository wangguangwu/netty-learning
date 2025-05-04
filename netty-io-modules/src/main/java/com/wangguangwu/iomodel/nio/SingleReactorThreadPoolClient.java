package com.wangguangwu.iomodel.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * 单Reactor线程池模式客户端
 * 连接服务端并发送消息，接收回显
 */
public class SingleReactorThreadPoolClient {
    public static void main(String[] args) throws IOException {
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress("localhost", 8081));
        client.configureBlocking(true); // 简化处理
        System.out.println("[Client] Connected to server");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[Client] Enter message: ");
            String msg = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            client.write(buffer);
            buffer.clear();
            // 读取服务端回显
            int read = client.read(buffer);
            if (read > 0) {
                buffer.flip();
                System.out.println("[Client] Received: " + new String(buffer.array(), 0, buffer.limit()));
            }
            buffer.clear();
        }
    }
}
