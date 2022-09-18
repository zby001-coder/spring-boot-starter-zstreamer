package zstreamer.commons.annotation;

import org.springframework.context.annotation.Import;
import zstreamer.spring.config.ServerFactoryConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author 张贝易
 * @date 2022/9/18
 **/
@Retention(RetentionPolicy.RUNTIME)
@Import(ServerFactoryConfig.class)
public @interface EnableZstreamerServer {
}
