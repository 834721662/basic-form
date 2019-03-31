package http;

import io.netty.util.AttributeKey;

/**
 * @author zj
 * @since 2019/3/31
 */
public class AttributeKeys {

    public static final AttributeKey<String> CLIENT_IP = AttributeKey.valueOf(HttpConstants.CLIENT_IP);
    public static final AttributeKey<String> REQUEST_URI = AttributeKey.valueOf(HttpConstants.REQUEST_URI);
    public static final AttributeKey<String> REQUEST_START_TIME = AttributeKey.valueOf(HttpConstants.REQUEST_START_TIME);
    public static final AttributeKey<String> REQUEST_METHOD = AttributeKey.valueOf(HttpConstants.REQUEST_METHOD);
    public static final AttributeKey<String> X_REAL_IP = AttributeKey.valueOf(HttpConstants.HEADER_X_REAL_IP);
    public static final AttributeKey<String> X_FORWARDED_FOR = AttributeKey.valueOf(HttpConstants.HEADER_X_FORWARDED_FOR);
}
