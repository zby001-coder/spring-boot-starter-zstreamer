package zstreamer.rtmp.stream.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import zstreamer.rtmp.stream.MediaMessagePool;
import zstreamer.rtmp.stream.Streamer;
import zstreamer.rtmp.message.messageType.media.MediaMessage;

/**
 * @author 张贝易
 * 主播的媒体流处理器，完成推媒体流到池中，写出FLV文件的工作
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StreamerMediaHandler extends SimpleChannelInboundHandler<MediaMessage> {
    @Autowired
    private MediaMessagePool mediaMessagePool;
    private Streamer streamer;
    private ChannelHandlerContext context;

    /**
     * 创建房间，向媒体流池中开启一个房间
     *
     * @param roomName 房间的名称
     */
    public boolean createRoom(String roomName) {
        Streamer streamer = new Streamer(roomName, context, mediaMessagePool);
        this.streamer = streamer;
        return mediaMessagePool.createRoom(roomName, streamer);
    }

    public Streamer getStreamer() {
        return streamer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MediaMessage msg) throws Exception {
        streamer.pushNewMessage(msg);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
        super.channelRegistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        mediaMessagePool.closeRoom(streamer.getRoomName());
    }
}
