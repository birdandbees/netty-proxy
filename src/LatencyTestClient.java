/**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/31/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class LatencyTestClient {
    private final String host;
    private final int port;


    public LatencyTestClient(String host, int port) {
        this.host = host;
        this.port = port;

    }

    public void run(final int delay, final int threshold) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new LatencyTestClientHandler(delay, threshold));
                        }
                    });


            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {

            group.shutdownGracefully();
        }
    }


}
