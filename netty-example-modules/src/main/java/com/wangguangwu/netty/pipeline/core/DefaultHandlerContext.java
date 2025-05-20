package com.wangguangwu.netty.pipeline.core;

import com.wangguangwu.netty.pipeline.api.Handler;
import com.wangguangwu.netty.pipeline.api.HandlerContext;
import com.wangguangwu.netty.pipeline.api.Pipeline;

/**
 * 默认处理器上下文实现
 *
 * @author wangguangwu
 */
public class DefaultHandlerContext implements HandlerContext {
    
    /**
     * 当前处理器
     */
    private final Handler handler;
    
    /**
     * 下一个处理器上下文
     */
    private HandlerContext next;
    
    /**
     * 前一个处理器上下文
     */
    private HandlerContext prev;
    
    /**
     * 所属的Pipeline
     */
    private final Pipeline pipeline;
    
    /**
     * 处理器名称
     */
    private final String name;
    
    /**
     * 构造函数
     *
     * @param pipeline 所属的Pipeline
     * @param name 处理器名称
     * @param handler 处理器实例
     */
    public DefaultHandlerContext(Pipeline pipeline, String name, Handler handler) {
        this.pipeline = pipeline;
        this.name = name;
        this.handler = handler;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    @Override
    public Handler handler() {
        return handler;
    }
    
    /**
     * 设置下一个处理器上下文
     */
    public void setNext(HandlerContext next) {
        this.next = next;
    }
    
    @Override
    public HandlerContext next() {
        return next;
    }
    
    /**
     * 设置前一个处理器上下文
     */
    public void setPrev(HandlerContext prev) {
        this.prev = prev;
    }
    
    @Override
    public HandlerContext prev() {
        return prev;
    }
    
    @Override
    public Pipeline pipeline() {
        return pipeline;
    }
    
    @Override
    public void fireHandle(Object request) {
        try {
            handler.handle(this, request);
        } catch (Throwable cause) {
            // 发生异常时，直接跳转到尾节点处理异常
            pipeline.fireCatchException(this, cause);
        }
    }
    
    @Override
    public void fireNext(Object request) {
        if (next != null) {
            next.fireHandle(request);
        }
    }
    
    @Override
    public void fireExceptionCaught(Throwable cause) {
        try {
            handler.exceptionCaught(this, cause);
        } catch (Throwable error) {
            // 如果异常处理器本身抛出异常，记录错误并继续传播
            System.err.println("处理异常时发生错误: " + error.getMessage());
            error.printStackTrace();
            
            // 如果不是尾节点，继续传播异常
            if (next != null) {
                next.fireExceptionCaught(cause);
            }
        }
    }
}
