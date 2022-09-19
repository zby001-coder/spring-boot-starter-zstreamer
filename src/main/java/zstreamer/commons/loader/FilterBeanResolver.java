package zstreamer.commons.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import zstreamer.commons.annotation.FilterPath;
import zstreamer.commons.util.UrlTool;
import zstreamer.http.filter.AbstractHttpFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author 张贝易
 * filter的匹配工具
 */
@Component
public class FilterBeanResolver {
    /**
     * 请求路径于对应的Handler的class对象的映射
     */
    private static final UrlBeanTier<AbstractHttpFilter> TIER = new UrlBeanTier<>();
    @Autowired
    private ApplicationContext context;
    private volatile boolean loaded = false;

    public List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> resolveFilter(String pathPattern) throws IOException {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    loadClasses(context.getBeansOfType(AbstractHttpFilter.class).values());
                    loaded = true;
                }
            }
        }
        return doResolveFilter(pathPattern);
    }

    private List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> doResolveFilter(String pattern) {
        return TIER.matchFilter(pattern);
    }

    /**
     * 将filter注册到前缀树中
     *
     * @param filters filter
     */
    private void loadClasses(Collection<AbstractHttpFilter> filters) {
        for (AbstractHttpFilter filter : filters) {
            FilterPath path = filter.getClass().getDeclaredAnnotation(FilterPath.class);
            if (path == null) {
                continue;
            }
            TIER.addPrefix(path.value(), filter, UrlTool::formatFilterPath);
        }
    }
}
