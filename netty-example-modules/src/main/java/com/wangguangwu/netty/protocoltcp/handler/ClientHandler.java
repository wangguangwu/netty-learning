package com.wangguangwu.netty.protocoltcp.handler;

import com.wangguangwu.netty.protocoltcp.model.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 客户端消息处理器
 * 负责发送消息到服务器并处理服务器响应
 *
 * @author wangguangwu
 */
public class ClientHandler extends SimpleChannelInboundHandler<Message> {
    
    /**
     * 接收到的消息计数器
     */
    private int count;
    
    /**
     * 测试消息数量
     */
    private static final int MESSAGE_COUNT = 5;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("客户端连接到服务器: " + ctx.channel().remoteAddress());
        System.out.println("开始发送消息...");
        
        // 发送多条测试消息
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String content = "消息 #" + i + ": 今天天气很好，适合编程";
            Message message = new Message(content);
            
            ctx.writeAndFlush(message);
            System.out.println("客户端发送消息: " + content);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 获取消息内容
        int length = msg.getLength();
        String content = msg.getContentAsString();
        
        // 打印接收到的消息
        System.out.println("\n客户端接收到消息:");
        System.out.println("长度: " + length);
        System.out.println("内容: " + content);
        System.out.println("消息计数: " + (++this.count));
        
        // 如果收到足够的响应，可以考虑关闭连接
        if (this.count >= MESSAGE_COUNT) {
            System.out.println("已收到所有响应，准备关闭连接...");
            // 这里不主动关闭，让用户手动关闭以观察结果
            // ctx.close();
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("与服务器断开连接");
        System.out.println("客户端共接收 " + count + " 条响应");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("客户端异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
