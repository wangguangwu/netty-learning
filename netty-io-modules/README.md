# netty-io-modules

## 项目说明

本项目演示了 Java 中三种主要 I/O 模型（BIO、NIO、AIO）的实现方式和使用场景，通过简单的服务端/客户端示例展示它们的工作原理和编程模式。每个示例都包含详细注释，特别是对 ByteBuffer 操作和异常处理的解释，便于初学者理解。

## 各种 I/O 模型简介与原理

### 1. BIO（阻塞式 IO）
- **简介**：传统的 Java IO，服务端每接收一个客户端连接，就会创建一个线程处理。
- **原理**：`ServerSocket.accept()` 和 `InputStream.read()` 等方法会阻塞，直到有连接或数据到来。
- **实现方式**：`java.net.ServerSocket`、`java.net.Socket`。
- **优缺点**：实现简单，适合低并发场景，但线程资源消耗大。

### 2. NIO（非阻塞式 IO）
- **简介**：Java 1.4 引入，支持多路复用，单线程可管理多个连接。
- **原理**：基于 `Selector`、`Channel`，通过事件驱动机制实现非阻塞 IO。
- **实现方式**：`java.nio.channels.Selector`、`ServerSocketChannel`、`SocketChannel`。
- **优缺点**：适合高并发，编程复杂度高，调试困难。

### 3. AIO（异步 IO）
- **简介**：Java 7 引入的异步 IO。
- **原理**：基于回调（`CompletionHandler`），操作异步执行。
- **实现方式**：`AsynchronousServerSocketChannel`、`AsynchronousSocketChannel`。
- **注意**：在 Linux 下底层实现并非真正的异步 IO。
- **优缺点**：适合连接数极多且 IO 操作耗时的场景，API 复杂。

---

## 目录结构说明

```
netty-io-modules/
├── src/main/java/com/wangguangwu/iomodel/
│   ├── bio/      # 阻塞 IO 示例（单线程与线程池）
│   ├── nio/      # 非阻塞 IO 示例（Selector 多路复用）
│   ├── aio/      # 异步 IO 示例（CompletionHandler）
│   └── common/   # 工具类、日志工具等
```

---

## 示例运行说明

- **服务端**：
  - BIO 单线程：运行 `bio/BioSingleThreadServer.java`（监听 8080）
  - BIO 线程池：运行 `bio/BioThreadPoolServer.java`（监听 8081）
  - NIO Selector：运行 `nio/NioSelectorServer.java`（监听 8082）
  - AIO：运行 `aio/AioServer.java`（监听 8083）
- **客户端**：
  - 统一运行 `client/BioClient.java`、`client/NioClient.java`、`client/AioClient.java`，可分别连接不同端口体验不同模型。

所有示例均可直接运行 main 方法进行测试。

---

## 各模型核心代码片段

### BIO 单线程核心
```java
ServerSocket serverSocket = new ServerSocket(8080);
while (true) {
    Socket clientSocket = serverSocket.accept(); // 阻塞
    handleClient(clientSocket);
}
```

### BIO 线程池核心
```java
ExecutorService pool = Executors.newFixedThreadPool(4);
while (true) {
    Socket clientSocket = serverSocket.accept();
    pool.execute(() -> handleClient(clientSocket));
}
```

### NIO 多路复用核心
```java
Selector selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false); // 非阻塞模式才能用于 selector
serverChannel.register(selector, SelectionKey.OP_ACCEPT);
while (!Thread.currentThread().isInterrupted()) { // 优雅退出
    selector.select(); // 多路复用阻塞
    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
    while (iter.hasNext()) {
        SelectionKey key = iter.next();
        iter.remove(); // 必须移除，否则会重复处理
        if (key.isAcceptable()) { /* 处理新连接 */ }
        else if (key.isReadable()) { /* 处理可读事件 */ }
    }
}
```

### AIO 核心
```java
AsynchronousServerSocketChannel serverChannel =
    AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(8083));
serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
    @Override
    public void completed(AsynchronousSocketChannel client, Void attachment) {
        // 继续接收下一个连接
        serverChannel.accept(null, this); // 递归接收
        // 读取客户端数据
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buf) {
                buf.flip(); // 切换为读模式
                // 处理数据...
            }
            @Override
            public void failed(Throwable exc, ByteBuffer buf) { /* 处理异常 */ }
        });
    }
    @Override
    public void failed(Throwable exc, Void attachment) { /* 处理异常 */ }
});
```

---

## 推荐使用场景与优缺点对比

| 模型 | 推荐场景 | 优点 | 缺点 |
| ---- | -------- | ---- | ---- |
| BIO  | 低并发、实现简单 | 编程简单 | 资源消耗大，扩展性差 |
| NIO  | 高并发、网络服务器 | 高性能、资源占用少 | 编程复杂，调试难 |
| AIO  | 超高并发、长连接 | 真正异步、适合海量连接 | API 复杂，Linux 下非真正 AIO |

---

## 其他说明
- `common` 包提供日志工具、字符串编码工具等，方便各模型复用。
- 可根据需要扩展消息协议类。
- 推荐使用 JDK 17 及以上。

---

> 如需运行截图，可在 Markdown 中插入图片展示。
