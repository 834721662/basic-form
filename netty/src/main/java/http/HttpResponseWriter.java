package http;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http返回包回写工具
 *
 * @author zj
 * @since 2019/3/31
 */
public class HttpResponseWriter {


    private static final String LOCALHOST_ACCESS = "localhost_access";

    public static final byte[] EMPTY_RESPONSE = "".getBytes(Charsets.UTF_8);

    private static final Logger localHostAccessLogger = LoggerFactory.getLogger(LOCALHOST_ACCESS);
    private static final Logger logger = LoggerFactory.getLogger(HttpResponseWriter.class);

    public static void writeResponse(ChannelHandlerContext ctx, HttpVersion httpVersion, HttpResponseStatus
            responseStatus, boolean isKeepAlive, byte[] response) {
        Channel channel = ctx.channel();
        try {
            if (channel.isWritable()) {
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(httpVersion, responseStatus, Unpooled
                        .wrappedBuffer(response));
                httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpConstants.TEXT_PLAIN);
                httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
                ChannelFuture channelFuture = channel.writeAndFlush(httpResponse);

                //no keepalive.
                if (!isKeepAlive) {
                    // Close the connection when the whole content is written out.
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                logger.warn("Channel is not writable...");
            }
        } finally {
            Attribute<String> clientIp = channel.attr(AttributeKeys.CLIENT_IP);
            Attribute<String> requestUri = channel.attr(AttributeKeys.REQUEST_URI);
            Attribute<String> startTime = channel.attr(AttributeKeys.REQUEST_START_TIME);
            Attribute<String> requestMethod = channel.attr(AttributeKeys.REQUEST_METHOD);
            Attribute<String> xRealIp = channel.attr(AttributeKeys.X_REAL_IP);
            Attribute<String> xForwardedFor = channel.attr(AttributeKeys.X_FORWARDED_FOR);

            localHostAccessLogger.info("{} - - [{}] \"{} {} {}\" {} {} {} {} {}", clientIp.get(), formatAccessLogTime
                            (Long.parseLong(startTime.get())), requestMethod.get(), requestUri.get(), httpVersion.text(),
                    responseStatus.code(), response.length, System.currentTimeMillis() - Long.parseLong(startTime.get()),
                    formatAccessLogField(xRealIp.get()), formatAccessLogField(xForwardedFor.get()));
        }
    }

    private static String formatAccessLogField(String v) {
        return StringUtils.isNotBlank(v) ? v.trim() : "-";
    }

    private static String formatAccessLogTime(long timeMillis) {
        return DateFormatUtils.format(timeMillis, HttpConstants.ACCESS_LOG_TIME_FORMAT);
    }

    public static void writeResponse(ChannelHandlerContext ctx, boolean isKeepAlive, byte[] response) {
        writeResponse(ctx, HTTP_1_1, HttpResponseStatus.OK, isKeepAlive, response);
    }

    /**
     * 默认使用http 1.1的版本
     *
     * @param ctx
     * @param responseStatus
     * @param isKeepAlive
     * @param responseStr
     */
    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus responseStatus, boolean
            isKeepAlive, String responseStr) {
        writeResponse(ctx, HTTP_1_1, responseStatus, isKeepAlive, responseStr.getBytes(Charsets.UTF_8));
    }

    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus responseStatus, boolean isKeepAlive, ByteBuf byteBuf) {
        byte[] bytes;
        if (byteBuf.hasArray()) {
            bytes = byteBuf.array();
        } else {
            bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        }
        writeResponse(ctx, HTTP_1_1, responseStatus, isKeepAlive, bytes);
    }

    /**
     * 允许定义http版本
     *
     * @param ctx
     * @param responseStatus
     * @param httpVersion
     * @param isKeepAlive
     * @param byteBuf
     */
    public static void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus responseStatus, HttpVersion httpVersion,
                                     boolean isKeepAlive, ByteBuf byteBuf) {
        byte[] bytes;
        if (byteBuf.hasArray()) {
            bytes = byteBuf.array();
        } else {
            bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        }
        writeResponse(ctx, httpVersion, responseStatus, isKeepAlive, bytes);
    }

}
