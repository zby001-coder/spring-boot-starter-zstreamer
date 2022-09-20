package zstreamer.rtmp.stream.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import zstreamer.rtmp.message.messageType.media.DataMessage;

/**
 * @author 张贝易
 * 用这个handler来保存最近一次Stream的元数据，即@setDataFrame里的内容
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MetaDataHandler extends SimpleChannelInboundHandler<DataMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataMessage msg) throws Exception {
        if (DataMessage.META_DATA.equals(msg.getCommand().getValue())) {
            StreamerMediaHandler streamerMediaHandler = ctx.pipeline().get(StreamerMediaHandler.class);
            streamerMediaHandler.getStreamer().setMetaData(msg);
        }
    }
}
