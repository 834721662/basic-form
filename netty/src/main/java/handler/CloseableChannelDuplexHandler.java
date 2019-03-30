package handler;

import io.netty.channel.ChannelDuplexHandler;

/**
 * @author zj
 * @since 2019/3/30
 */
public abstract class CloseableChannelDuplexHandler extends ChannelDuplexHandler {

    public abstract void close();

}
