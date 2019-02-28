package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * http解析
 * @author zj
 * @since 2019/2/28
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private Router router;

    HttpServerInitializer() {
        this.router = new Router();
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new SimpleHttpServerHandler(router));
    }
}
