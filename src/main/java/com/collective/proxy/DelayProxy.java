package main.java.com.collective.proxy; /**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/23/13
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class DelayProxy {
    private final int localPort;
    private final int relayPort;
    private final String LOCAL_HOST = "127.0.0.1";
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;


    public DelayProxy(int source, int target) {
        localPort = source;
        relayPort = target;
    }


    public void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.AUTO_READ, false);

    }

    public void delay(int seconds) {

        serverBootstrap.childHandler(new DelayProxyInitializer(LOCAL_HOST, relayPort, seconds))
                .bind(localPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if (!future.isSuccess()) {
                    future.channel().close();
                    throw new java.net.BindException();
                }
            }
        });
    }

    public void shutDown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }

    public boolean isShutDown() {
        return bossGroup.isShuttingDown() && workerGroup.isShuttingDown();
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(localPort).
                toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        DelayProxy rhs = (DelayProxy) obj;
        return new EqualsBuilder()
                .append(localPort, rhs.localPort)
                .isEquals();
    }


}
