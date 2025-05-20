package com.wangguangwu.netty.pipeline.example;

import com.wangguangwu.netty.pipeline.api.Pipeline;
import com.wangguangwu.netty.pipeline.core.PipelineFactory;
import com.wangguangwu.netty.pipeline.core.handlers.ExceptionThrowingHandler;
import com.wangguangwu.netty.pipeline.core.handlers.LoggingHandler;

/**
 * 责任链框架使用示例
 *
 * @author wangguangwu
 */
public class PipelineExample {
    
    public static void main(String[] args) {
        System.out.println("===== 测试正常流程 =====");
        testNormalFlow();
        
        System.out.println("\n===== 测试异常流程（默认异常处理器）=====");
        testExceptionFlow();
        
        System.out.println("\n===== 测试异常流程（自定义异常处理器）=====");
        testCustomExceptionHandler();
    }
    
    /**
     * 测试正常流程
     */
    private static void testNormalFlow() {
        // 创建责任链
        Pipeline pipeline = PipelineFactory.createPipeline();
        
        // 添加处理器
        pipeline.addLast("handler1", new LoggingHandler("Handler1"));
        pipeline.addLast("handler2", new LoggingHandler("Handler2"));
        pipeline.addLast("handler3", new LoggingHandler("Handler3"));
        
        // 启动责任链处理
        pipeline.start("Hello, Pipeline!");
    }
    
    /**
     * 测试异常流程
     */
    private static void testExceptionFlow() {
        // 创建责任链
        Pipeline pipeline = PipelineFactory.createPipeline();
        
        // 添加处理器
        pipeline.addLast("handler1", new LoggingHandler("Handler1"));
        pipeline.addLast("handler2", new ExceptionThrowingHandler());
        pipeline.addLast("handler3", new LoggingHandler("Handler3")); // 这个处理器不会被执行
        
        // 启动责任链处理
        pipeline.start("Hello, Exception!");
    }
    
    /**
     * 测试自定义异常处理器
     */
    private static void testCustomExceptionHandler() {
        // 创建责任链
        Pipeline pipeline = PipelineFactory.createPipeline();
        
        // 设置自定义异常处理器
        pipeline.setExceptionHandler(cause -> {
            System.out.println("自定义异常处理器: " + cause.getMessage());
            System.out.println("执行自定义恢复逻辑...");
        });
        
        // 添加处理器
        pipeline.addLast("handler1", new LoggingHandler("Handler1"));
        pipeline.addLast("handler2", new ExceptionThrowingHandler());
        pipeline.addLast("handler3", new LoggingHandler("Handler3")); // 这个处理器不会被执行
        
        // 启动责任链处理
        pipeline.start("Hello, Custom Exception Handler!");
    }
}
