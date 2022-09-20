package zstreamer.http.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.concurrent.FastThreadLocal;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public class ObjectMapperFastThreadLocal extends FastThreadLocal<ObjectMapper> {
    @Override
    protected ObjectMapper initialValue() throws Exception {
        return new ObjectMapper();
    }
}
