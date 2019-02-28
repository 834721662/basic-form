package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import server.Action.Action;


/**
 * handler action 跳转类，选择对应的action
 * @author zj
 * @since 2019/2/27
 */
public class SimpleHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Router router;

    SimpleHttpServerHandler(Router router) {
        this.router = router;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {

        Action action = router.route("/url/example");
        action.doAction();
        // do anything you want here


        // 唤醒下一个存在pipeline当中的ChannelInboundHandler
        ctx.fireChannelRead(msg.retain());
    }

}
