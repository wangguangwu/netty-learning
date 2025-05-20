package com.wangguangwu.netty.pipeline.core.handlers;

import com.wangguangwu.netty.pipeline.api.Handler;
import com.wangguangwu.netty.pipeline.api.HandlerContext;

/**
 * 抛出异常的处理器
 * 用于测试异常传播机制
 *
 * @author wangguangwu
 */
public class ExceptionThrowingHandler implements Handler {
    
    @Override
    public void handle(HandlerContext ctx, Object request) throws Exception {
        System.out.println("ExceptionThrowingHandler 处理请求: " + request);
        // 抛出异常，会被直接传递到尾节点处理
        throw new RuntimeException("模拟处理异常");
    }
    
    @Override
    public void exceptionCaught(HandlerContext ctx, Throwable cause) {
        System.out.println("ExceptionThrowingHandler 捕获异常: " + cause.getMessage());
        // 传递给下一个处理器
        ctx.next().fireExceptionCaught(cause);
    }
}
