package com.wangguangwu.netty.websocket.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HTTP 静态文件处理器
 * 用于处理 HTTP 请求，提供静态文件服务
 *
 * @author wangguangwu
 */
public class HttpStaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 静态文件目录
     */
    private final String webRoot;

    /**
     * HTTP 日期格式化器
     */
    private static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     * 文件路径分隔符正则表达式
     */
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    /**
     * 允许的文件扩展名
     */
    private static final String[] ALLOWED_FILE_EXTENSIONS = {
            ".html", ".htm", ".css", ".js", ".txt", ".json", ".xml",
            ".png", ".jpg", ".jpeg", ".gif", ".ico"
    };

    static {
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 构造函数
     *
     * @param webRoot 静态文件根目录
     */
    public HttpStaticFileHandler(String webRoot) {
        this.webRoot = webRoot;
    }

    /**
     * 处理 HTTP 请求
     *
     * @param ctx     通道处理器上下文
     * @param request HTTP 请求
     * @throws Exception 处理过程中可能出现的异常
     */
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 检查请求是否有效
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // 只支持 GET 请求
        if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // 获取请求路径
        String uri = request.uri();
        String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 如果请求路径是目录，则默认返回 index.html
        File file = new File(path);
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                path = path + "index.html";
                file = new File(path);
            } else {
                sendRedirect(ctx, uri + "/");
                return;
            }
        }

        // 检查文件是否存在
        if (!file.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        // 检查文件是否可读
        if (!file.isFile() || !file.canRead()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 检查文件扩展名是否允许
        if (!isAllowedExtension(file.getName())) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 获取文件 MIME 类型
        String contentType = getMimeType(file.getName());

        // 打开文件
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        long fileLength = raf.length();

        // 设置响应头
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, contentType);
        setDateAndCacheHeaders(response, file);

        // 如果是 Keep-Alive，则设置相应的头信息
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // 发送响应头
        ctx.write(response);

        // 发送文件内容
        ChannelFuture sendFileFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            // 非 SSL 连接，使用零拷贝发送文件
            sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
        } else {
            // SSL 连接，不能使用零拷贝
            sendFileFuture = ctx.write(new ChunkedFile(raf, 0, fileLength, 8192), ctx.newProgressivePromise());
        }

        // 发送结束标记
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        // 如果不是 Keep-Alive，则在发送完成后关闭连接
        if (!HttpUtil.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        handleHttpRequest(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 清理 URI，防止路径遍历攻击
     *
     * @param uri 原始 URI
     * @return 清理后的文件路径，如果 URI 不合法则返回 null
     */
    private String sanitizeUri(String uri) {
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // 将 URI 转换为文件路径
        uri = uri.replace('/', File.separatorChar);

        // 简单的安全检查
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // 转换为绝对路径
        return webRoot + uri;
    }

    /**
     * 检查文件扩展名是否允许
     *
     * @param fileName 文件名
     * @return 如果扩展名允许则返回 true，否则返回 false
     */
    private boolean isAllowedExtension(String fileName) {
        fileName = fileName.toLowerCase();
        for (String extension : ALLOWED_FILE_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件 MIME 类型
     *
     * @param fileName 文件名
     * @return MIME 类型
     */
    private String getMimeType(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 设置内容类型头
     *
     * @param response    HTTP 响应
     * @param contentType 内容类型
     */
    private void setContentTypeHeader(HttpResponse response, String contentType) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    /**
     * 设置日期和缓存头
     *
     * @param response HTTP 响应
     * @param file     文件
     */
    private void setDateAndCacheHeaders(HttpResponse response, File file) {
        // 设置日期头
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, HTTP_DATE_FORMAT.format(time.getTime()));

        // 设置最后修改时间
        time.setTimeInMillis(file.lastModified());
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, HTTP_DATE_FORMAT.format(time.getTime()));

        // 设置缓存控制
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=3600");
        response.headers().set(HttpHeaderNames.EXPIRES, HTTP_DATE_FORMAT.format(new Date(System.currentTimeMillis() + 3600 * 1000)));
    }

    /**
     * 发送错误响应
     *
     * @param ctx    通道处理器上下文
     * @param status HTTP 状态码
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // 关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送重定向响应
     *
     * @param ctx    通道处理器上下文
     * @param newUri 新的 URI
     */
    private void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        // 关闭连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
