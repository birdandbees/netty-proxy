package test.java.com.collective.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/31/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LatencyTestClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(
            LatencyTestClientHandler.class.getName());
    private final int delay;
    private final int threshold;

    public LatencyTestClientHandler(int delay, int threshold) {
        this.delay = delay;
        this.threshold = threshold;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copyInt(13));

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf m = (ByteBuf) msg;
        try {

            long serverTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
            long latency = (System.currentTimeMillis() - serverTimeMillis) / 1000;

            if (latency < delay || latency > delay + threshold) {
                throw new SocketTimeoutException("latency test failed");
            }
            System.out.println("Server response delay:" + latency + " seconds");
            ctx.close();
        } finally {
            m.release();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
        ctx.close();
    }
}
