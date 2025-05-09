package com.wangguangwu.netty.dubborpc.provider;

import com.wangguangwu.netty.dubborpc.api.GreetingService;
import com.wangguangwu.netty.dubborpc.transport.server.NettyServer;

/**
 * Server Bootstrap class
 * Used to start the RPC server and register services
 *
 * @author wangguangwu
 */
public class ServerBootstrap {

    public static void main(String[] args) {
        // Create service implementation
        GreetingService greetingService = new GreetingServiceImpl();

        // Create RPC server (port 8080)
        NettyServer server = new NettyServer(8080);

        // Register service
        server.registerService(GreetingService.class, greetingService);

        try {
            // Start server
            System.out.println("RPC server is starting...");
            server.start();
        } catch (Exception e) {
            System.err.println("RPC server startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
