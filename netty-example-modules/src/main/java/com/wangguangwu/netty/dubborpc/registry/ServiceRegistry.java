package com.wangguangwu.netty.dubborpc.registry;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册表
 * 用于管理服务的注册和发现
 *
 * @author wangguangwu
 */
public class ServiceRegistry {

    /**
     * 单例实例
     * -- GETTER --
     *  获取单例实例
     *
     * @return 服务注册表实例

     */
    @Getter
    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    /**
     * 服务映射表，接口名称 -> 服务实例
     */
    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 私有构造函数，防止外部实例化
     */
    private ServiceRegistry() {
    }

    /**
     * 注册服务
     *
     * @param interfaceClass 服务接口类
     * @param serviceInstance 服务实例
     */
    public void registerService(Class<?> interfaceClass, Object serviceInstance) {
        if (interfaceClass == null || serviceInstance == null) {
            throw new IllegalArgumentException("接口类和服务实例不能为空");
        }
        
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("注册的类必须是接口: " + interfaceClass.getName());
        }
        
        if (!interfaceClass.isInstance(serviceInstance)) {
            throw new IllegalArgumentException("服务实例必须实现指定的接口");
        }
        
        String serviceName = interfaceClass.getName();
        serviceMap.put(serviceName, serviceInstance);
        System.out.println("注册服务: " + serviceName);
    }

    /**
     * 获取服务实例
     *
     * @param interfaceName 接口名称
     * @return 服务实例，如果未找到则返回null
     */
    public Object getService(String interfaceName) {
        return serviceMap.get(interfaceName);
    }

    /**
     * 移除服务
     *
     * @param interfaceName 接口名称
     * @return 被移除的服务实例，如果未找到则返回null
     */
    public Object removeService(String interfaceName) {
        Object removed = serviceMap.remove(interfaceName);
        if (removed != null) {
            System.out.println("移除服务: " + interfaceName);
        }
        return removed;
    }
}
