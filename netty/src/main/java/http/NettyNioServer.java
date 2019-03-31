package http;

import com.zj.basicform.common.server.Server;
import handler.CloseableChannelDuplexHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * netty 服务的核心实现类，基于主从多线程模型
 *
 * @author zj
 * @since 2019/3/29
 */
public abstract class NettyNioServer implements Server {

    public Logger logger = LoggerFactory.getLogger(NettyNioServer.class);

    // 默认大小为2m
    public static final int DEFAULT_MAX_CONTENT_LENGTH = 2 * 1024 * 1024;
    public static final int DEFAULT_SO_RCVBUF = 128 * 1024;
    public static final int DEFAULT_SO_BACKLOG = 10000;
    public static final boolean DEFAULT_TCP_NO_DELAY = true;
    public static final boolean DEFAULT_SO_KEEP_ALIVE = false;

    public AtomicBoolean isRunning = new AtomicBoolean();
    public static ChannelHandler logginHandler = new LoggingHandler(LogLevel.INFO);

    private int port;
    private int bossThreads;
    private int workerThreads;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int soRcvbuf = DEFAULT_SO_RCVBUF;
    private int soBacklog = DEFAULT_SO_BACKLOG;
    private boolean tcpNoDelay = DEFAULT_TCP_NO_DELAY;
    private boolean soKeepAlive = DEFAULT_SO_KEEP_ALIVE;
    private CloseableChannelDuplexHandler monitorHandler;

    public NettyNioServer(int port) {
        this.port = port;
    }

    public NettyNioServer(int port, int bossThreads, int workerThreads) {
        this.port = port;
        this.bossThreads = bossThreads;
        this.workerThreads = workerThreads;
    }

    public NettyNioServer(int port, int bossThreads, int workerThreads, int soRcvbuf, int soBacklog, boolean tcpNoDelay, boolean soKeepAlive) {
        this.port = port;
        this.soRcvbuf = soRcvbuf;
        this.soBacklog = soBacklog;
        this.tcpNoDelay = tcpNoDelay;
        this.soKeepAlive = soKeepAlive;
        this.bossThreads = bossThreads;
        this.workerThreads = workerThreads;
    }

    @Override
    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();

            bossGroup = bossThreads > 0 ? new NioEventLoopGroup(bossThreads) : new NioEventLoopGroup();
            workerGroup = workerThreads > 0 ? new NioEventLoopGroup(workerThreads) : new NioEventLoopGroup();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, soBacklog)
                    .option(ChannelOption.SO_KEEPALIVE, soKeepAlive)
                    .option(ChannelOption.SO_RCVBUF, soRcvbuf)
                    .option(ChannelOption.TCP_NODELAY, tcpNoDelay);
            option(serverBootstrap);
            monitorHandler = monitorHandler();
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                /**
                 * 在pipeline当中添加对应处理
                 * @param channel
                 * @throws Exception
                 */
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(logginHandler);
                    if (monitorHandler != null) {
                        channel.pipeline().addLast(monitorHandler);
                    }
                    addChannelHandlers(channel);
                }
            });
            try {
                Channel ch = serverBootstrap.bind(port).sync().channel();
                logger.info("netty server start success, port={}", port);
                ch.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException("netty server start failed.", e);
            }
        } else {
            throw new IllegalStateException("netty server had already started.");
        }
    }

    @Override
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (monitorHandler != null) {
                monitorHandler.close();
            }
            close();
        } else {
            throw new IllegalStateException("netty server had already stopped.");
        }
    }

    public abstract CloseableChannelDuplexHandler monitorHandler();

    /**
     * 用于在子类当中修改需要的Option
     *
     * @param serverBootstrap
     */
    public void option(ServerBootstrap serverBootstrap) {

    }

    /**
     * 子类添加handler入口
     *
     * @param channel
     */
    public abstract void addChannelHandlers(SocketChannel channel);

    public void close() {
    }
}
