package com.wangguangwu.iomodel.bio;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * BIO 并发客户端，用于测试线程池服务器
 *
 * @author wangguangwu
 */
public class BioThreadPoolClient {

    /**
     * 客户端主入口，负责并发测试和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            runConcurrentClients();
        } catch (InterruptedException e) {
            System.err.println("主线程中断: " + e.getMessage());
        }
    }

    /**
     * 并发启动多个客户端连接 BIO 线程池服务器。
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
        System.out.println("所有 BIO 线程池客户端请求完成");
    }

    /**
     * 单个客户端向服务器发送消息并接收响应。
     *
     * @param idx   客户端编号
     * @param latch 并发同步器
     */
    private static void sendMessage(int idx, CountDownLatch latch) {
        try (Socket socket = new Socket("localhost", 8081);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.write("你好，BIO 线程池服务器！我是客户端 " + idx + "\n");
            out.flush();
            String resp = in.readLine();
            System.out.println("客户端 " + idx + " 收到响应: " + resp);
        } catch (IOException e) {
            System.err.println("客户端 " + idx + " 异常: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}

