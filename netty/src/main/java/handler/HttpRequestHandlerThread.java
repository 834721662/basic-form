package handler;

import http.HttpResponseWriter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zj
 * @since 2019/3/31
 */
public class HttpRequestHandlerThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestHandlerThread.class);

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;
    private final HttpRequestHandler requestHandler;

    public HttpRequestHandlerThread(ChannelHandlerContext ctx, FullHttpRequest request, HttpRequestHandler requestHandler) {
        this.ctx = ctx;
        this.request = request;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try {
            requestHandler.invokeHandle(ctx, request);
        } catch (Throwable t) {
            logger.error("handle request error", t);
            HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpHeaders.isKeepAlive(request),
                    "Internal Server Error");
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

}
