package handler;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import http.AttributeKeys;
import http.HttpResponseWriter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static http.HttpConstants.HEADER_X_FORWARDED_FOR;
import static http.HttpConstants.HEADER_X_REAL_IP;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 请求方法过滤器
 * @author zj
 * @since 2019/3/30
 */
@ChannelHandler.Sharable
public class RequestFilterChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    private ImmutableMultimap<String, HttpMethod> urlMethodMap;

    private static final Logger logger = LoggerFactory.getLogger(RequestFilterChannelHandler.class);

    public RequestFilterChannelHandler(Multimap<String, HttpMethod> urlMethodMap) {
        this.urlMethodMap = ImmutableMultimap.copyOf(urlMethodMap);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            QueryStringDecoder decoder = new QueryStringDecoder(request.getUri(), CharsetUtil.UTF_8);
            String path = decoder.path();

            Channel channel = ctx.channel();
            channel.attr(AttributeKeys.REQUEST_URI).set(decoder.uri());
            channel.attr(AttributeKeys.REQUEST_START_TIME).set(Long.toString(System.currentTimeMillis()));
            channel.attr(AttributeKeys.REQUEST_METHOD).set(request.getMethod().name());
            channel.attr(AttributeKeys.X_REAL_IP).set(HttpHeaders.getHeader(request, HEADER_X_REAL_IP));
            channel.attr(AttributeKeys.X_FORWARDED_FOR).set(HttpHeaders.getHeader(request, HEADER_X_FORWARDED_FOR));
            String clientIp = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress().trim();
            channel.attr(AttributeKeys.CLIENT_IP).set(clientIp);

            if (this.urlMethodMap.containsEntry(path, request.getMethod())) {
                if (msg instanceof ReferenceCounted) {
                    ctx.fireChannelRead(((ReferenceCounted) msg).retain());
                } else {
                    ctx.fireChannelRead(msg);
                }
            } else {
                if (this.urlMethodMap.containsKey(path)) {
                    HttpResponseWriter.writeResponse(ctx, HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, false,
                            HttpResponseWriter.EMPTY_RESPONSE);
                } else {
                    HttpResponseWriter.writeResponse(ctx, HTTP_1_1, HttpResponseStatus.NOT_FOUND, false,
                            HttpResponseWriter.EMPTY_RESPONSE);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        logger.error(e.getMessage(), e);
        ctx.close();
    }

}
