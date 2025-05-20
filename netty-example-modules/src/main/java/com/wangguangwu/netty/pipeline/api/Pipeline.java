package com.wangguangwu.netty.pipeline.api;

/**
 * 责任链管理接口
 * 定义责任链的核心功能
 *
 * @author wangguangwu
 */
public interface Pipeline {
    
    /**
     * 设置自定义异常处理器
     *
     * @param exceptionHandler 异常处理器
     * @return 当前Pipeline实例，支持链式调用
     */
    Pipeline setExceptionHandler(ExceptionHandler exceptionHandler);
    
    /**
     * 获取自定义异常处理器
     *
     * @return 异常处理器
     */
    ExceptionHandler exceptionHandler();
    
    /**
     * 在责任链末尾添加处理器
     *
     * @param name 处理器名称
     * @param handler 处理器实例
     * @return 当前Pipeline实例，支持链式调用
     */
    Pipeline addLast(String name, Handler handler);
    
    /**
     * 在责任链末尾添加处理器，使用默认名称
     *
     * @param handler 处理器实例
     * @return 当前Pipeline实例，支持链式调用
     */
    Pipeline addLast(Handler handler);
    
    /**
     * 移除指定名称的处理器
     *
     * @param name 处理器名称
     * @return 被移除的处理器
     */
    Handler remove(String name);
    
    /**
     * 获取指定名称的处理器上下文
     *
     * @param name 处理器名称
     * @return 处理器上下文
     */
    HandlerContext context(String name);
    
    /**
     * 启动责任链处理
     *
     * @param request 请求对象
     */
    void start(Object request);
    
    /**
     * 触发异常处理
     * 直接跳转到尾节点处理异常
     *
     * @param ctx 发生异常的上下文
     * @param cause 异常原因
     */
    void fireCatchException(HandlerContext ctx, Throwable cause);
}
