package zstreamer.rtmp.stream.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import zstreamer.rtmp.message.handler.command.CommandHandler;
import zstreamer.rtmp.message.handler.control.ChunkSizeHandler;
import zstreamer.rtmp.message.handler.control.PeerBandWidthHandler;
import zstreamer.rtmp.message.handler.control.WindowAckSizeHandler;

/**
 * @author 张贝易
 * 初始化各种rtmp消息处理器的handler
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageHandlerInitializer extends ChannelInboundHandlerAdapter {
    @Autowired
    private ApplicationContext appCtx;
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        initMessageHandlers(ctx);
        super.channelRegistered(ctx);
        ctx.pipeline().remove(this.getClass());
    }

    private void initMessageHandlers(ChannelHandlerContext ctx) {
        ctx.pipeline()
                .addLast(appCtx.getBean(ChunkSizeHandler.class))
                .addLast(appCtx.getBean(CommandHandler.class))
                .addLast(appCtx.getBean(WindowAckSizeHandler.class))
                .addLast(appCtx.getBean(PeerBandWidthHandler.class))
                .addLast(appCtx.getBean(MetaDataHandler.class));
    }
}
