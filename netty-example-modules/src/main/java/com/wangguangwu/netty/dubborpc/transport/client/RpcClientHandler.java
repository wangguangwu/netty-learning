package com.wangguangwu.netty.dubborpc.transport.client;

import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import com.wangguangwu.netty.dubborpc.transport.RpcRequestManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * RPC客户端处理器
 * 处理服务端返回的RPC响应
 *
 * @author wangguangwu
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 请求管理器
     */
    private final RpcRequestManager requestManager;

    /**
     * 构造函数
     */
    public RpcClientHandler() {
        this.requestManager = RpcRequestManager.getINSTANCE();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        String requestId = response.getRequestId();
        System.out.println("RPC客户端收到响应: " + requestId);
        
        // 将响应传递给对应的请求
        requestManager.notifyResponse(requestId, response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("RPC客户端异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
