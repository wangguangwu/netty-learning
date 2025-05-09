package com.wangguangwu.netty.dubborpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * RPC响应对象
 * 定义了RPC响应的格式
 *
 * @author wangguangwu
 */
@Setter
@Getter
public class RpcResponse implements Serializable {

    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求ID，与请求对象中的ID对应
     */
    private String requestId;

    /**
     * 错误信息，如果调用成功则为null
     */
    private String errorMessage;

    /**
     * 调用结果
     */
    private Object result;

    /**
     * 判断调用是否成功
     *
     * @return 如果没有错误信息则表示调用成功
     */
    public boolean isSuccess() {
        return errorMessage == null;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", result=" + result +
                '}';
    }
}
