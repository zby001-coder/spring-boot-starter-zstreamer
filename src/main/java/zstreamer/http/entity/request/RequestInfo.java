package zstreamer.http.entity.request;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import zstreamer.commons.loader.UrlBeanTier;
import zstreamer.http.filter.AbstractHttpFilter;
import zstreamer.http.handler.AbstractHttpHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 张贝易
 * 请求数据
 */
public class RequestInfo {
    /**
     * 请求中的参数
     */
    private final HashMap<String, Object> params = new HashMap<>();
    /**
     * 请求头
     */
    private final HttpHeaders headers;
    /**
     * 请求的url
     */
    private final String url;
    private final HttpMethod method;
    /**
     * 处理该请求的handler信息
     */
    private final UrlBeanTier.BeanInfo<AbstractHttpHandler> handlerInfo;
    /**
     * 处理该请求的filter信息
     */
    private final List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> filterInfo;

    public RequestInfo(HttpHeaders headers, String url, HttpMethod method,
                       UrlBeanTier.BeanInfo<AbstractHttpHandler> handlerInfo,
                       List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> filterInfo,
                       Map<String, ?> params) {
        this.headers = headers;
        this.url = url;
        this.method = method;
        this.handlerInfo = handlerInfo;
        this.filterInfo = filterInfo;
        if (params != null) {
            this.params.putAll(params);
        }
    }

    public RequestInfo(HttpHeaders headers, String url, HttpMethod method,
                       UrlBeanTier.BeanInfo<AbstractHttpHandler> handlerInfo,
                       List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> filterInfo) {
        this(headers, url, method, handlerInfo, filterInfo, null);
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public String getUrl() {
        return url;
    }

    public void setParam(String key, Object value) {
        params.put(key, value);
    }

    public void setParams(HashMap<String, ?> params) {
        this.params.putAll(params);
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public UrlBeanTier.BeanInfo<AbstractHttpHandler> getHandlerInfo() {
        return handlerInfo;
    }

    public List<UrlBeanTier.BeanInfo<AbstractHttpFilter>> getFilterInfo() {
        if (filterInfo != null) {
            return new ArrayList<>(filterInfo);
        }
        return new ArrayList<>();
    }
}
