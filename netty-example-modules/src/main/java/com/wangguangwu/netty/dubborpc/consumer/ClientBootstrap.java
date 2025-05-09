package com.wangguangwu.netty.dubborpc.consumer;

import com.wangguangwu.netty.dubborpc.api.GreetingService;
import com.wangguangwu.netty.dubborpc.proxy.RpcClientProxy;
import com.wangguangwu.netty.dubborpc.transport.client.NettyClient;

/**
 * 客户端启动类
 * 用于启动RPC客户端并调用远程服务
 *
 * @author wangguangwu
 */
public class ClientBootstrap {

    // 服务端地址和端口
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    
    // 默认超时时间（毫秒）
    private static final long DEFAULT_TIMEOUT = 5000;

    public static void main(String[] args) {
        // 创建Netty客户端
        NettyClient client = new NettyClient(SERVER_HOST, SERVER_PORT);
        
        try {
            // 启动客户端
            System.out.println("RPC客户端正在连接服务器...");
            client.start();
            
            // 创建RPC客户端代理
            RpcClientProxy proxy = new RpcClientProxy(client, DEFAULT_TIMEOUT);
            
            // 获取远程服务代理
            GreetingService greetingService = proxy.getProxy(GreetingService.class);
            
            // 调用远程方法
            for (int i = 0; i < 5; i++) {
                String result = greetingService.greet("User " + i);
                System.out.println("调用结果: " + result);
                
                // 暂停一下，方便观察
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("RPC客户端调用失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭客户端
            client.shutdown();
        }
    }
}
