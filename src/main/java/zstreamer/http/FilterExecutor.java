package zstreamer.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;
import zstreamer.commons.loader.UrlBeanTier;
import zstreamer.http.entity.request.RequestInfo;
import zstreamer.http.entity.request.WrappedHead;
import zstreamer.http.entity.response.WrappedResponse;
import zstreamer.http.filter.AbstractHttpFilter;

import java.util.List;
import java.util.Optional;

/**
 * @author 张贝易
 * 过滤器执行工具
 */
@ChannelHandler.Sharable
@Component
public class FilterExecutor extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof WrappedHead)) {
            ctx.fireChannelRead(msg);
            return;
        }
        //获取所有的filter并执行它们的handleIn
        List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> filterInfo = ((WrappedHead) msg).getFilterInfo();
        for (UrlBeanTier.BeanInfo<AbstractHttpFilter> info : filterInfo) {
            AbstractHttpFilter filter = info.getBean();
            WrappedResponse response = filter.handleIn((WrappedHead) msg);
            //如果filter生成了response，那么可以直接响应了
            if (response != null) {
                ReferenceCountUtil.release(msg);
                ctx.channel().writeAndFlush(response);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //获取所有的filter并执行它们的handleOut
        Optional<List<UrlBeanTier.BeanInfo<AbstractHttpFilter>>> filterInfos;
        WrappedResponse response = (WrappedResponse) msg;
        filterInfos = Optional.ofNullable(response).map(WrappedResponse::getRequestInfo).map(RequestInfo::getFilterInfo);
        if (filterInfos.isPresent()) {
            for (UrlBeanTier.BeanInfo<AbstractHttpFilter> info : filterInfos.get()) {
                AbstractHttpFilter filter = info.getBean();
                filter.handleOut((WrappedResponse) msg);
            }
        }
        ctx.write(msg, promise);
    }
}
