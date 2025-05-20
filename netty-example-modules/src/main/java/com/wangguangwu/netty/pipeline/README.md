# 类Netty责任链（ChannelPipeline）实现

## 项目概述

本项目实现了一个类似Netty的责任链模式（ChannelPipeline），支持异常中断并统一处理。该实现参考了Netty的ChannelPipeline设计，但进行了简化，专注于责任链的核心功能。

## 代码结构

项目采用分层架构，将接口和实现分离，便于扩展和维护：

```
com.wangguangwu.netty.pipeline
├── api                 # 接口定义
│   ├── Handler.java           # 处理器接口
│   ├── HandlerContext.java    # 处理器上下文接口
│   ├── Pipeline.java          # 责任链管理接口
│   └── ExceptionHandler.java  # 异常处理器接口
├── core                # 核心实现
│   ├── DefaultHandlerContext.java  # 默认处理器上下文实现
│   ├── DefaultPipeline.java        # 默认责任链管理实现
│   ├── PipelineFactory.java        # 责任链工厂类
│   └── handlers                    # 内置处理器实现
│       ├── LoggingHandler.java          # 日志记录处理器
│       └── ExceptionThrowingHandler.java # 异常抛出处理器（测试用）
└── example             # 使用示例
    └── PipelineExample.java      # 责任链使用示例
```

## 核心组件

### 1. Handler（处理器接口）

处理器是责任链中的核心处理单元，负责处理请求和异常。

```java
public interface Handler {
    // 处理请求
    void handle(HandlerContext ctx, Object request) throws Exception;
    
    // 异常处理
    void exceptionCaught(HandlerContext ctx, Throwable cause);
}
```

### 2. HandlerContext（处理器上下文接口）

HandlerContext封装了Handler，并维护责任链的链表结构，负责请求和异常的传递。

```java
public interface HandlerContext {
    // 获取处理器名称
    String name();
    
    // 获取处理器
    Handler handler();
    
    // 获取下一个处理器上下文
    HandlerContext next();
    
    // 获取前一个处理器上下文
    HandlerContext prev();
    
    // 触发处理请求
    void fireHandle(Object request);
    
    // 将请求传递给下一个处理器
    void fireNext(Object request);
    
    // 处理异常
    void fireExceptionCaught(Throwable cause);
}
```

### 3. Pipeline（责任链管理接口）

Pipeline管理整个责任链结构，包括添加/移除处理器、启动责任链处理等功能。

```java
public interface Pipeline {
    // 设置自定义异常处理器
    Pipeline setExceptionHandler(ExceptionHandler exceptionHandler);
    
    // 在责任链末尾添加处理器
    Pipeline addLast(String name, Handler handler);
    
    // 移除指定名称的处理器
    Handler remove(String name);
    
    // 启动责任链处理
    void start(Object request);
    
    // 触发异常处理
    void fireCatchException(HandlerContext ctx, Throwable cause);
}
```

### 4. ExceptionHandler（自定义异常处理器接口）

允许用户自定义异常处理逻辑，替换默认的异常处理方式。

```java
public interface ExceptionHandler {
    void handle(Throwable cause);
}
```

## 设计特点

1. **接口与实现分离**：通过接口定义核心功能，实现类提供具体实现，便于扩展和替换。
2. **双向链表结构**：每个HandlerContext持有前后节点的引用，形成双向链表。
3. **头尾节点**：Pipeline包含头节点（HeadContext）和尾节点（TailContext），所有请求从头节点开始，尾节点负责统一处理异常。
4. **异常中断机制**：任意节点抛出异常时，会立即中断后续处理，直接跳转到尾节点进行异常处理。
5. **线程安全**：通过同步机制确保在多线程环境下安全地修改责任链结构。
6. **自定义异常处理**：支持设置自定义的异常处理器，灵活处理不同场景的异常。
7. **工厂模式**：使用PipelineFactory创建Pipeline实例，隐藏实现细节。

## 异常传播机制

当责任链中的任意Handler抛出异常时，异常传播机制如下：

1. HandlerContext捕获异常，调用pipeline.fireCatchException()
2. Pipeline直接将异常传递给尾节点（TailContext）
3. 尾节点执行异常处理逻辑（默认或自定义）
4. 异常处理完成，不再继续传递请求

这种机制确保了异常能够被统一处理，且不会执行异常点之后的Handler。

## 使用示例

### 基本用法

```java
// 创建责任链
Pipeline pipeline = PipelineFactory.createPipeline();

// 添加处理器
pipeline.addLast("handler1", new LoggingHandler("Handler1"));
pipeline.addLast("handler2", new BusinessHandler());
pipeline.addLast("handler3", new ResponseHandler());

// 启动责任链处理
pipeline.start("请求数据");
```

### 自定义异常处理

```java
// 创建责任链
Pipeline pipeline = PipelineFactory.createPipeline();

// 设置自定义异常处理器
pipeline.setExceptionHandler(cause -> {
    System.out.println("自定义异常处理: " + cause.getMessage());
    // 执行恢复逻辑
});

// 添加处理器
pipeline.addLast("handler1", new LoggingHandler("Handler1"));
pipeline.addLast("handler2", new ExceptionThrowingHandler());

// 启动责任链处理
pipeline.start("请求数据");
```

## 与Netty的区别

1. **简化设计**：移除了Inbound/Outbound的区分，专注于单向请求处理。
2. **异常处理**：Netty中异常会沿着pipeline传播，而本实现中异常直接跳转到尾节点。
3. **无事件类型**：Netty支持多种事件类型，本实现只关注请求处理和异常处理。
4. **无Channel概念**：移除了与网络相关的Channel概念，使责任链更加通用。

## 扩展方向

1. **增加Inbound/Outbound区分**：支持请求和响应的双向处理。
2. **添加事件类型**：支持不同类型的事件处理。
3. **增加拦截器**：在责任链前后添加拦截器，实现更复杂的处理逻辑。
4. **支持异步处理**：引入Future/Promise机制，支持异步处理请求。

## 总结

本实现提供了一个简洁而功能完整的责任链框架，特别适合需要严格异常中断的场景。通过合理的设计，确保了异常能够被统一处理，同时保持了代码的可扩展性和可维护性。
