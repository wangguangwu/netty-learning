package com.wangguangwu.netty.protocoltcp.handler;

import com.wangguangwu.netty.protocoltcp.model.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.UUID;

/**
 * 服务器消息处理器
 * 处理客户端发送的消息并返回响应
 *
 * @author wangguangwu
 */
public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    
    /**
     * 接收到的消息计数器
     */
    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 获取消息内容
        int length = msg.getLength();
        String content = msg.getContentAsString();
        
        // 打印接收到的消息
        System.out.println("\n服务器接收到消息:");
        System.out.println("长度: " + length);
        System.out.println("内容: " + content);
        System.out.println("消息计数: " + (++this.count));
        
        // 构建响应消息
        String responseContent = UUID.randomUUID().toString();
        Message response = new Message(responseContent);
        
        // 发送响应
        ctx.writeAndFlush(response);
        System.out.println("服务器发送响应: " + responseContent);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端连接: " + ctx.channel().remoteAddress());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("客户端断开连接: " + ctx.channel().remoteAddress());
        System.out.println("服务器共处理 " + count + " 条消息");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("服务器异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
