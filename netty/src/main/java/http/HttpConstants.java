package http;

/**
 * @author zj
 * @since 2019/3/30
 */
public class HttpConstants {

    public static final String REQUEST_URI = "request_url";
    public static final String REQUEST_START_TIME = "request_start_time";
    public static final String REQUEST_METHOD = "request_method";

    public static final String CLIENT_IP = "client_ip";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";
    public static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    public static final String URI_HEALTH_STATUS_DEFAULT = "/checkhealth/index.jsp"; // 兼容tomcat的checkhealth路径,等同于/health-status/get
    public static final String URI_HEALTH_STATUS_GET = "/health-status/get"; //netty http服务健康状态获取，用于前端proxy监控http服务
    public static final String URI_HEALTH_STATUS_SET = "/health-status/set"; //netty http服务健康状态修改，用于项目维护；简单实现只支持get请求
    public static final String URI_VERSION = "/__version__"; //netty http服务版本查看
    public static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    public static final String PARAM_STATUS = "status";
    public static final String OK = "ok";

    public static final String ACCESS_LOG_TIME_FORMAT = "dd/MM/yyyy:HH:mm:ss,SSS Z";
}
