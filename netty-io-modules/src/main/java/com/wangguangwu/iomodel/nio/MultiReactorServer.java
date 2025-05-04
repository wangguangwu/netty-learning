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
 * 多Reactor多线程模式服务端
 * 主Reactor负责接收连接，多个子Reactor负责读写和业务处理
 */
public class MultiReactorServer {
    private static final int SUB_REACTOR_COUNT = 2;
    private static final int PORT = 8082;

    public static void main(String[] args) throws IOException {
        // 主Reactor
        Selector bossSelector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(bossSelector, SelectionKey.OP_ACCEPT);
        System.out.println("[Server] MultiReactorServer started on port " + PORT);

        // 子Reactor线程池
        SubReactor[] subReactors = new SubReactor[SUB_REACTOR_COUNT];
        for (int i = 0; i < SUB_REACTOR_COUNT; i++) {
            subReactors[i] = new SubReactor();
            new Thread(subReactors[i], "SubReactor-" + i).start();
        }
        int index = 0;

        while (true) {
            bossSelector.select();
            Iterator<SelectionKey> iterator = bossSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    // 轮询分配给子Reactor
                    subReactors[index++ % SUB_REACTOR_COUNT].register(client);
                    System.out.println("[Server] Accepted connection from " + client.getRemoteAddress());
                }
            }
        }
    }

    /**
     * 子Reactor负责IO读写和业务处理
     */
    static class SubReactor implements Runnable {
        private final Selector selector;
        private final ExecutorService pool;

        public SubReactor() throws IOException {
            selector = Selector.open();
            pool = Executors.newFixedThreadPool(2);
        }

        public void register(SocketChannel client) throws IOException {
            selector.wakeup();
            client.register(selector, SelectionKey.OP_READ);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            pool.submit(() -> handleRead(client));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleRead(SocketChannel client) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int read = client.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    String msg = new String(buffer.array(), 0, buffer.limit());
                    System.out.println("[SubReactor] Received: " + msg);
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
}
