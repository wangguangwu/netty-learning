package com.wangguangwu.netty.pipeline.api;

/**
 * 自定义异常处理器接口
 * 用于替换默认的尾节点异常处理逻辑
 *
 * @author wangguangwu
 */
public interface ExceptionHandler {
    
    /**
     * 处理异常
     *
     * @param cause 异常原因
     */
    void handle(Throwable cause);
}
