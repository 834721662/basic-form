package handler;

import http.HttpResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zj
 * @since 2019/3/30
 */
@ChannelHandler.Sharable
public class NettyServerStatusHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final AtomicReference<HttpResponseStatus> httpResponseStatus = new AtomicReference<>(HttpResponseStatus
            .SERVICE_UNAVAILABLE); //服务启动默认状态是503，如果要推上线需要手动更新状态为200(正常添加200.sh脚本)

    private static final Logger logger = LoggerFactory.getLogger(NettyServerStatusHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            final String requestUri = new URI(request.getUri()).getPath();
            final boolean isKeepAlive = HttpHeaders.isKeepAlive(request);

            if (StringUtils.equalsIgnoreCase(http.HttpConstants.URI_HEALTH_STATUS_GET, requestUri)
                    || StringUtils.equalsIgnoreCase(http.HttpConstants.URI_HEALTH_STATUS_DEFAULT, requestUri)) {
                HttpResponseStatus status = this.httpResponseStatus.get();
                HttpResponseWriter.writeResponse(ctx, status, isKeepAlive, status.toString());
            } else if (StringUtils.equalsIgnoreCase(http.HttpConstants.URI_HEALTH_STATUS_SET, requestUri)) {
                if (HttpMethod.GET.equals(request.getMethod())) {
                    QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
                    Map<String, List<String>> parameters = decoder.parameters();

                    List<String> list = parameters.get(http.HttpConstants.PARAM_STATUS);
                    try {
                        if (null != list && list.size() == 1) {
                            HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(Integer.parseInt(list.get(0)));
                            this.httpResponseStatus.set(httpResponseStatus);

                            HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.OK, isKeepAlive, http.HttpConstants.OK);
                            return;
                        }
                    } catch (Exception e) {
                        logger.warn("Catch Exception while handle health status set", e);
                    }

                    HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.BAD_REQUEST, isKeepAlive,
                            HttpResponseStatus.BAD_REQUEST.toString());
                } else {
                    logger.warn("Health status set should only accept get request, but now is: {}", request.getMethod());
                    HttpResponseWriter.writeResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, isKeepAlive,
                            HttpResponseStatus.METHOD_NOT_ALLOWED.toString());
                }
            }
        }
    }

}
