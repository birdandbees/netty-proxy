package com.collective.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class DelayProxyInitializer extends ChannelInitializer<SocketChannel> {


    private final String remoteHost;
    private final int remotePort;
    private volatile int seconds;

    public DelayProxyInitializer(String remoteHost, int remotePort, int seconds) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.seconds = seconds;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                //new LoggingHandler(LogLevel.INFO),
                new DelayProxyFrontendHandler(remoteHost, remotePort, seconds));
    }

}
