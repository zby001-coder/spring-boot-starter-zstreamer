package zstreamer.http.example.handler.httpflv;

import io.netty.handler.codec.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import zstreamer.rtmp.example.MemoryMediaMessagePool;
import zstreamer.commons.annotation.RequestPath;
import zstreamer.commons.util.InstanceTool;
import zstreamer.http.entity.request.WrappedRequest;
import zstreamer.http.entity.response.WrappedResponse;
import zstreamer.http.handler.AbstractHttpHandler;
import zstreamer.rtmp.stream.MediaMessagePool;


/**
 * @author 张贝易
 * 观众拉流处理器
 */
@RequestPath("/live/audience/{roomName}")
public class AudienceHandler extends AbstractHttpHandler {
    private final MediaMessagePool mediaMessagePool;

    public AudienceHandler(MediaMessagePool mediaMessagePool) {
        this.mediaMessagePool = mediaMessagePool;
    }

    @Override
    protected WrappedResponse handleGet(WrappedRequest msg) throws Exception {
        String roomName = (String) msg.getParam("roomName");
        if (mediaMessagePool.hasRoom(roomName)) {
            DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "video/x-flv");
            return new FlvChunkResponse(response, msg, roomName, 0, mediaMessagePool);
        } else {
            return InstanceTool.getNotFoundResponse(msg);
        }
    }
}
