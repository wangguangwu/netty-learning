package com.wangguangwu.netty.dubborpc.provider;

import com.wangguangwu.netty.dubborpc.api.GreetingService;

/**
 * 问候服务实现类
 * 这是实际提供服务的类，将通过RPC暴露给客户端
 *
 * @author wangguangwu
 */
public class GreetingServiceImpl implements GreetingService {

    /**
     * 请求计数器，用于跟踪处理的请求数量
     */
    private static int counter = 0;

    @Override
    public String greet(String name) {
        System.out.println("服务端收到问候请求，名称: " + name);
        
        // 增加计数器
        counter++;
        
        // 模拟服务处理延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 返回格式化的问候语
        return "Greetings, " + name + "! (请求次数: " + counter + ")";
    }
}
