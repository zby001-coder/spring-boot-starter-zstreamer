package zstreamer.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;
import zstreamer.http.entity.request.WrappedRequest;
import zstreamer.http.handler.AbstractHttpHandler;

/**
 * @author 张贝易
 * 分发消息给handler
 */
@ChannelHandler.Sharable
@Component
public class RequestDispatcher extends SimpleChannelInboundHandler<WrappedRequest> {

    public RequestDispatcher() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WrappedRequest msg) throws Exception {
        AbstractHttpHandler handler = getServiceHandler(msg);
        handler.channelRead(ctx, msg);
    }

    private AbstractHttpHandler getServiceHandler(WrappedRequest request) throws Exception {
        return request.getHandlerInfo().getBean();

    }
}
