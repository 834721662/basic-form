package server;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author zj
 * @since 2019/2/28
 */
public class Response {
    public final static String CONTENT_TYPE_JSON = "application/json";

    private ChannelHandlerContext ctx;
    private Request request;

    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpResponseStatus status = HttpResponseStatus.OK;
    private String contentType = CONTENT_TYPE_JSON;
    private Charset charset = CharsetUtil.UTF_8;
    private HttpHeaders headers = new DefaultHttpHeaders();
    private ByteBuf content = Unpooled.EMPTY_BUFFER;
    //发送完成标记
    private boolean isSent;
    private boolean success;

    public Response(ChannelHandlerContext ctx, Request request) {
        this.ctx = ctx;
        this.request = request;
    }

    /**
     * 响应状态码<br>
     * 使用io.netty.handler.codec.http.HttpResponseStatus对象
     *
     * @param status 状态码
     * @return 自己
     */
    public Response setStatus(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    /**
     * 响应状态码
     *
     * @param status 状态码
     * @return 自己
     */
    public Response setStatus(int status) {
        return setStatus(HttpResponseStatus.valueOf(status));
    }

    /**
     * 设置Content-Type
     *
     * @param contentType Content-Type
     * @return 自己
     */
    public Response setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 增加响应的Header<br>
     * 重复的Header将被叠加
     *
     * @param name  名
     * @param value 值，可以是String，Date， int
     * @return 自己
     */
    public Response addHeader(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    /**
     * 设置响应的Header<br>
     * 重复的Header将被替换
     *
     * @param name  名
     * @param value 值，可以是String，Date， int
     * @return 自己
     */
    public Response setHeader(String name, Object value) {
        headers.set(name, value);
        return this;
    }

    /**
     * 设置是否长连接
     *
     * @return 自己
     */
    public Response setKeepAlive() {
        setHeader("connection", "keep-alive");
        return this;
    }

    /**
     * 设置响应HTML文本内容
     *
     * @param contentText 响应的文本
     * @return 自己
     */
    public Response setContent(String contentText) {
        this.content = Unpooled.copiedBuffer(contentText, charset);
        return this;
    }

    /**
     * 设置响应JSON文本内容
     *
     * @param contentText 响应的JSON文本
     * @return 自己
     */
    public Response setJsonContent(String contentText) {
        setContentType(CONTENT_TYPE_JSON);
        return setContent(contentText);
    }

    /**
     * 转换为Netty所用Response<br>
     * 用于返回一般类型响应（文本）
     *
     * @return FullHttpResponse
     */
    private FullHttpResponse toFullHttpResponse() {
        final ByteBuf byteBuf = (ByteBuf) content;
        final FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, status, byteBuf);

        // headers
        final HttpHeaders httpHeaders = fullHttpResponse.headers().add(headers);
        httpHeaders.set("content-type", contentType + ";charset=" + charset.name());
        httpHeaders.set("content-encoding", charset);
        httpHeaders.set("content-length", byteBuf.readableBytes());

        return fullHttpResponse;
    }

    /**
     * 发送响应到客户端<br>
     *
     * @return ChannelFuture
     */
    public ChannelFuture send() {
        ChannelFuture channelFuture = sendFull();

        this.isSent = true;
        return channelFuture;
    }

    /**
     * @return 是否已经出发发送请求，内部使用<br>
     */
    protected boolean isSent() {
        return this.isSent;
    }

    /**
     * 发送响应到客户端
     *
     * @return ChannelFuture
     */
    private ChannelFuture sendFull() {
        if (request != null && request.isKeepAlive()) {
            setKeepAlive();
            return ctx.writeAndFlush(this.toFullHttpResponse());
        } else {
            return sendAndCloseFull();
        }
    }

    /**
     * 发送给到客户端并关闭ChannelHandlerContext
     *
     * @return ChannelFuture
     */
    private ChannelFuture sendAndCloseFull() {
        return ctx.writeAndFlush(this.toFullHttpResponse()).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 发送错误消息
     *
     * @param status 错误状态码
     * @param msg    消息内容
     * @return ChannelFuture
     */
    public ChannelFuture sendError(HttpResponseStatus status, String msg) {
        if (ctx.channel().isActive()) {
            return this.setStatus(status).setContent(msg).send();
        }
        return null;
    }


    /**
     * 发送500 Internal Server Error
     *
     * @param msg 消息内容
     * @return ChannelFuture
     */
    public ChannelFuture sendServerError(String msg) {
        return sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("headers:\r\n ");
        for (Map.Entry<String, String> entry : headers.entries()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("content: ").append(content);

        return sb.toString();
    }

    protected static Response build(ChannelHandlerContext ctx, Request request) {
        return new Response(ctx, request);
    }

    protected static Response build(ChannelHandlerContext ctx) {
        return new Response(ctx, null);
    }

    public void success(Object data) {
        this.success = true;
        JSONObject response = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("code", HttpResponseStatus.OK.code());
        meta.put("msg", "");
        JSONObject datas = new JSONObject();
        if (data != null) {
            datas.put("data", data);
        }
        response.put("success", success);
        response.put("response", datas);
        response.put("meta", meta);
        setJsonContent(response.toJSONString());
    }

    public void fail(String msg) {
        this.success = false;
        JSONObject response = new JSONObject();
        JSONObject meta = new JSONObject();
        meta.put("code", HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        meta.put("msg", msg);
        response.put("meta", meta);
        response.put("success", success);
        setJsonContent(response.toJSONString());
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public ByteBuf getContent() {
        return content;
    }

    public boolean isSuccess() {
        return success;
    }
}
