package zstreamer.commons.loader;

import zstreamer.commons.constance.HandlerConstance;
import zstreamer.commons.util.UrlTool;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author 张贝易
 * @date 2022/9/19
 **/
public class UrlBeanTier<T> {
    private final HashMap<String, UrlBeanTier<T>> children = new HashMap<>();
    private final String present;
    private String url;
    private final LinkedList<T> handlers = new LinkedList<>();

    public UrlBeanTier() {
        present = "";
    }

    /**
     * 构造前缀树节点，同时也是一个前缀树
     *
     * @param splitPrefix 所有前缀的数组
     * @param idx         当前前缀的索引
     * @param handler     url对应的handler的实例
     */
    private UrlBeanTier(String[] splitPrefix, int idx, T handler, String url) {
        present = splitPrefix[idx];
        if (idx + 1 < splitPrefix.length) {
            children.put(splitPrefix[idx + 1], new UrlBeanTier<T>(splitPrefix, idx + 1, handler, url));
        } else {
            this.handlers.add(handler);
            this.url = url;
        }
    }

    /**
     * 从头开始添加前缀
     *
     * @param url     url字符串
     * @param handler url对应的handler对象
     */
    public void addPrefix(String url, T handler, Function<String, String> initializer) {
        String origin = url;
        url = initializer.apply(url);
        String[] splitPrefix = url.split("/");
        if (!children.containsKey(splitPrefix[0])) {
            children.put(splitPrefix[0], new UrlBeanTier<T>(splitPrefix, 0, handler, origin));
        } else if (splitPrefix.length > 1) {
            children.get(splitPrefix[0]).addPrefix(splitPrefix, 1, handler, origin);
        } else {
            children.get(splitPrefix[0]).handlers.add(handler);
            children.get(splitPrefix[0]).url = url;
        }
    }

    /**
     * 添加儿子前缀
     *
     * @param splitPrefix 前缀数组
     * @param childIdx    当前tier的儿子前缀的索引，即下一个tier的前缀索引
     * @param handler     url对应的handler对象
     * @param origin      原始的url
     */
    private void addPrefix(String[] splitPrefix, int childIdx, T handler, String origin) {
        if (!children.containsKey(splitPrefix[childIdx])) {
            children.put(splitPrefix[childIdx], new UrlBeanTier<T>(splitPrefix, childIdx, handler, origin));
        } else if (splitPrefix.length > childIdx + 1) {
            children.get(splitPrefix[childIdx]).addPrefix(splitPrefix, childIdx + 1, handler, origin);
        } else {
            children.get(splitPrefix[childIdx]).handlers.add(handler);
            children.get(splitPrefix[childIdx]).url = origin;
        }
    }

    public BeanInfo<T> matchHandler(String url) {
        url = UrlTool.formatHandlerPath(url);
        return matchHandler(url.split("/"), 0, this);
    }

    /**
     * 用一个url找出与之对应的handler
     *
     * @param prefixes url切分出来的前缀
     * @param childIdx 当前tier的儿子前缀的索引，即下一个tier的前缀索引
     * @param tier     当前前缀树
     * @return handler信息
     */
    private BeanInfo<T> matchHandler(String[] prefixes, int childIdx, UrlBeanTier<T> tier) {
        BeanInfo<T> result = null;
        if (tier == null) {
            return null;
        }
        if (childIdx < prefixes.length && tier.children.containsKey(prefixes[childIdx])) {
            result = matchHandlerExactly(prefixes, childIdx, tier);
        }
        if (childIdx < prefixes.length && result == null && tier.children.containsKey(HandlerConstance.PLACE_HOLDER_REPLACER_STR)) {
            result = matchHandlerPlaceholder(prefixes, childIdx, tier);
        }
        return result;
    }

    /**
     * 儿子匹配精确的前缀（孙子可以用占位符）
     *
     * @param prefixes 前缀数组
     * @param childIdx 儿子的前缀的索引
     * @param tier     当前的前缀树
     * @return 如果能一直匹配到底就返回handler信息，否则返回null
     */
    private BeanInfo<T> matchHandlerExactly(String[] prefixes, int childIdx, UrlBeanTier<T> tier) {
        if (childIdx + 1 >= prefixes.length) {
            UrlBeanTier<T> child = tier.children.get(prefixes[childIdx]);
            return (child.url != null && hasElement(child.handlers)) ? new BeanInfo<T>(child.handlers.get(0), child.url) : null;
        } else {
            return matchHandler(prefixes, childIdx + 1, tier.children.get(prefixes[childIdx]));
        }
    }

    /**
     * 儿子匹配占位符（孙子可以用精确的字符）
     *
     * @param prefixes 前缀数组
     * @param childIdx 儿子的前缀的索引
     * @param tier     当前的前缀树
     * @return 如果能一直匹配到底就返回handler信息，否则返回null
     */
    private BeanInfo<T> matchHandlerPlaceholder(String[] prefixes, int childIdx, UrlBeanTier<T> tier) {
        if (childIdx + 1 >= prefixes.length) {
            UrlBeanTier<T> child = tier.children.get(HandlerConstance.PLACE_HOLDER_REPLACER_STR);
            return (child.url != null && hasElement(child.handlers)) ? new BeanInfo<>(child.handlers.get(0), child.url) : null;
        } else {
            return matchHandler(prefixes, childIdx + 1, tier.children.get(HandlerConstance.PLACE_HOLDER_REPLACER_STR));
        }
    }

    /**
     * 用请求的url匹配filter
     *
     * @param url 请求的url
     * @return 对应的filter链表
     */
    public List<BeanInfo<T>> matchFilter(String url) {
        url = UrlTool.formatHandlerPath(url);
        return matchFilter(url.split("/"), 0, this);
    }

    /**
     * 用url匹配filter
     *
     * @param prefixes 前缀数组
     * @param childIdx 前缀树儿子要匹配的前缀下标
     * @param tier     前缀树
     * @return 对应的filter链表
     */
    private List<BeanInfo<T>> matchFilter(String[] prefixes, int childIdx, UrlBeanTier<T> tier) {
        LinkedList<BeanInfo<T>> result = new LinkedList<>();
        if (tier == null) {
            return result;
        }
        if (tier.children.containsKey(HandlerConstance.PLACE_HOLDER_REPLACER_STR)) {
            UrlBeanTier<T> child = tier.children.get(HandlerConstance.PLACE_HOLDER_REPLACER_STR);
            for (T handler : child.handlers) {
                result.add(new BeanInfo<>(handler, child.url));
            }
        }
        if (childIdx + 1 >= prefixes.length) {
            UrlBeanTier<T> child = tier.children.get(prefixes[childIdx]);
            if (child != null && !prefixes[childIdx].equals(HandlerConstance.PLACE_HOLDER_REPLACER_STR)) {
                for (T handler : child.handlers) {
                    result.add(new BeanInfo<>(handler, child.url));
                }
            }
        } else {
            result.addAll(matchFilter(prefixes, childIdx + 1, tier.children.get(prefixes[childIdx])));
        }
        return result;
    }

    /**
     * 判定一个链表是否有元素
     *
     * @param list 链表
     */
    private boolean hasElement(LinkedList list) {
        return (list != null && list.size() > 0);
    }

    /**
     * url对应的class信息
     *
     * @param <T> class类型
     */
    public static class BeanInfo<T> {
        private final T bean;
        private final String urlPattern;

        public BeanInfo(T bean, String urlPattern) {
            this.bean = bean;
            this.urlPattern = urlPattern;
        }

        public T getBean() {
            return bean;
        }

        public String getUrlPattern() {
            return urlPattern;
        }
    }
}
