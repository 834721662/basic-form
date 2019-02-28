package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author zj
 * @since 2019/2/27
 */
public class HttpServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    public void start() throws InterruptedException {
        int port = 8809;

        // 配置服务端nio线程组
        bossGroup  = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap s = new ServerBootstrap();
            s.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            // 绑定端口，同步等待
            Channel ch = s.bind(port).sync().channel();
            ch.closeFuture().sync();
        } finally {
            // 优雅退出，释放资源
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}
