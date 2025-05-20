package com.wangguangwu.netty.pipeline.core;

import com.wangguangwu.netty.pipeline.api.Pipeline;

/**
 * Pipeline工厂类
 * 用于创建Pipeline实例
 *
 * @author wangguangwu
 */
public final class PipelineFactory {
    
    private PipelineFactory() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 创建默认的Pipeline实例
     *
     * @return Pipeline实例
     */
    public static Pipeline createPipeline() {
        return new DefaultPipeline();
    }
}
