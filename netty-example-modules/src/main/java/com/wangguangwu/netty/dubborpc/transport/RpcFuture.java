package com.wangguangwu.netty.dubborpc.transport;

import com.wangguangwu.netty.dubborpc.protocol.RpcRequest;
import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * RPC异步结果处理类
 * 用于等待RPC调用结果返回
 *
 * @author wangguangwu
 */
public class RpcFuture {

    /**
     * 用于等待结果的闭锁
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    /**
     * RPC调用响应结果
     */
    private RpcResponse response;

    /**
     * 调用失败的异常
     */
    private Throwable cause;

    /**
     * 请求对象
     */
    private final RpcRequest request;

    /**
     * 创建一个新的RPC Future
     *
     * @param request 请求对象
     */
    public RpcFuture(RpcRequest request) {
        this.request = request;
    }

    /**
     * 等待RPC调用结果
     *
     * @param timeout  最长等待时间
     * @param timeUnit 时间单位
     * @return RPC响应结果
     * @throws Exception 等待过程中的异常
     */
    public Object get(long timeout, TimeUnit timeUnit) throws Exception {
        // 等待响应到达或超时
        if (latch.await(timeout, timeUnit)) {
            if (cause != null) {
                throw new RuntimeException(cause);
            }
            return response;
        } else {
            throw new RuntimeException("RPC调用超时: " + request.getRequestId());
        }
    }

    /**
     * 设置响应结果
     *
     * @param response RPC响应
     */
    public void setResponse(RpcResponse response) {
        this.response = response;
        latch.countDown();
    }

    /**
     * 设置调用失败
     *
     * @param cause 失败原因
     */
    public void setFailure(Throwable cause) {
        this.cause = cause;
        latch.countDown();
    }

    /**
     * 判断调用是否已完成
     *
     * @return 是否已完成
     */
    public boolean isDone() {
        return latch.getCount() == 0;
    }
}
