package zstreamer.commons.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import zstreamer.commons.annotation.RequestPath;
import zstreamer.commons.util.UrlTool;
import zstreamer.http.handler.AbstractHttpHandler;

import java.io.IOException;
import java.util.Collection;


/**
 * @author 张贝易
 * 解析path对应的handler
 * 先用前缀数匹配整个url，然后得到handler的url模式串，用模式串中的占位符去取出url中的参数
 * 模式串的占位符可以使用*来表示，让其可以匹配任何字符串且优先级最低
 */
@Component
public class HandlerBeanResolver {
    /**
     * 请求路径于对应的Handler的class对象的映射
     */
    private static final UrlBeanTier<AbstractHttpHandler> TIER = new UrlBeanTier<>();
    @Autowired
    private ApplicationContext context;

    private volatile boolean loaded = false;

    public UrlBeanTier.BeanInfo<AbstractHttpHandler> resolveHandler(String requestPath) throws IOException {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    loadBeans(context.getBeansOfType(AbstractHttpHandler.class).values());
                    loaded = true;
                }
            }
        }
        return doResolveHandler(requestPath);
    }

    private UrlBeanTier.BeanInfo<AbstractHttpHandler> doResolveHandler(String url) {
        return TIER.matchHandler(url);
    }

    /**
     * 将handler注册到前缀树中
     *
     * @param handlers handler
     */
    private void loadBeans(Collection<AbstractHttpHandler> handlers) {
        for (AbstractHttpHandler handler : handlers) {
            RequestPath path = handler.getClass().getDeclaredAnnotation(RequestPath.class);
            if (path == null) {
                continue;
            }
            TIER.addPrefix(path.value(), handler, UrlTool::formatHandlerPath);
        }
    }
}
