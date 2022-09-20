package zstreamer.http.entity.response.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import zstreamer.http.entity.request.WrappedRequest;
import zstreamer.http.util.ObjectMapperFastThreadLocal;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public class SimpleJsonResult extends SimpleResponse {
    private static final ObjectMapperFastThreadLocal OBJECT_MAPPER = new ObjectMapperFastThreadLocal();

    public SimpleJsonResult(DefaultFullHttpResponse header, WrappedRequest request, Object body) throws JsonProcessingException {
        super(header, request);
        header.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        header.content().writeBytes(Unpooled.wrappedBuffer(OBJECT_MAPPER.get().writeValueAsBytes(body)));
    }
}
