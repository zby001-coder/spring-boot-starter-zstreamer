package zstreamer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.core.env.Environment;
import zstreamer.commons.HandlerInjector;
import zstreamer.commons.constance.ServerPropertyDefault;
import zstreamer.commons.constance.ServerPropertyKeys;

import java.net.InetSocketAddress;

/**
 * @author 张贝易
 */
public class ZstreamerServer implements WebServer {
    private static final Log logger = LogFactory.getLog(ZstreamerServer.class);
    private ChannelFuture httpFuture;
    private ChannelFuture rtmpFuture;
    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;
    private final Environment env;
    private final HandlerInjector handlerInjector;
    private final InetSocketAddress httpAddress;
    private final InetSocketAddress rtmpAddress;
    private final int bossCount;
    private final int workerCount;

    public ZstreamerServer(Environment env, HandlerInjector handlerInjector) {
        this.env = env;
        this.handlerInjector = handlerInjector;
        int httpPort = env.getProperty(ServerPropertyKeys.HTTP_PORT, Integer.class, ServerPropertyDefault.HTTP_PORT);
        int rtmpPort = env.getProperty(ServerPropertyKeys.RTMP_PORT, Integer.class, ServerPropertyDefault.RTMP_PORT);
        String httpIp = env.getProperty(ServerPropertyKeys.HTTP_ADDRESS, String.class);
        String rtmpIp = env.getProperty(ServerPropertyKeys.RTMP_ADDRESS, String.class);
        httpAddress = httpIp == null ? new InetSocketAddress(httpPort) : new InetSocketAddress(httpIp, httpPort);
        rtmpAddress = rtmpIp == null ? new InetSocketAddress(rtmpPort) : new InetSocketAddress(rtmpIp, rtmpPort);
        bossCount = env.getProperty(ServerPropertyKeys.BOSS_COUNT, Integer.class, ServerPropertyDefault.BOSS_COUNT);
        workerCount = env.getProperty(ServerPropertyKeys.WORKER_COUNT, Integer.class, ServerPropertyDefault.WORKER_COUNT);
    }

    @Override
    public void start() throws WebServerException {
        boss = new NioEventLoopGroup(bossCount);
        worker = new NioEventLoopGroup(workerCount);
        ServerBootstrap server = new ServerBootstrap();
        server.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //允许监听多个端口，方便rtmp、http-flv多协议使用
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(handlerInjector);
                    }
                });
        try {
            httpFuture = server.bind(httpAddress).sync();
            rtmpFuture = server.bind(rtmpAddress).sync();
            logger.info("Zstreamer-http start success on " + httpAddress + "!!!");
            logger.info("Zstreamer-rtmp start success on " + rtmpAddress + "!!!");
        } catch (InterruptedException e) {
            logger.error("Fail to start!!!");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    @Override
    public void stop() throws WebServerException {
        try {
            rtmpFuture.channel().close().sync();
            httpFuture.channel().close().sync();
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        } catch (InterruptedException e) {
            logger.error("Fail to shutDown!!!");
        }
    }

    @Override
    public int getPort() {
        return httpAddress.getPort();
    }
}
