package com.wangguangwu.netty.groupchat.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty 群聊服务器处理器，支持用户名注册与展示。
 * 提供心跳检测、用户管理和消息转发功能。
 *
 * @author wangguangwu
 */
public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 管理所有 channel 的组，线程安全
     */
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 维护 channel 到用户名的映射
     */
    private static final Map<Channel, String> USER_MAP = new ConcurrentHashMap<>();

    /**
     * 维护用户心跳次数
     */
    private static final Map<Channel, AtomicInteger> IDLE_COUNT = new ConcurrentHashMap<>();

    /**
     * 最大允许的心跳丢失次数，超过将断开连接
     */
    private static final int MAX_IDLE_COUNT = 3;

    /**
     * 心跳消息标识
     */
    private static final String HEARTBEAT_MSG = "PING";

    /**
     * 日期格式化
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 新客户端加入时触发。
     * 将 channel 添加到组，但不立即广播，等待用户名注册。
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        CHANNEL_GROUP.add(channel);
        // 初始化心跳计数器
        IDLE_COUNT.put(channel, new AtomicInteger(0));
        System.out.println("[INFO] 新连接: " + channel.remoteAddress() + "，等待用户名注册，当前在线: " + CHANNEL_GROUP.size());
    }

    /**
     * 客户端断开连接时触发。
     * 清理用户信息并广播离开消息。
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String username = getUserName(channel);

        // 广播用户离开消息
        String leaveMsg = String.format("[系统] 用户 %s 离开了聊天室\n", username);
        broadcastMessage(channel, leaveMsg);

        // 清理用户资源
        USER_MAP.remove(channel);
        IDLE_COUNT.remove(channel);

        System.out.println("[INFO] 用户 " + username + " 离开，当前在线: " + CHANNEL_GROUP.size());
    }

    /**
     * 客户端物理连接断开时触发。
     * 记录用户离线日志。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String username = getUserName(channel);
        System.out.println("[INFO] 用户 " + username + " 离线");
    }

    /**
     * 客户端物理连接建立时触发。
     * 记录连接建立日志。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.out.println("[INFO] 新连接建立: " + channel.remoteAddress());
    }

    /**
     * 读取并处理客户端消息。
     * 第一条消息视为用户名注册，后续消息转发给其他用户。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();

        // 重置心跳计数
        resetIdleCount(channel);

        // 处理第一条消息作为用户名注册
        if (!USER_MAP.containsKey(channel)) {
            handleUsernameRegistration(channel, msg.trim());
            return;
        }

        // 检查是否是心跳消息
        if (HEARTBEAT_MSG.equals(msg.trim())) {
            handleHeartbeatMessage(channel);
            return;
        }

        // 处理普通聊天消息
        handleChatMessage(channel, msg);
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(Channel channel) {
        String username = getUserName(channel);
        System.out.println("[HEARTBEAT] 收到来自 " + username + " 的心跳消息");
        // 心跳消息不需要转发，只需重置空闲计数
        IDLE_COUNT.remove(channel);
    }

    /**
     * 处理用户名注册
     */
    private void handleUsernameRegistration(Channel channel, String username) {
        // 用户名验证（可扩展更复杂的验证规则）
        if (username.isEmpty()) {
            channel.writeAndFlush("[系统] 用户名不能为空，请重新连接并设置有效用户名");
            channel.close();
            return;
        }

        // 检查用户名是否已被使用
        if (USER_MAP.containsValue(username)) {
            String newUsername = username + "_" + System.currentTimeMillis() % 1000;
            channel.writeAndFlush("[系统] 用户名 " + username + " 已被使用，已自动分配新用户名: " + newUsername);
            username = newUsername;
        }

        // 注册用户名
        USER_MAP.put(channel, username);

        // 发送欢迎消息给新用户
        String welcomeMsg = String.format("[系统] 欢迎 %s 加入聊天室！当前在线人数: %d\n",
                username, CHANNEL_GROUP.size());
        channel.writeAndFlush(welcomeMsg);

        // 广播新用户加入消息
        String joinMsg = String.format("[系统] 用户 %s 加入了聊天室 (%s)\n",
                username, sdf.format(new Date()));
        broadcastMessage(channel, joinMsg);

        System.out.println("[INFO] 用户名注册成功: " + username + "，当前在线: " + CHANNEL_GROUP.size());
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Channel channel, String message) {
        String username = USER_MAP.get(channel);

        // 消息为空则忽略
        if (message.trim().isEmpty()) {
            return;
        }

        // 检查是否是命令消息
        if (message.startsWith("/")) {
            handleCommandMessage(channel, message);
            return;
        }

        // 记录接收到的消息
        System.out.println("[RECV] 来自 " + username + ": " + message);

        // 转发消息给其他用户
        CHANNEL_GROUP.forEach(ch -> {
            if (channel != ch) {
                ch.writeAndFlush(String.format("[%s] %s\n", username, message));
            } else {
                ch.writeAndFlush(String.format("[自己] %s\n", message));
            }
        });
    }

    /**
     * 处理命令消息
     */
    private void handleCommandMessage(Channel channel, String commandMsg) {
        String username = USER_MAP.get(channel);
        String[] parts = commandMsg.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/help":
                // 帮助命令
                StringBuilder help = new StringBuilder("[系统] 可用命令:\n");
                help.append("/help - 显示帮助信息\n");
                help.append("/list - 显示在线用户列表\n");
                help.append("/nick <新用户名> - 修改用户名\n");
                channel.writeAndFlush(help.toString());
                break;

            case "/list":
                // 显示在线用户列表
                StringBuilder userList = new StringBuilder("[系统] 当前在线用户 (" + CHANNEL_GROUP.size() + "):\n");
                USER_MAP.values().forEach(name -> userList.append("- ").append(name).append("\n"));
                channel.writeAndFlush(userList.toString());
                break;

            case "/nick":
                // 修改用户名
                if (parts.length < 2 || parts[1].trim().isEmpty()) {
                    channel.writeAndFlush("[系统] 用法: /nick <新用户名>");
                    return;
                }

                String newUsername = parts[1].trim();
                String oldUsername = username;

                // 检查新用户名是否已被使用
                if (USER_MAP.containsValue(newUsername)) {
                    channel.writeAndFlush("[系统] 用户名 " + newUsername + " 已被使用");
                    return;
                }

                // 更新用户名
                USER_MAP.put(channel, newUsername);
                channel.writeAndFlush("[系统] 您的用户名已更改为: " + newUsername);

                // 广播用户名变更消息
                String changeMsg = String.format("[系统] 用户 %s 已更名为 %s\n", oldUsername, newUsername);
                broadcastMessage(channel, changeMsg);

                System.out.println("[INFO] 用户 " + oldUsername + " 更名为 " + newUsername);
                break;

            default:
                channel.writeAndFlush("[系统] 未知命令: " + command + "，输入 /help 获取帮助");
        }
    }

    /**
     * 广播消息给除了指定 channel 外的所有用户
     */
    private void broadcastMessage(Channel excludeChannel, String message) {
        CHANNEL_GROUP.forEach(ch -> {
            if (ch != excludeChannel) {
                ch.writeAndFlush(message);
            }
        });
    }

    /**
     * 获取用户名，如果未注册则返回地址
     */
    private String getUserName(Channel channel) {
        return USER_MAP.getOrDefault(channel, channel.remoteAddress().toString());
    }

    /**
     * 重置心跳计数
     */
    private void resetIdleCount(Channel channel) {
        AtomicInteger count = IDLE_COUNT.get(channel);
        if (count != null) {
            count.set(0);
        }
    }

    /**
     * 处理心跳事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            Channel channel = ctx.channel();
            String username = getUserName(channel);

            // 处理读写空闲事件
            if (event.state() == IdleState.ALL_IDLE) {
                AtomicInteger idleCount = IDLE_COUNT.get(channel);
                int count = idleCount.incrementAndGet();

                System.out.println("[IDLE] 用户 " + username + " 心跳超时 (" + count + "/" + MAX_IDLE_COUNT + ")");

                // 超过最大允许心跳丢失次数，断开连接
                if (count >= MAX_IDLE_COUNT) {
                    System.out.println("[IDLE] 用户 " + username + " 心跳超时次数过多，断开连接");
                    ctx.close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理。
     * 记录异常并关闭连接。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        String username = getUserName(channel);
        System.out.println("[ERROR] 用户 " + username + " 发生异常: " + cause.getMessage());
        ctx.close();
    }
}
