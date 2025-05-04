package com.wangguangwu.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 演示 Netty 的 ByteBuf 基本用法。
 * <p>
 * 区别说明：
 * 1. Netty 的 ByteBuf 与 Java NIO 的 ByteBuffer 区别：
 * - ByteBuffer 需要手动 flip() 进行读写切换，且容量固定。
 * - ByteBuf 不需要 flip，内部通过 readerIndex 和 writerIndex 自动管理读写区域，支持动态扩容。
 * - ByteBuf 支持方法链、池化、零拷贝等高级特性。
 * 2. ByteBuf 内部结构：
 * - [0, readerIndex): 已读取区域
 * - [readerIndex, writerIndex): 可读区域
 * - [writerIndex, capacity): 可写区域
 *
 * @author wangguangwu
 */
public class NettyByteBuf01 {

    public static void main(String[] args) {
        // 创建一个容量为10的 ByteBuf
        ByteBuf buffer = Unpooled.buffer(10);

        // 写入数据到 ByteBuf
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.writeByte(i);
        }

        // 输出容量
        System.out.println("capacity=" + buffer.capacity());

        // 读取所有数据（读指针会自动递增）
        while (buffer.isReadable()) {
            System.out.println(buffer.readByte());
        }
        System.out.println("执行完毕");
    }
}
