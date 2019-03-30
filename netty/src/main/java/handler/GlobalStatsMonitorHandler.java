package handler;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全局监控类
 * 监控姿势：通过netty socketChannel连接时候调用的就绪方法实现，目前仅能实现长连接的监控
 * todo 实现短链接的请求监控
 *
 * @author zj
 * @since 2019/3/30
 */
@ChannelHandler.Sharable
public abstract class GlobalStatsMonitorHandler extends CloseableChannelDuplexHandler {

    public static final int DEFAULT_CHECK_INTERVAL = 30;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    private static Logger logger = LoggerFactory.getLogger(GlobalStatsMonitorHandler.class);
    /**
     * 利用multiSet实现数量监控，可以clear，也可以通过其他数据结构实现
     */
    private static Multiset<String> stats = ConcurrentHashMultiset.create();
    private static MonitorRunner monitorRunner;

    public GlobalStatsMonitorHandler(ScheduledExecutorService executor) {
        this(executor, DEFAULT_CHECK_INTERVAL);
    }

    public GlobalStatsMonitorHandler(ScheduledExecutorService executor, int checkInterval) {
        this(executor, checkInterval, DEFAULT_TIME_UNIT);
    }

    public GlobalStatsMonitorHandler(ScheduledExecutorService executor, int checkInterval, TimeUnit timeUnit) {
        start(executor, checkInterval, timeUnit);
    }

    private synchronized void start(ScheduledExecutorService executor, int checkInterval, TimeUnit timeUnit) {
        if (monitorRunner == null) {
            monitorRunner = new MonitorRunner(executor, checkInterval, timeUnit);
            try {
                monitorRunner.start();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    public void close() {
        if (monitorRunner != null) {
            try {
                monitorRunner.stop();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        closeExecutor();
    }

    public abstract void closeExecutor();


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future) throws Exception {
        super.bind(ctx, localAddress, future);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise future) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, future);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        super.disconnect(ctx, future);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        stats.add(StatisticsType.CLOSE.name());
        super.close(ctx, future);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        super.deregister(ctx, future);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.CHANNEL_REGISTER.name());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.CHANNEL_UNREGISTER.name());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.CHANNEL_ACTIVE.name());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.CHANNEL_INACTIVE.name());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long size = size(msg);
        if (size > 0) {
            stats.add(StatisticsType.READ_BYTES.name(), (int) size);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.CHANNEL_READ_COMPLETE.name());
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        stats.add(StatisticsType.EXCEPTION.name());
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.HANDLER_ADD.name());
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        stats.add(StatisticsType.HANDLER_REMOVE.name());
        super.handlerRemoved(ctx);
    }

    protected long size(Object msg) {
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).readableBytes();
        }
        if (msg instanceof ByteBufHolder) {
            return ((ByteBufHolder) msg).content().readableBytes();
        }
        return -1;
    }

    /**
     * 利用调度线程实现
     */
    class MonitorRunner {

        private AtomicBoolean isRunning = new AtomicBoolean();
        private ScheduledExecutorService executor;
        private ScheduledFuture scheduledFuture;
        private int checkInterval;
        private TimeUnit timeUnit;
        private MonitorTask task;

        public MonitorRunner(ScheduledExecutorService executor, int checkInterval, TimeUnit timeUnit) {
            this.executor = executor;
            this.checkInterval = checkInterval;
            this.timeUnit = timeUnit;
        }

        public void start() throws Exception {
            if (isRunning.compareAndSet(false, true)) {
                task = new MonitorTask();
                scheduledFuture = executor.schedule(task, checkInterval, timeUnit);
                logger.info("global stats monitor start.");
            }
        }

        public void stop() throws Exception {
            if (isRunning.compareAndSet(true, false)) {
                task.handle();
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
                logger.info("global stats monitor close.");
            }
        }
    }

    class MonitorTask implements Runnable {

        @Override
        public void run() {
            handle();
        }

        public void handle() {
            try {
                if (!stats.isEmpty()) {
                    handleMetrics(stats);
                }
            } finally {
                reset();
                monitorRunner.executor.schedule(this, monitorRunner.checkInterval, monitorRunner.timeUnit);
            }
        }
    }

    public void reset() {
        stats.clear();
    }

    public abstract void handleMetrics(Multiset<String> stats);

    /**
     * 监控指标
     */
    public enum StatisticsType {
        CHANNEL_REGISTER, CHANNEL_ACTIVE, READ_BYTES, CHANNEL_READ_COMPLETE, CHANNEL_INACTIVE,
        CHANNEL_UNREGISTER, HANDLER_ADD, HANDLER_REMOVE, EXCEPTION, CLOSE
    }

}
