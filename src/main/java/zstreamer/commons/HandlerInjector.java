package zstreamer.commons;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import zstreamer.commons.constance.ServerPropertyDefault;
import zstreamer.commons.constance.ServerPropertyKeys;
import zstreamer.http.*;
import zstreamer.rtmp.chunk.ChunkCodec;
import zstreamer.rtmp.handshake.RtmpHandShaker;
import zstreamer.rtmp.message.codec.RtmpMessageDecoder;
import zstreamer.rtmp.message.codec.RtmpMessageEncoder;
import zstreamer.rtmp.stream.handler.MessageHandlerInitializer;
import zstreamer.rtmp.message.handler.control.AckSenderReceiver;
import zstreamer.ssl.SslHandlerBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

/**
 * @author 张贝易
 * 初始化所有handler的工具
 */
@ChannelHandler.Sharable
@Component
public class HandlerInjector extends ChannelInitializer<SocketChannel> {
    @Autowired
    private RequestDispatcher requestDispatcher;
    @Autowired
    private ResponseResolver responseResolver;
    @Autowired
    private FilterExecutor filterExecutor;
    @Autowired
    private RequestResolver requestResolver;
    @Autowired
    private ApplicationContext ctx;
    private final Environment env;

    public HandlerInjector(Environment env) {
        this.env = env;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new ChannelTrafficShapingHandler(
                env.getProperty(ServerPropertyKeys.BYTE_OUT_PER_SECOND,
                        Integer.class, ServerPropertyDefault.BYTE_OUT_PER_SECOND),
                env.getProperty(ServerPropertyKeys.BYTE_IN_PER_SECOND,
                        Integer.class, ServerPropertyDefault.BYTE_IN_PER_SECOND),
                env.getProperty(ServerPropertyKeys.CHECK_INTERVAL,
                        Integer.class, ServerPropertyDefault.CHECK_INTERVAL)));
        InetSocketAddress address = ch.localAddress();
        int port = address.getPort();
        if (port == env.getProperty(ServerPropertyKeys.HTTP_PORT, Integer.class, ServerPropertyDefault.HTTP_PORT)) {
            initHttp(ch.pipeline());
        } else if (port == env.getProperty(ServerPropertyKeys.RTMP_PORT, Integer.class, ServerPropertyDefault.RTMP_PORT)) {
            initRtmp(ch.pipeline());
        }
        ch.pipeline().remove(this.getClass());
    }

    private void initHttp(ChannelPipeline pipeline) throws SSLException {
        if (env.getProperty(ServerPropertyKeys.SSL_ENABLED, Boolean.class, ServerPropertyDefault.SSL_ENABLED)) {
            pipeline.addLast(SslHandlerBuilder.instance(pipeline.channel()));
        }
        pipeline
                .addLast(new HttpServerCodec())
                .addLast(new ChunkedWriteHandler())
                .addLast(responseResolver)
                .addLast(requestResolver)
                .addLast(filterExecutor)
                .addLast(requestDispatcher)
                .addLast(new ContextHandler(env));
    }

    private void initRtmp(ChannelPipeline pipeline) {
        pipeline
                .addLast(ctx.getBean(RtmpHandShaker.class))
                .addLast(ctx.getBean(AckSenderReceiver.class))
                .addLast(ctx.getBean(ChunkCodec.class))
                .addLast(ctx.getBean(RtmpMessageDecoder.class))
                .addLast(ctx.getBean(RtmpMessageEncoder.class))
                .addLast(ctx.getBean(MessageHandlerInitializer.class));
    }
}
