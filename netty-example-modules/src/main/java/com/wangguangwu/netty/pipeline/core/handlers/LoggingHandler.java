package com.wangguangwu.netty.pipeline.core.handlers;

import com.wangguangwu.netty.pipeline.api.Handler;
import com.wangguangwu.netty.pipeline.api.HandlerContext;

/**
 * 日志记录处理器
 * 用于记录请求和异常信息
 *
 * @author wangguangwu
 */
public class LoggingHandler implements Handler {
    
    private final String name;
    
    public LoggingHandler(String name) {
        this.name = name;
    }
    
    @Override
    public void handle(HandlerContext ctx, Object request) {
        System.out.println(name + " 处理请求: " + request);
        // 传递给下一个处理器
        ctx.fireNext(request);
    }
    
    @Override
    public void exceptionCaught(HandlerContext ctx, Throwable cause) {
        System.out.println(name + " 捕获异常: " + cause.getMessage());
        // 传递给下一个处理器
        ctx.next().fireExceptionCaught(cause);
    }
}
