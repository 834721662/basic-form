package handler;

import com.google.common.collect.ImmutableMap;
import http.AttributeKeys;
import http.HttpResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zj
 * @since 2019/3/30
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final ThreadPoolExecutor executor;
    private final ImmutableMap<String, HttpRequestHandler> httpRequestHandlerMap;

    public NettyServerHandler(ThreadPoolExecutor executor, Map<String, HttpRequestHandler> httpRequestHandlerMap) {
        this.executor = executor;
        this.httpRequestHandlerMap = ImmutableMap.copyOf(httpRequestHandlerMap);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        String requestURI = ctx.channel().attr(AttributeKeys.REQUEST_URI).get();
        String path = new QueryStringDecoder(requestURI, CharsetUtil.UTF_8).path();
        final HttpRequestHandler requestHandler = httpRequestHandlerMap.get(path);
        if (requestHandler == null) {
            ctx.fireChannelRead(request.retain());
            return;
        }

        // 异步处理请求
        if (requestHandler.isAsync() && executor != null) {
            try {
                executor.execute(new HttpRequestHandlerThread(ctx, request.retain(), requestHandler));
            } catch (RejectedExecutionException rejectException) {
                HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        HttpHeaders.isKeepAlive(request), "service reject");
                logger.warn("process thread pool is full, reject, active={} poolSize={} corePoolSize={} maxPoolSize={}"
                                + " taskCount={}", executor.getActiveCount(), executor.getPoolSize(),
                        executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getTaskCount());
            }
        } else { // 同步请求处理
            try {
                requestHandler.invokeHandle(ctx, request);
            } catch (Throwable t) {
                logger.error("handle request error", t);
                HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        HttpHeaders.isKeepAlive(request), "Internal Server Error");
            }
        }
    }
}
