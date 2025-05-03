package com.wangguangwu.iomodel.bio;

import com.wangguangwu.iomodel.bio.handler.BioServerHandler;
import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO 单线程版示例
 * 阻塞式 IO，单线程处理所有客户端。
 * 适合低并发场景，易于理解。
 *
 * @author wangguangwu
 */
public class BioSingleThreadServer {

    /**
     * 服务器主入口，负责启动和异常捕获。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            LoggerUtil.error("服务器启动失败: " + e.getMessage());
        }
    }

    /**
     * 启动 BIO 单线程服务器，监听指定端口并循环接收客户端连接。
     *
     * @throws IOException 启动或接收连接时发生的 IO 异常
     */
    private static void startServer() throws IOException {
        System.out.println("BIO 单线程服务器启动，监听端口 " + 8080 + " ...");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    LoggerUtil.info("客户端已连接: " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                } catch (IOException e) {
                    LoggerUtil.error("客户端处理异常: " + e.getMessage());
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
        BioServerHandler.handleClient(clientSocket);
    }
}

