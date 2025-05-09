package com.wangguangwu.netty.dubborpc.transport;

import com.wangguangwu.netty.dubborpc.protocol.RpcResponse;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * RPC请求管理器
 * 管理所有进行中的RPC请求，实现请求和响应的匹配
 *
 * @author wangguangwu
 */
public class RpcRequestManager {

    /**
     * 单例实例
     * -- GETTER --
     * 获取单例实例
     *
     * @return RpcRequestManager实例
     */
    @Getter
    private static final RpcRequestManager INSTANCE = new RpcRequestManager();

    /**
     * 请求ID到Future的映射，用于存储和管理进行中的请求
     */
    private final Map<String, RpcFuture> requestMap = new ConcurrentHashMap<>();

    /**
     * 默认RPC调用超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 30;

    /**
     * 私有构造函数，防止外部实例化
     */
    private RpcRequestManager() {
    }

    /**
     * 注册请求
     *
     * @param requestId 请求ID
     * @param future    RPC Future
     */
    public void registerRequest(String requestId, RpcFuture future) {
        requestMap.put(requestId, future);
    }

    /**
     * 获取请求的Future
     *
     * @param requestId 请求ID
     * @return RPC Future，如果不存在则返回null
     */
    public RpcFuture getRequest(String requestId) {
        return requestMap.get(requestId);
    }

    /**
     * 移除请求
     *
     * @param requestId 请求ID
     * @return 被移除的RPC Future，如果不存在则返回null
     */
    public RpcFuture removeRequest(String requestId) {
        return requestMap.remove(requestId);
    }

    /**
     * 通知响应已收到
     *
     * @param requestId 请求ID
     * @param response  RPC响应
     */
    public void notifyResponse(String requestId, RpcResponse response) {
        RpcFuture future = removeRequest(requestId);
        if (future != null) {
            future.setResponse(response);
        } else {
            System.err.println("未找到对应的请求: " + requestId);
        }
    }

    /**
     * 等待所有请求完成
     *
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return 是否所有请求都已完成
     */
    public boolean waitForAllRequests(long timeout, TimeUnit timeUnit) {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        while (!requestMap.isEmpty() && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return requestMap.isEmpty();
    }
}
