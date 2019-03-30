package handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.zj.basicform.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zj
 * @since 2019/3/30
 */
public class LoggingMonitorHandler extends GlobalStatsMonitorHandler {

    private static final String MONITOR_LOG_NAME = "qps_monitor";
    private static Logger logger = LoggerFactory.getLogger(MONITOR_LOG_NAME);
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("monitor-handler"));
    private static List<StatisticsType> metrics = Lists.newArrayList(StatisticsType.HANDLER_ADD,
            StatisticsType.HANDLER_REMOVE,
            StatisticsType.CHANNEL_ACTIVE,
            StatisticsType.CHANNEL_INACTIVE,
            StatisticsType.CHANNEL_REGISTER,
            StatisticsType.CHANNEL_UNREGISTER,
            StatisticsType.READ_BYTES,
            StatisticsType.CLOSE);

    public LoggingMonitorHandler() {
        super(scheduler, 10, TimeUnit.SECONDS);
    }

    @Override
    public void closeExecutor() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void handleMetrics(Multiset<String> stats) {
        StringBuilder sb = new StringBuilder();
        for (StatisticsType metric : metrics) {
            sb.append(metric).append(": ").append(stats.count(metric.name())).append(", ");
        }
        logger.info(sb.toString());
    }

}
