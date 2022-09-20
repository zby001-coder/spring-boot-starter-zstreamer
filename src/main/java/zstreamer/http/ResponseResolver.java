package zstreamer.http;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedFile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import zstreamer.commons.constance.ServerPropertyDefault;
import zstreamer.commons.constance.ServerPropertyKeys;
import zstreamer.http.entity.response.WrappedResponse;
import zstreamer.http.entity.response.chunk.ChunkedResponse;
import zstreamer.http.entity.response.chunk.SuccessorChuck;
import zstreamer.http.entity.response.file.FileResponse;
import zstreamer.http.entity.response.simple.SimpleResponse;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

/**
 * @author 张贝易
 * 将包装的响应解析成可用的数据
 */
@ChannelHandler.Sharable
@Component
public class ResponseResolver extends ChannelOutboundHandlerAdapter {
    private final boolean sslEnabled;
    private final int fileChunkSize;
    private final int chunkMaxFailTime;
    private final int maxChunkRetryInterval;
    private final int minChunkRetryInterval;
    private final int initialChunkRetryInterval;
    private final int preferSuccessChunk;
    private final int fastRetryInterval;

    public ResponseResolver(Environment env) {
        this.sslEnabled = env.getProperty(ServerPropertyKeys.SSL_ENABLED, Boolean.class, ServerPropertyDefault.SSL_ENABLED);
        this.fileChunkSize = env.getProperty(ServerPropertyKeys.FILE_CHUNK_SIZE, Integer.class, ServerPropertyDefault.FILE_CHUNK_SIZE);
        this.chunkMaxFailTime = env.getProperty(ServerPropertyKeys.CHUNK_RESPONSE_FAIL_MAX_TIME, Integer.class, ServerPropertyDefault.CHUNK_RESPONSE_FAIL_MAX_TIME);
        this.minChunkRetryInterval = env.getProperty(ServerPropertyKeys.MIN_CHUNK_RESPONSE_RETRY_INTERVAL, Integer.class, ServerPropertyDefault.MIN_CHUNK_RESPONSE_RETRY_INTERVAL);
        this.maxChunkRetryInterval = env.getProperty(ServerPropertyKeys.MAX_CHUNK_RESPONSE_RETRY_INTERVAL, Integer.class, ServerPropertyDefault.MAX_CHUNK_RESPONSE_RETRY_INTERVAL);
        this.initialChunkRetryInterval = env.getProperty(ServerPropertyKeys.INITIAL_CHUNK_RESPONSE_RETRY_INTERVAL, Integer.class, ServerPropertyDefault.INITIAL_CHUNK_RESPONSE_RETRY_INTERVAL);
        this.preferSuccessChunk = env.getProperty(ServerPropertyKeys.PREFER_SUCCESS_CHUNK, Integer.class, ServerPropertyDefault.PREFER_SUCCESS_CHUNK);
        this.fastRetryInterval = env.getProperty(ServerPropertyKeys.FAST_FAIL_RETRY_INTERVAL, Integer.class, ServerPropertyDefault.FAST_FAIL_RETRY_INTERVAL);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof WrappedResponse)) {
            return;
        }
        if (msg instanceof ChunkedResponse) {
            //由于未知的问题，HttpServerCodec发送Chunk过的信息无法正常展示
            //所以使用自己的chunk逻辑，同时提供重试机制和无限推送的机制
            ctx.channel().eventLoop().execute(new Runnable() {
                private final ChannelHandlerContext context = ctx;
                private final ChunkedResponse generator = (ChunkedResponse) msg;
                private int failTimes = 0;
                private int successTimes = 0;
                private int lastWaitTime = 0;

                {
                    //先写一个响应头，然后去除HttpCodec
                    context.writeAndFlush(generator.getDelegate());
                    context.pipeline().remove(HttpServerCodec.class);
                }

                @Override
                public void run() {
                    //如果channel已经关闭，写一个结尾
                    if (!ctx.channel().isActive()) {
                        context.writeAndFlush(new SuccessorChuck(new byte[0]).getChunkContent(), promise);
                        context.pipeline().addFirst(new HttpServerCodec());
                        return;
                    }
                    SuccessorChuck successorChuck = generator.generateChunk();
                    if (successorChuck != null) {
                        //不为空，说明有内容
                        failTimes = 0;
                        if (!successorChuck.isEnd()) {
                            //记录当前成功次数，为计算下一次等待时间做准备
                            successTimes++;
                            context.writeAndFlush(successorChuck.getChunkContent());
                            context.channel().eventLoop().execute(this);
                        } else {
                            //如果不为空，但没有内容，说明到了结尾了，将promise放入，提示整个chunkedResponse响应完成
                            context.writeAndFlush(successorChuck.getChunkContent(), promise);
                            context.pipeline().addFirst(new HttpServerCodec());
                        }
                    } else {
                        //为空，给它几次重试的机会
                        failTimes++;
                        if (failTimes > chunkMaxFailTime) {
                            //重试几次都不行，直接结束
                            context.writeAndFlush(new SuccessorChuck(new byte[0]).getChunkContent(), promise);
                            context.pipeline().addFirst(new HttpServerCodec());
                        } else {
                            int waitTime;
                            //如果一个都没成功，就要进入快速重试阶段
                            if (successTimes == 0) {
                                waitTime = fastRetryInterval;
                            } else {
                                //如果当前是第一次重试，使用初始等待时间
                                //否则根据成功次数和上次等待时间计算出一个chunk要等几秒，然后根据期望连续成功次数计算出等待时间
                                waitTime = lastWaitTime == 0 ? initialChunkRetryInterval : (int) (Math.ceil(((double) lastWaitTime / successTimes) * preferSuccessChunk));
                                waitTime = Integer.min(maxChunkRetryInterval, waitTime);
                                waitTime = Integer.max(minChunkRetryInterval, waitTime);
                            }
                            successTimes = 0;
                            lastWaitTime = waitTime;
                            context.channel().eventLoop().schedule(this, waitTime, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });
        } else if (msg instanceof FileResponse) {
            FileResponse response = (FileResponse) msg;
            File file = response.getFile();
            //写响应头
            ctx.write(response.getDelegate());
            //写响应体
            if (sslEnabled) {
                ctx.write(new ChunkedFile(new RandomAccessFile(file, "r"), response.getOffSet(), response.getSize(), fileChunkSize));
            } else {
                ctx.write(new DefaultFileRegion(file, response.getOffSet(), response.getSize()));
            }
            ctx.write(new DefaultLastHttpContent(), promise);
        } else if (msg instanceof SimpleResponse) {
            //普通的响应必须是FullResponse，所以可以不用写lastContent
            ctx.write(((SimpleResponse) msg).getDelegate(), promise);
        }
    }
}
