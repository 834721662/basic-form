package http;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zj.basicform.common.server.Server;
import handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.commons.lang.StringUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zj
 * @since 2019/3/29
 */
public class HttpNettyNioServer implements Server {


    private AtomicBoolean isRunning = new AtomicBoolean();
    private GlobalStatsMonitorHandler monitorHandler = new LoggingMonitorHandler();
    private NettyServerStatusHandler serverStatusHandler;
    private ChannelHandler requestURIFilterHandler;
    private NettyNioServer httpServer;

    private String serverName;
    private int port;
    private int soRcvbuf;
    private int soBacklog;
    private boolean tcpNoDelay;
    private boolean soKeepAlive;
    private int bossThreads;
    private int workerThreads;
    private int maxContentLength;
    private List<HttpRequestHandler> httpRequestHandlers;
    private ThreadPoolExecutor poolExecutor;

    public HttpNettyNioServer(HttpNettyNioServerBuilder builder) {
        this.port = builder.port;
        this.soRcvbuf = builder.soRcvbuf;
        this.soBacklog = builder.soBacklog;
        this.serverName = builder.serverName;
        this.tcpNoDelay = builder.tcpNoDelay;
        this.soKeepAlive = builder.soKeepAlive;
        this.bossThreads = builder.bossThreads;
        this.workerThreads = builder.workTheads;
        this.httpRequestHandlers = builder.requestHandlers;
        this.poolExecutor = builder.poolExecutor;
    }

    @Override
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            Map<String, HttpRequestHandler> requestHandlerMap = Maps.newHashMap();
            HashMultimap<String, HttpMethod> urlMethodMap = HashMultimap.create();
            urlMethodMap.putAll(HttpConstants.URI_VERSION, Lists.newArrayList(HttpMethod.POST, HttpMethod.GET));
            urlMethodMap.putAll(HttpConstants.URI_HEALTH_STATUS_GET, Lists.newArrayList(HttpMethod.POST, HttpMethod.GET, HttpMethod.HEAD));
            urlMethodMap.putAll(HttpConstants.URI_HEALTH_STATUS_SET, Lists.newArrayList(HttpMethod.POST, HttpMethod.GET, HttpMethod.HEAD));
            urlMethodMap.putAll(HttpConstants.URI_HEALTH_STATUS_DEFAULT, Lists.newArrayList(HttpMethod.HEAD, HttpMethod.GET, HttpMethod.POST));
            for (HttpRequestHandler httpRequestHandler : httpRequestHandlers) {
                String uri = httpRequestHandler.getUri();
                requestHandlerMap.put(uri, httpRequestHandler);
                urlMethodMap.putAll(uri, httpRequestHandler.getHttpMethods());
            }
            final NettyServerHandler nettyServerHandler = new NettyServerHandler(poolExecutor, requestHandlerMap);
            requestURIFilterHandler = new RequestFilterChannelHandler(urlMethodMap);
            serverStatusHandler = new NettyServerStatusHandler();
            // http server
            httpServer = new NettyNioServer(port, bossThreads, workerThreads) {
                @Override
                public void addChannelHandlers(SocketChannel ch) {
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast(new HttpRequestDecoder());
                    ch.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
                    ch.pipeline().addLast(requestURIFilterHandler);
                    ch.pipeline().addLast(nettyServerHandler);
                    ch.pipeline().addLast(serverStatusHandler);
                }

                @Override
                public CloseableChannelDuplexHandler monitorHandler() {
                    return monitorHandler;
                }

                @Override
                public void option(ServerBootstrap bootstrap) {
                    super.option(bootstrap);
                    bootstrap.option(ChannelOption.SO_RCVBUF, soRcvbuf)
                            .option(ChannelOption.SO_BACKLOG, soBacklog)
                            .option(ChannelOption.TCP_NODELAY, tcpNoDelay)
                            .option(ChannelOption.SO_KEEPALIVE, soKeepAlive);
                }

                @Override
                public void close() {
                }
            };
            httpServer.start();
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {

        }
    }


    public static class HttpNettyNioServerBuilder {

        /**
         * 业务服务名称
         */
        private String serverName;
        /**
         * 业务服务端口，目前仅支持单端口模式
         */
        private int port;
        /**
         * netty服务 boss线程 selector
         */
        private int bossThreads;
        /**
         * netty服务 worker线程
         */
        private int workTheads;
        /**
         * 请求长度
         */
        private int maxContentLength = NettyNioServer.DEFAULT_MAX_CONTENT_LENGTH;
        /**
         * tcp缓冲区数据接收的大小
         */
        private int soRcvbuf = NettyNioServer.DEFAULT_SO_RCVBUF;
        /**
         * 服务端接受连接的队列长度，队列满是时，新的连接被拒绝
         */
        private int soBacklog = NettyNioServer.DEFAULT_SO_BACKLOG;
        /**
         * 注意：这个参数不代表长连接
         * Socket参数，连接保活，默认值为False
         */
        private boolean soKeepAlive = NettyNioServer.DEFAULT_SO_KEEP_ALIVE;
        /**
         * TCP参数，立即发送数据，默认值为Ture
         */
        private boolean tcpNoDelay = NettyNioServer.DEFAULT_TCP_NO_DELAY;

        private List<HttpRequestHandler> requestHandlers = new ArrayList<HttpRequestHandler>();
        /**
         * 业务线程池
         */
        private ThreadPoolExecutor poolExecutor;

        public HttpNettyNioServerBuilder setServerName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public HttpNettyNioServerBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public HttpNettyNioServerBuilder setBossThreads(int bossThreads) {
            this.bossThreads = bossThreads;
            return this;
        }

        public HttpNettyNioServerBuilder setWorkThreads(int workTheads) {
            this.workTheads = workTheads;
            return this;
        }

        public HttpNettyNioServerBuilder setMaxContentLength(int maxContentLength) {
            this.maxContentLength = maxContentLength;
            return this;
        }

        public HttpNettyNioServerBuilder setSoRcbuf(int soBacklog) {
            this.soBacklog = soBacklog;
            return this;
        }

        public HttpNettyNioServerBuilder setSoBack(int soBacklog) {
            this.soBacklog = soBacklog;
            return this;
        }

        public HttpNettyNioServerBuilder setSoKeepAlive(boolean soKeepAlive) {
            this.soKeepAlive = soKeepAlive;
            return this;
        }

        public HttpNettyNioServerBuilder setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
            return this;
        }

        public HttpNettyNioServerBuilder addHttpRequestHandler(HttpRequestHandler httpRequestHandler) {
            this.requestHandlers.add(httpRequestHandler);
            return this;
        }

        public HttpNettyNioServerBuilder addHttpRequestHandlerList(List<HttpRequestHandler> httpRequestHandlers) {
            this.requestHandlers.addAll(httpRequestHandlers);
            return this;
        }

        public HttpNettyNioServerBuilder setAsyncHandlerThreadPool(ThreadPoolExecutor threadPool) {
            poolExecutor = threadPool;
            return this;
        }

        public HttpNettyNioServer build() {
            if (port == 0) {
                throw new InvalidParameterException("[parameter] 'port' must set.");
            }
            if (requestHandlers.isEmpty()) {
                throw new InvalidParameterException("[parameter] http url request handlers must set.");
            }
            if (StringUtils.isBlank(serverName)) {
                throw new InvalidParameterException("[parameter] http server name must set.");
            }
            boolean hasAsyncHandler = false;
            for (HttpRequestHandler handler : requestHandlers) {
                hasAsyncHandler = handler.isAsync();
                if (hasAsyncHandler) {
                    break;
                }
            }
            if (hasAsyncHandler && poolExecutor == null) {
                throw new InvalidParameterException("[parameter] async handler thread pool must set.");
            }
            return new HttpNettyNioServer(this);
        }

    }


}
