import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/31/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */


public class TimestampServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(
            TimestampServerHandler.class.getName());


    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (((ByteBuf) msg).readInt() == 13) {
            final ByteBuf time = ctx.alloc().buffer(4);
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
            ctx.write(time);
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
