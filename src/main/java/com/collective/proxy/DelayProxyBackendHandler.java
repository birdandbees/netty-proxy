package main.java.com.collective.proxy; /**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/29/13
 * Time: 10:38 AM
 * To change this template use File | Settings | File Templates.
 */

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

public class DelayProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;
    private volatile int seconds;

    public DelayProxyBackendHandler(Channel inboundChannel, int seconds) {
        this.inboundChannel = inboundChannel;
        this.seconds = seconds;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
        ctx.write(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (seconds != 0) {
            Thread.sleep(seconds * 1000);
        }
        inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        DelayProxyFrontendHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        DelayProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}
