package com.wangguangwu.iomodel.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单Reactor线程池模式服务端
 * 一个Selector主线程负责事件分发，业务处理交给线程池
 */
public class SingleReactorThreadPoolServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8081));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("[Server] SingleReactorThreadPoolServer started on port 8081");

        ExecutorService pool = Executors.newFixedThreadPool(4); // 业务线程池

        while (true) {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("[Server] Accepted connection from " + client.getRemoteAddress());
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    pool.submit(() -> handleRead(client));
                }
            }
        }
    }

    private static void handleRead(SocketChannel client) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = client.read(buffer);
            if (read > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, buffer.limit());
                System.out.println("[Server] Received: " + msg);
                // 回写数据
                buffer.clear();
                buffer.put(("Echo: " + msg).getBytes());
                buffer.flip();
                client.write(buffer);
            } else if (read == -1) {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
