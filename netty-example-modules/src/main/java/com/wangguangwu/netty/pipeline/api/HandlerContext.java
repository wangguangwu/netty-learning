package com.wangguangwu.netty.pipeline.api;

/**
 * 处理器上下文接口
 * 定义处理器上下文的核心功能
 *
 * @author wangguangwu
 */
public interface HandlerContext {
    
    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    String name();
    
    /**
     * 获取处理器
     *
     * @return 处理器实例
     */
    Handler handler();
    
    /**
     * 获取下一个处理器上下文
     *
     * @return 下一个处理器上下文
     */
    HandlerContext next();
    
    /**
     * 获取前一个处理器上下文
     *
     * @return 前一个处理器上下文
     */
    HandlerContext prev();
    
    /**
     * 获取所属的Pipeline
     *
     * @return Pipeline实例
     */
    Pipeline pipeline();
    
    /**
     * 触发处理请求
     * 将请求传递给当前处理器处理
     *
     * @param request 请求对象
     */
    void fireHandle(Object request);
    
    /**
     * 将请求传递给下一个处理器
     *
     * @param request 请求对象
     */
    void fireNext(Object request);
    
    /**
     * 处理异常
     * 
     * @param cause 异常原因
     */
    void fireExceptionCaught(Throwable cause);
}
