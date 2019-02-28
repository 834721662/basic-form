package server;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * @author zj
 * @since 2019/2/28
 */
public class Request {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private FullHttpRequest nettyRequest;

    private String path;
    private String ip;
    private Long startTime;
    private Map<String, String> headers = Maps.newHashMap();
    private Map<String, Object> params = Maps.newHashMap();
    private ByteBuf content = Unpooled.EMPTY_BUFFER;

    private Request(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        this.nettyRequest = nettyRequest;
        final String uri = nettyRequest.getUri();
        this.path = getPath(getUri());
        this.startTime = System.currentTimeMillis();

        this.putHeaders(nettyRequest.headers());

        // request URI parameters
        this.putParams(new QueryStringDecoder(uri));
        if (nettyRequest.getMethod() != HttpMethod.GET) {
            HttpPostRequestDecoder decoder = null;
            try {
                decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, nettyRequest);
                this.putParams(decoder);
            } finally {
                if (null != decoder) {
                    decoder.destroy();
                    decoder = null;
                }
            }
        }
        // IP
        this.putIp(ctx);

        this.content = nettyRequest.content();
    }

    /**
     * @return Netty的HttpRequest
     */
    public HttpRequest getNettyRequest() {
        return this.nettyRequest;
    }

    /**
     * 获得版本信息
     *
     * @return 版本
     */
    public String getProtocolVersion() {
        return nettyRequest.getProtocolVersion().text();
    }

    /**
     * 获得URI（带参数的路径）
     *
     * @return URI
     */
    public String getUri() {
        return nettyRequest.getUri();
    }

    /**
     * @return 获得path（不带参数的路径）
     */
    public String getPath() {
        return path;
    }

    /**
     * 获得Http方法
     *
     * @return Http method
     */
    public String getMethod() {
        return nettyRequest.getMethod().name();
    }

    /**
     * 获得IP地址
     *
     * @return IP地址
     */
    public String getIp() {
        return ip;
    }

    /**
     * 获得所有头信息
     *
     * @return 头信息Map
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 使用ISO8859_1字符集获得Header内容<br>
     * 由于Header中很少有中文，故一般情况下无需转码
     *
     * @param headerKey 头信息的KEY
     * @return 值
     */
    public String getHeader(String headerKey) {
        return headers.get(headerKey);
    }

    /**
     * @param name 参数名
     * @return 获得请求参数
     */
    public String getParam(String name) {
        return MapUtils.getString(params, name);
    }

    /**
     * @param name         参数名
     * @param defaultValue 当客户端未传参的默认值
     * @return 获得请求参数
     */
    public String getParam(String name, String defaultValue) {
        return MapUtils.getString(params, name, defaultValue);
    }


    /**
     * 获得所有请求参数
     *
     * @return Map
     */
    public Map<String, Object> getParams() {
        return params;
    }

    public String getContentString() {
        content.resetReaderIndex();
        byte[] byteArray = new byte[content.capacity()];
        content.readBytes(byteArray);
        return new String(byteArray);
    }

    /**
     * @return 是否为长连接
     */
    public boolean isKeepAlive() {
        final String connectionHeader = getHeader("connection");
        // 无论任何版本Connection为close时都关闭连接
        if ("close".equalsIgnoreCase(connectionHeader)) {
            return false;
        }

        // HTTP/1.0只有Connection为Keep-Alive时才会保持连接
        if (HttpVersion.HTTP_1_0.text().equals(getProtocolVersion())) {
            if ("keep-alive".equalsIgnoreCase(connectionHeader)) {
                return false;
            }
        }
        // HTTP/1.1默认打开Keep-Alive
        return true;
    }

    /**
     * 填充参数（GET请求的参数）
     *
     * @param decoder QueryStringDecoder
     */
    protected void putParams(QueryStringDecoder decoder) {
        if (null != decoder) {
            List<String> valueList;
            for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                valueList = entry.getValue();
                if (null != valueList) {
                    this.putParam(entry.getKey(), 1 == valueList.size() ? valueList.get(0) : valueList);
                }
            }
        }
    }

    /**
     * 填充参数（POST请求的参数）
     *
     * @param decoder QueryStringDecoder
     */
    protected void putParams(HttpPostRequestDecoder decoder) {
        if (null == decoder) {
            return;
        }

        for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
            putParam(data);
        }
    }

    /**
     * 填充参数
     *
     * @param data InterfaceHttpData
     */
    protected void putParam(InterfaceHttpData data) {
        final InterfaceHttpData.HttpDataType dataType = data.getHttpDataType();
        if (dataType == InterfaceHttpData.HttpDataType.Attribute) {
            //普通参数
            Attribute attribute = (Attribute) data;
            try {
                this.putParam(attribute.getName(), attribute.getValue());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 填充参数
     *
     * @param key   参数名
     * @param value 参数值
     */
    protected void putParam(String key, Object value) {
        this.params.put(key, value);
    }

    /**
     * 填充头部信息和Cookie信息
     *
     * @param headers HttpHeaders
     */
    protected void putHeaders(HttpHeaders headers) {
        for (Map.Entry<String, String> entry : headers) {
            this.headers.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 设置客户端IP
     *
     * @param ctx ChannelHandlerContext
     */
    protected void putIp(ChannelHandlerContext ctx) {
        String ip = getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            ip = getMultistageReverseProxyIp(ip);
        } else {
            final InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = insocket.getAddress().getHostAddress();
        }
        this.ip = ip;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\r\nprotocolVersion: ").append(getProtocolVersion()).append("\r\n");
        sb.append("uri: ").append(getUri()).append("\r\n");
        sb.append("path: ").append(path).append("\r\n");
        sb.append("method: ").append(getMethod()).append("\r\n");
        sb.append("ip: ").append(ip).append("\r\n");
        sb.append("headers:\r\n ");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("params: \r\n");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        return sb.toString();
    }

    /**
     * 构建Request对象
     *
     * @param ctx          ChannelHandlerContext
     * @param nettyRequest Netty的HttpRequest
     * @return Request
     */
    protected static final Request build(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        return new Request(ctx, nettyRequest);
    }

    private static String getPath(String uriStr) {
        URI uri = null;
        try {
            uri = new URI(uriStr);
            return uri.getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getStartTime() {
        return startTime;
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!StringUtils.isBlank(subIp) && !"unknown".equalsIgnoreCase(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

}
