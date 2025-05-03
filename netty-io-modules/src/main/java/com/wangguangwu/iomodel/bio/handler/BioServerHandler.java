package com.wangguangwu.iomodel.bio.handler;

import com.wangguangwu.iomodel.common.LoggerUtil;

import java.io.*;
import java.net.Socket;

/**
 * 服务端通用客户端处理工具类
 * 封装 BIO 处理逻辑，避免重复代码
 *
 * @author wangguangwu
 */
public class BioServerHandler {

    public static void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String line = in.readLine();
            LoggerUtil.info("收到客户端消息: " + line);
            out.write("服务端已收到\n");
            out.flush();
        } catch (IOException e) {
            LoggerUtil.error("处理客户端消息异常: " + e.getMessage());
        }
    }
}
