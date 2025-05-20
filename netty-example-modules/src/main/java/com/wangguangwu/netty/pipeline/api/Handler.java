package com.wangguangwu.netty.pipeline.api;

/**
 * 处理器接口
 * 类似于Netty的ChannelHandler
 *
 * @author wangguangwu
 */
public interface Handler {
    
    /**
     * 处理请求
     *
     * @param ctx 处理器上下文
     * @param request 请求对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    void handle(HandlerContext ctx, Object request) throws Exception;
    
    /**
     * 异常处理
     *
     * @param ctx 处理器上下文
     * @param cause 异常原因
     */
    void exceptionCaught(HandlerContext ctx, Throwable cause);
}
