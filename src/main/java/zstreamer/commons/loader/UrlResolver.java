package zstreamer.commons.loader;

import org.springframework.stereotype.Component;
import zstreamer.commons.constance.HandlerConstance;
import zstreamer.commons.util.UrlTool;

import java.util.HashMap;

@Component
public class UrlResolver {
    public UrlResolver() {
    }

    public RestfulUrl resolveUrl(String url, String urlPattern) {
        url = UrlTool.formatSplitter(url);
        urlPattern = UrlTool.formatSplitter(urlPattern);
        RestfulUrl result = resolveTailParams(url);
        String[] rawPrefixes = result.url.split("/");
        String[] patternPrefixes = urlPattern.split("/");
        for (int i = 0; i < rawPrefixes.length; i++) {
            String prefix = patternPrefixes[i];
            if (prefix.length() < 2) {
                continue;
            }
            if (prefix.charAt(0) == HandlerConstance.PLACE_HOLDER_START && prefix.charAt(prefix.length() - 1) == HandlerConstance.PLACE_HOLDER_END) {
                String key = prefix.substring(1, prefix.length() - 1);
                result.params.put(key, rawPrefixes[i]);
            }
        }
        return result;
    }

    private RestfulUrl resolveTailParams(String url) {
        url = UrlTool.removeSpace(url);
        int paramStart = url.indexOf('?');
        String path = "";
        String paramStr = "";
        if (paramStart != -1) {
            path = url.substring(0, paramStart);
            paramStr = url.substring(paramStart + 1);
        } else {
            path = url;
        }
        return new RestfulUrl(path, resolveParams(paramStr));
    }

    private HashMap<String, String> resolveParams(String paramStr) {
        String[] splits = paramStr.split("&");
        HashMap<String, String> params = new HashMap<>(splits.length);
        for (String split : splits) {
            String[] kv = split.split("=");
            if (kv.length > 1) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    public static class RestfulUrl {
        private final String url;
        private final HashMap<String, String> params;

        public RestfulUrl(String url, HashMap<String, String> params) {
            this.url = url;
            this.params = params;
        }

        public String getUrl() {
            return url;
        }

        public String getParam(String key) {
            return params.get(key);
        }

        public HashMap<String, String> getParams() {
            return params;
        }
    }
}
