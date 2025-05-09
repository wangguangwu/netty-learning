package com.wangguangwu.netty.dubborpc.api;

/**
 * 问候服务接口
 * 定义了RPC服务的方法
 *
 * @author wangguangwu
 */
public interface GreetingService {

    /**
     * 问候方法
     *
     * @param name 接收问候的名称
     * @return 格式化的问候语
     */
    String greet(String name);
}
