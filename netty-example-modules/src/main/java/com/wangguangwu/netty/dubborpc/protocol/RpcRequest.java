package com.wangguangwu.netty.dubborpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

/**
 * RPC请求对象
 * 定义了RPC请求的格式
 *
 * @author wangguangwu
 */
@Setter
@Getter
public class RpcRequest implements Serializable {

    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，用于唯一标识一次RPC请求
     */
    private String requestId;

    /**
     * 接口名称，即服务的全限定类名
     */
    private String interfaceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型数组
     */
    private Class<?>[] parameterTypes;

    /**
     * 参数值数组
     */
    private Object[] parameters;

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
