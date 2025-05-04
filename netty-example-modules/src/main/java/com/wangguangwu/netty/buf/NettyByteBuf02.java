package com.wangguangwu.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 演示 Netty ByteBuf 字符串操作。
 * <p>
 * 区别说明：
 * Netty 的 ByteBuf 不需要像 NIO 的 ByteBuffer 一样 flip() 切换读写模式，
 * 内部自动管理读写指针，支持动态扩容、池化、零拷贝等高级特性。
 *
 * @author wangguangwu
 */
public class NettyByteBuf02 {

    public static void main(String[] args) {
        // 创建 ByteBuf，内容为 "hello,world!"
        ByteBuf byteBuf = Unpooled.copiedBuffer("hello,world!", StandardCharsets.UTF_8);

        // 判断是否有可访问的底层数组
        if (byteBuf.hasArray()) {
            System.out.println("byteBuf=" + byteBuf);
            // 0
            System.out.println("arrayOffset=" + byteBuf.arrayOffset());
            // 0
            System.out.println("readerIndex=" + byteBuf.readerIndex());
            // 12
            System.out.println("writerIndex=" + byteBuf.writerIndex());
            // 64
            System.out.println("capacity=" + byteBuf.capacity());
            // 104
            System.out.println("第一个字节(ASCII)=" + byteBuf.getByte(0));
            // 可读字节数 12
            int len = byteBuf.readableBytes();
            System.out.println("len=" + len);
            // 逐字节输出
            for (int i = 0; i < len; i++) {
                System.out.println((char) byteBuf.getByte(i));
            }
            // 按范围读取
            System.out.println(byteBuf.getCharSequence(0, 5, StandardCharsets.UTF_8));
            System.out.println(byteBuf.getCharSequence(6, 5, StandardCharsets.UTF_8));
        }
    }
}
