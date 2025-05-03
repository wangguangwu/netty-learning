package com.wangguangwu.iomodel.bio;

import java.io.*;
import java.net.Socket;

/**
 * BIO 客户端示例
 *
 * @author wangguangwu
 */
public class BioSingleClient {

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
     * 向指定服务器发送消息并接收响应。
     *
     * @throws IOException 网络异常
     */
    private static void sendMessage() throws IOException {
        try (Socket socket = new Socket("localhost", 8080);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.write("你好，BIO 服务器！" + "\n");
            out.flush();
            String resp = in.readLine();
            System.out.println("收到服务端响应: " + resp);
        }
    }
}

