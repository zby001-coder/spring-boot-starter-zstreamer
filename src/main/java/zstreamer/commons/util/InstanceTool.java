package zstreamer.commons.util;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.stereotype.Component;
import zstreamer.http.entity.request.WrappedRequest;
import zstreamer.http.entity.response.WrappedResponse;
import zstreamer.http.entity.response.simple.SimpleResponse;

@Component
public class InstanceTool {
    public static WrappedResponse getNotFoundResponse(WrappedRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return new SimpleResponse(response, request);
    }

    public static WrappedResponse getWrongMethodResponse(WrappedRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return new SimpleResponse(response, request);
    }

    public static WrappedResponse getExceptionResponse(WrappedRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, "0");
        return new SimpleResponse(response, request);
    }

    public static WrappedResponse getEmptyOkResponse(WrappedRequest request) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, "0");
        return new SimpleResponse(response, request);
    }
}
