package handler;

import http.HttpResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.List;

/**
 * http请求返回处理
 * @author zj
 * @since 2019/3/29
 */
@ChannelHandler.Sharable
public abstract class HttpRequestHandler {

    protected String uri;
    protected List<HttpMethod> httpMethods;

    private boolean async = true;

    public HttpRequestHandler(String uri, List<HttpMethod> httpMethods) {
        this.uri = uri;
        this.httpMethods = httpMethods;
    }

    public HttpRequestHandler(String uri, List<HttpMethod> httpMethods, boolean async) {
        this.uri = uri;
        this.httpMethods = httpMethods;
        this.async = async;
    }

    /**
     * http message
     * @param ctx
     * @param request
     * @return
     */
    protected abstract DefaultFullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request);

    protected void invokeHandle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        DefaultFullHttpResponse response = handle(ctx, request);
        boolean isKeepAlive = HttpHeaders.isKeepAlive(request);
        // http 1.0版本 默认false
        if (HttpVersion.HTTP_1_0.equals(response.getProtocolVersion())) {
            isKeepAlive = false;
        }
        HttpResponseWriter.writeResponse(ctx, response.getStatus(), response.getProtocolVersion(),  isKeepAlive, response.content());
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public List<HttpMethod> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<HttpMethod> httpMethods) {
        this.httpMethods = httpMethods;
    }
}
