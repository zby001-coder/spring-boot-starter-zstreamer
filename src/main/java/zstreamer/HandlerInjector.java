package zstreamer;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.springframework.core.env.Environment;
import zstreamer.commons.constance.ServerPropertyDefault;
import zstreamer.commons.constance.ServerPropertyKeys;
import zstreamer.http.*;
import zstreamer.rtmp.chunk.ChunkCodec;
import zstreamer.rtmp.handshake.RtmpHandShaker;
import zstreamer.rtmp.message.codec.RtmpMessageDecoder;
import zstreamer.rtmp.message.codec.RtmpMessageEncoder;
import zstreamer.rtmp.message.handlers.MessageHandlerInitializer;
import zstreamer.rtmp.message.handlers.control.AckSenderReceiver;
import zstreamer.ssl.SslHandlerBuilder;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;

/**
 * @author 张贝易
 * 初始化所有handler的工具
 */
@ChannelHandler.Sharable
public class HandlerInjector extends ChannelInitializer<SocketChannel> {
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
                .addLast(ResponseResolver.getInstance(env))
                .addLast(RequestResolver.getInstance(env))
                .addLast(FilterExecutor.getInstance())
                .addLast(RequestDispatcher.getInstance())
                .addLast(new ContextHandler(env));
    }

    private void initRtmp(ChannelPipeline pipeline) {
        pipeline
                .addLast(new RtmpHandShaker())
                .addLast(new AckSenderReceiver())
                .addLast(new ChunkCodec())
                .addLast(new RtmpMessageDecoder())
                .addLast(new RtmpMessageEncoder())
                .addLast(new MessageHandlerInitializer());
    }
}
