package com.wangguangwu.netty.websocket;

import com.wangguangwu.netty.websocket.handler.HttpStaticFileHandler;
import com.wangguangwu.netty.websocket.handler.TextWebSocketFrameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * WebSocket 服务器
 * 基于 Netty 实现的 WebSocket 服务器，支持浏览器与服务器的双向通信
 * 并提供静态文件服务，可以直接访问 HTML 页面
 *
 * @author wangguangwu
 */
public class WebSocketServer {

    /**
     * 服务器端口
     */
    private static final int PORT = 8080;

    /**
     * WebSocket 路径
     */
    private static final String WEBSOCKET_PATH = "/websocket";

    /**
     * 静态文件根目录
     */
    private static final String WEB_ROOT = getWebRoot();

    /**
     * 获取静态文件根目录
     * 
     * @return 静态文件根目录的绝对路径
     */
    private static String getWebRoot() {
        // 尝试从类路径加载资源
        URL resourceUrl = WebSocketServer.class.getClassLoader().getResource("static");
        if (resourceUrl != null) {
            try {
                // 使用 URI 来处理可能包含空格的路径
                return new File(resourceUrl.toURI()).getAbsolutePath();
            } catch (URISyntaxException e) {
                // 如果 URI 转换失败，回退到直接使用路径
                return new File(resourceUrl.getPath()).getAbsolutePath();
            }
        }
        
        // 如果无法从类路径加载，尝试使用相对路径
        String classPath = WebSocketServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File projectRoot = new File(classPath).getParentFile().getParentFile();
        
        // 首先尝试 target/classes/static 目录
        File targetStaticDir = new File(projectRoot, "static");
        if (targetStaticDir.exists() && targetStaticDir.isDirectory()) {
            return targetStaticDir.getAbsolutePath();
        }
        
        // 然后尝试 src/main/resources/static 目录
        return new File(projectRoot, "src/main/resources/static").getAbsolutePath();
    }

    /**
     * 启动 WebSocket 服务器
     */
    public static void main(String[] args) throws Exception {
        // 创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建服务器启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 配置服务器
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 为 bossGroup 添加日志处理器
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 设置TCP参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加 HTTP 协议编解码器
                            // WebSocket 是基于 HTTP 协议的，所以需要 HTTP 编解码器
                            pipeline.addLast(new HttpServerCodec());

                            // 添加块写处理器
                            // 以块的方式来写数据，支持大数据的分块传输
                            pipeline.addLast(new ChunkedWriteHandler());

                            // 添加 HTTP 消息聚合器
                            // HTTP 请求可能会被分成多个部分，HttpObjectAggregator 可以将多个部分聚合成一个完整的 HTTP 请求或响应
                            // 参数 8192 表示聚合的消息内容的最大长度
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            
                            // 添加路由处理器，根据请求路径决定使用哪个处理器
                            pipeline.addLast(new HttpRequestRouter());
                            
                            // 添加 WebSocket 压缩处理器
                            pipeline.addLast(new WebSocketServerCompressionHandler());

                            // 添加 WebSocket 协议处理器
                            // 用于处理 WebSocket 握手和控制帧（Close、Ping、Pong）
                            // 参数 WEBSOCKET_PATH 表示 WebSocket 的路径
                            // 当客户端通过 ws://server:port/websocket 连接时，会被此处理器处理
                            pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));

                            // 添加自定义的 WebSocket 帧处理器
                            // 用于处理 WebSocket 的文本帧，即处理业务逻辑
                            pipeline.addLast(new TextWebSocketFrameHandler());
                        }
                    });

            // 启动服务器
            System.out.println("[INFO] WebSocket 服务器启动中，端口: " + PORT);
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("[INFO] WebSocket 服务器启动成功");
            System.out.println("[INFO] 静态文件根目录: " + WEB_ROOT);
            System.out.println("[INFO] 可通过浏览器访问 http://localhost:" + PORT + "/index.html 来测试");

            // 等待服务器关闭
            channelFuture.channel().closeFuture().sync();

        } finally {
            // 优雅关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("[INFO] WebSocket 服务器已关闭");
        }
    }
    
    /**
     * HTTP 请求路由处理器
     * 根据请求路径决定使用哪个处理器
     */
    private static class HttpRequestRouter extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            // 如果是 WebSocket 握手请求，则直接传递给下一个处理器
            if (isWebSocketHandshake(request)) {
                ctx.fireChannelRead(request.retain());
                return;
            }
            
            // 否则使用静态文件处理器处理 HTTP 请求
            HttpStaticFileHandler fileHandler = new HttpStaticFileHandler(WEB_ROOT);
            fileHandler.handleHttpRequest(ctx, request);
        }
        
        /**
         * 判断是否是 WebSocket 握手请求
         */
        private boolean isWebSocketHandshake(FullHttpRequest request) {
            return request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
                   request.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true) &&
                   request.uri().startsWith(WEBSOCKET_PATH);
        }
    }
}
