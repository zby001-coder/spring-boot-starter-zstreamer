package zstreamer.rtmp.stream;

import io.netty.channel.ChannelHandlerContext;
import zstreamer.rtmp.message.messageType.media.DataMessage;
import zstreamer.rtmp.message.messageType.media.MediaMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Streamer {
    private DataMessage metaData;
    private MediaMessage aac;
    private MediaMessage avc;
    private MediaMessage sei;
    /**
     * 房间的名称
     */
    private final String roomName;
    private final ChannelHandlerContext context;
    private final MediaMessagePool mediaMessagePool;

    public Streamer(String roomName, ChannelHandlerContext context, MediaMessagePool mediaMessagePool) {
        this.context = context;
        this.roomName = roomName;
        this.mediaMessagePool = mediaMessagePool;
    }

    /**
     * 真正执行关闭直播间的工作
     * 设置关闭状态并且通知观众直播间关闭了
     */
    public void doCloseRoom() {
        //关闭本channel
        context.channel().close();
    }

    public SocketAddress getLocalAddress() {
        return context.channel().localAddress();
    }

    public SocketAddress getRemoteAddress() {
        return context.channel().remoteAddress();
    }

    public void pushNewMessage(MediaMessage msg) throws Exception {
        if (avc == null && msg instanceof MediaMessage.VideoMessage) {
            //avc一定先于所有videoMessage
            this.avc = msg;
        } else if (aac == null && msg instanceof MediaMessage.AudioMessage) {
            //aac一定先于所有audioMessage
            this.aac = msg;
        } else if (sei == null && msg instanceof MediaMessage.VideoMessage && msg.getTimeStamp() == 0) {
            //sei必须在avc之后，而且timestamp是0，可以将它和其他的videMessage区分开来
            this.sei = msg;
        } else {
            //将MediaMessage推送到信息的池子里
            mediaMessagePool.pushMediaMessage(roomName, msg, this);
        }
    }

    public DataMessage getMetaData() {
        return metaData;
    }

    public void setMetaData(DataMessage metaData) {
        this.metaData = metaData;
    }

    public MediaMessage getAac() {
        return aac;
    }

    public MediaMessage getAvc() {
        return avc;
    }

    public MediaMessage getSei() {
        return sei;
    }

    public String getRoomName() {
        return roomName;
    }
}
