package zstreamer.commons.constance;

/**
 * @author 张贝易
 * @date 2022/9/18
 **/
public class ServerPropertyKeys {
    public static final String BOSS_COUNT = "zstreamer.boss.count";
    public static final String WORKER_COUNT = "zstreamer.worker.count";
    public static final String HTTP_PORT = "server.port";
    public static final String RTMP_PORT = "zstreamer.rtmp.port";
    public static final String HTTP_ADDRESS = "server.address";
    public static final String RTMP_ADDRESS = "zstreamer.rtmp.address";
    /**
     * 下面三个参数分别为需要自动注入的handler的包名、自动注入的filter的包名
     */
    public static final String HANDLER_PACKAGE = "zstreamer.handler.base-package";
    public static final String FILTER_PACKAGE = "zstreamer.filter.base-package";

    public static final String SSL_ENABLED = "zstreamer.ssl.enabled";

    /**
     * 下面三个参数为TrafficShaping：每秒写入byte、每秒写出byte、检测间隔。出入byte为0表示没有限制
     */
    public static final String BYTE_OUT_PER_SECOND = "zstreamer.traffic.byte-out-per-second";
    public static final String BYTE_IN_PER_SECOND = "zstreamer.traffic.byte-in-per-second";
    public static final String CHECK_INTERVAL = "zstreamer.traffic.check-interval";

    /**
     * 文件分片传输时的分片大小
     */
    public static final String FILE_CHUNK_SIZE = "zstreamer.file.chunk.size";
    /**
     * chunk类型的响应的重试最大次数和重试间隔和期望连续成功的chunk次数
     */
    public static final String CHUNK_RESPONSE_FAIL_MAX_TIME = "zstreamer.http.chunk.max-fail-time";
    public static final String MAX_CHUNK_RESPONSE_RETRY_INTERVAL = "zstreamer.http.chunk.max-retry-interval";
    public static final String MIN_CHUNK_RESPONSE_RETRY_INTERVAL = "zstreamer.http.chunk.min-retry-interval";
    public static final String INITIAL_CHUNK_RESPONSE_RETRY_INTERVAL = "zstreamer.http.chunk.initial-retry-interval";
    public static final String PREFER_SUCCESS_CHUNK = "zstreamer.http.chunk.prefer-success-chunk";
    public static final String FAST_FAIL_RETRY_INTERVAL = "zstreamer.http.chunk.fast-retry-interval";

    /**
     * http连接最大空闲时间，毫秒
     */
    public static final String CONNECTION_MAX_IDLE_TIME = "zstreamer.http.connection.max-idle-time";
}
