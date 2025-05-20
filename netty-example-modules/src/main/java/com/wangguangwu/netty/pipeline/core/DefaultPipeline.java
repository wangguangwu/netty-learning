package com.wangguangwu.netty.pipeline.core;

import com.wangguangwu.netty.pipeline.api.ExceptionHandler;
import com.wangguangwu.netty.pipeline.api.Handler;
import com.wangguangwu.netty.pipeline.api.HandlerContext;
import com.wangguangwu.netty.pipeline.api.Pipeline;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认责任链管理实现
 *
 * @author wangguangwu
 */
public class DefaultPipeline implements Pipeline {
    
    /**
     * 头节点上下文
     */
    private final DefaultHandlerContext head;
    
    /**
     * 尾节点上下文
     */
    private final DefaultHandlerContext tail;
    
    /**
     * 处理器名称到上下文的映射
     */
    private final Map<String, DefaultHandlerContext> name2ctx = new ConcurrentHashMap<>();
    
    /**
     * 自定义异常处理器
     */
    private ExceptionHandler exceptionHandler;
    
    /**
     * 构造函数
     * 初始化头尾节点
     */
    public DefaultPipeline() {
        // 创建头尾节点
        head = new DefaultHandlerContext(this, "HeadContext", new HeadHandler());
        tail = new DefaultHandlerContext(this, "TailContext", new TailHandler());
        
        // 初始化链表结构
        head.setNext(tail);
        tail.setPrev(head);
    }
    
    @Override
    public Pipeline setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }
    
    @Override
    public ExceptionHandler exceptionHandler() {
        return exceptionHandler;
    }
    
    @Override
    public Pipeline addLast(String name, Handler handler) {
        synchronized (this) {
            // 检查名称是否已存在
            if (name2ctx.containsKey(name)) {
                throw new IllegalArgumentException("处理器名称已存在: " + name);
            }
            
            // 创建新的上下文
            DefaultHandlerContext newCtx = new DefaultHandlerContext(this, name, handler);
            
            // 获取尾节点的前一个节点
            DefaultHandlerContext prev = (DefaultHandlerContext) tail.prev();
            
            // 更新链表引用
            newCtx.setPrev(prev);
            newCtx.setNext(tail);
            prev.setNext(newCtx);
            tail.setPrev(newCtx);
            
            // 添加到映射表
            name2ctx.put(name, newCtx);
        }
        
        return this;
    }
    
    @Override
    public Pipeline addLast(Handler handler) {
        return addLast(handler.getClass().getSimpleName() + "#0", handler);
    }
    
    @Override
    public Handler remove(String name) {
        DefaultHandlerContext ctx;
        
        synchronized (this) {
            ctx = name2ctx.remove(name);
            if (ctx == null) {
                throw new NoSuchElementException("处理器不存在: " + name);
            }
            
            // 更新链表引用
            DefaultHandlerContext prev = (DefaultHandlerContext) ctx.prev();
            DefaultHandlerContext next = (DefaultHandlerContext) ctx.next();
            prev.setNext(next);
            next.setPrev(prev);
        }
        
        return ctx.handler();
    }
    
    @Override
    public HandlerContext context(String name) {
        return name2ctx.get(name);
    }
    
    @Override
    public void start(Object request) {
        head.fireHandle(request);
    }
    
    @Override
    public void fireCatchException(HandlerContext ctx, Throwable cause) {
        // 直接跳转到尾节点处理异常
        tail.fireExceptionCaught(cause);
    }
    
    /**
     * 头节点处理器
     * 仅负责将请求传递给下一个处理器
     */
    private static class HeadHandler implements Handler {
        
        @Override
        public void handle(HandlerContext ctx, Object request) {
            // 头节点不处理请求，直接传递给下一个处理器
            ctx.fireNext(request);
        }
        
        @Override
        public void exceptionCaught(HandlerContext ctx, Throwable cause) {
            // 头节点不处理异常，传递给下一个处理器
            ctx.next().fireExceptionCaught(cause);
        }
    }
    
    /**
     * 尾节点处理器
     * 负责统一处理未被捕获的异常
     */
    private class TailHandler implements Handler {
        
        @Override
        public void handle(HandlerContext ctx, Object request) {
            // 尾节点是责任链的终点，不再传递请求
            System.out.println("请求到达责任链终点，未被处理: " + request);
        }
        
        @Override
        public void exceptionCaught(HandlerContext ctx, Throwable cause) {
            // 如果设置了自定义异常处理器，则使用自定义处理器
            if (exceptionHandler != null) {
                try {
                    exceptionHandler.handle(cause);
                } catch (Throwable error) {
                    // 自定义异常处理器本身出错，使用默认处理
                    System.err.println("自定义异常处理器出错: " + error.getMessage());
                    defaultExceptionHandle(cause);
                }
            } else {
                // 使用默认异常处理
                defaultExceptionHandle(cause);
            }
        }
        
        /**
         * 默认异常处理
         */
        private void defaultExceptionHandle(Throwable cause) {
            System.err.println("责任链异常统一处理: " + cause.getMessage());
            cause.printStackTrace();
        }
    }
}
