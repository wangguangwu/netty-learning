package com.wangguangwu.netty.dubborpc.proxy;

import com.wangguangwu.netty.dubborpc.protocol.RpcRequest;
import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import com.wangguangwu.netty.dubborpc.transport.RpcFuture;
import com.wangguangwu.netty.dubborpc.transport.RpcRequestManager;
import com.wangguangwu.netty.dubborpc.transport.client.NettyClient;
import com.wangguangwu.netty.dubborpc.util.IdGenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * RPC客户端代理
 * 使用动态代理将接口方法调用转换为RPC请求
 *
 * @author wangguangwu
 */
public class RpcClientProxy implements InvocationHandler {

    /**
     * Netty客户端
     */
    private final NettyClient client;

    /**
     * 请求超时时间（毫秒）
     */
    private final long timeoutMillis;

    /**
     * 构造函数
     *
     * @param client        Netty客户端
     * @param timeoutMillis 请求超时时间（毫秒）
     */
    public RpcClientProxy(NettyClient client, long timeoutMillis) {
        this.client = client;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * 创建代理对象
     *
     * @param interfaceClass 接口类
     * @param <T>            接口类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceClass) {
        // 使用JDK动态代理创建代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                this
        );
    }

    /**
     * 处理代理对象的方法调用
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return 调用结果
     * @throws Throwable 调用异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object类的方法，直接调用
        if (Object.class == method.getDeclaringClass()) {
            return method.invoke(this, args);
        }
        
        // 创建RPC请求
        RpcRequest request = new RpcRequest();
        request.setRequestId(IdGenerator.generateId());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        
        // 发送请求并等待响应
        try {
            // 同步调用
            Object result = client.syncCall(request, timeoutMillis);
            
            // 处理响应
            if (result instanceof RpcResponse response) {
                // 检查是否有错误
                if (response.getErrorMessage() != null) {
                    throw new RuntimeException("RPC调用失败: " + response.getErrorMessage());
                }
                
                // 返回结果
                return response.getResult();
            } else {
                throw new RuntimeException("未知的响应类型: " + result.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("RPC调用异常: " + e.getMessage(), e);
        }
    }
}
