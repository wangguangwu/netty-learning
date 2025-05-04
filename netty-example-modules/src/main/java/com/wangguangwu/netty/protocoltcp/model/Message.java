package com.wangguangwu.netty.protocoltcp.model;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

/**
 * 自定义消息协议
 * 用于解决TCP粘包/拆包问题的消息格式
 *
 * @author wangguangwu
 */
@Getter
public class Message {

    /**
     * 消息长度
     * 用于标识消息体的字节数，解决粘包拆包问题的关键
     */
    @Setter
    private int length;

    /**
     * 消息内容
     * 实际传输的数据
     */
    private byte[] content;

    public Message() {
    }

    public Message(String content) {
        setContent(content.getBytes(StandardCharsets.UTF_8));
    }

    public void setContent(byte[] content) {
        this.content = content;
        this.length = content.length;
    }

    /**
     * 获取消息内容的字符串表示
     *
     * @return 消息内容的字符串
     */
    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "Message{" +
                "length=" + length +
                ", content='" + getContentAsString() + '\'' +
                '}';
    }
}
