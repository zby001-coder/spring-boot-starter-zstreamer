package zstreamer.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.AbstractConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.HttpHandler;
import zstreamer.ZstreamerServer;

/**
 * @author 张贝易
 * @date 2022/9/18
 **/

public class ServerFactoryConfig extends AbstractConfigurableWebServerFactory implements ReactiveWebServerFactory {
    @Autowired
    private Environment evn;

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        return new ZstreamerServer(evn);
    }
}
