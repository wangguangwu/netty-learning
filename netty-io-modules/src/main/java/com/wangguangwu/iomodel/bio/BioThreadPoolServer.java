package com.wangguangwu.iomodel.bio;

import com.wangguangwu.iomodel.bio.handler.BioServerHandler;
import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BIO 线程池版示例
 * 每个客户端连接交由线程池处理，提升并发能力。
 *
 * @author wangguangwu
 */
public class BioThreadPoolServer {

    /**
     * 服务器主入口，负责启动和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            startServer(8081);
        } catch (IOException e) {
            LoggerUtil.error("服务器启动失败: " + e.getMessage());
        }
    }

    /**
     * 启动 BIO 线程池服务器，监听指定端口并循环接收客户端连接。
     *
     * @param port 监听端口
     * @throws IOException 启动或接收连接时发生的 IO 异常
     */
    private static void startServer(int port) throws IOException {
        System.out.println("BIO 线程池服务器启动，监听端口 " + port + " ...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ExecutorService pool = Executors.newFixedThreadPool(4);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.execute(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    LoggerUtil.error("连接接收异常: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 处理单个客户端连接。
     *
     * @param clientSocket 客户端 Socket
     */
    private static void handleClient(Socket clientSocket) {
        try {
            LoggerUtil.info("客户端已连接: " + clientSocket.getInetAddress());
            BioServerHandler.handleClient(clientSocket);
        } catch (Exception e) {
            LoggerUtil.error("客户端处理异常: " + e.getMessage());
        }
    }
}

