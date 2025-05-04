package com.wangguangwu.iomodel.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 单Reactor单线程模型服务端
 * 只有一个Selector和一个线程负责所有事件处理，包括接收连接、读写数据
 */
public class SingleReactorServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("[Server] SingleReactorServer started on port 8080");

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
                }
            }
        }
    }
}
