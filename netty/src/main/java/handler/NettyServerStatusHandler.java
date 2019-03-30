package handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author zj
 * @since 2019/3/30
 */
@ChannelHandler.Sharable
public class NettyServerStatusHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


}
