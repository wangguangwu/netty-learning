package com.wangguangwu.netty.dubborpc.transport.server;

import com.wangguangwu.netty.dubborpc.protocol.RpcRequest;
import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import com.wangguangwu.netty.dubborpc.registry.ServiceRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * RPC服务端处理器
 * 处理客户端发送的RPC请求并返回响应
 *
 * @author wangguangwu
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * 服务注册表
     */
    private final ServiceRegistry serviceRegistry;

    /**
     * 构造函数
     *
     * @param serviceRegistry 服务注册表
     */
    public RpcServerHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // 创建响应对象
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            // 处理请求并设置响应结果
            Object result = handleRequest(request);
            response.setResult(result);
        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
            e.printStackTrace();
        }

        // 发送响应
        ctx.writeAndFlush(response);
        System.out.println("RPC服务端已发送响应: " + response);
    }

    /**
     * 处理RPC请求
     *
     * @param request RPC请求对象
     * @return 方法调用结果
     * @throws Exception 处理过程中的异常
     */
    private Object handleRequest(RpcRequest request) throws Exception {
        // 获取服务实例
        String interfaceName = request.getInterfaceName();
        Object serviceInstance = serviceRegistry.getService(interfaceName);

        if (serviceInstance == null) {
            throw new RuntimeException("找不到服务实现: " + interfaceName);
        }

        // 获取方法参数类型和参数值
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 获取服务实例的类对象
        Class<?> serviceClass = serviceInstance.getClass();

        // 查找并调用方法
        Method method = serviceClass.getMethod(request.getMethodName(), parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceInstance, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("RPC服务端异常: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
