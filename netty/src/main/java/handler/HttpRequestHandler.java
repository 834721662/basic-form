package handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

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
