package zstreamer.commons.constance;

/**
 * @author 张贝易
 * @date 2022/9/18
 **/
public class ServerPropertyDefault {
    public static final int BOSS_COUNT = 1;
    public static final int WORKER_COUNT = 8;
    public static final int HTTP_PORT = 8080;
    public static final int RTMP_PORT = 1935;
    /**
     * 下面三个参数分别为需要自动注入的handler的包名、自动注入的filter的包名
     */
    public static final String HANDLER_PACKAGE = "";
    public static final String FILTER_PACKAGE = "";

    public static final boolean SSL_ENABLED = false;

    /**
     * 下面三个参数为TrafficShaping：每秒写入byte、每秒写出byte、检测间隔。出入byte为0表示没有限制
     */
    public static final int BYTE_OUT_PER_SECOND = 0;
    public static final int BYTE_IN_PER_SECOND = 0;
    public static final int CHECK_INTERVAL = 1000;

    /**
     * 文件分片传输时的分片大小
     */
    public static final int FILE_CHUNK_SIZE = 8192;
    public static final int CHUNK_RESPONSE_FAIL_MAX_TIME = 10;
    public static final int CHUNK_RESPONSE_RETRY_INTERVAL = 10;

    /**
     * http连接最大空闲时间，毫秒
     */
    public static final long CONNECTION_MAX_IDLE_TIME = 30000;
}
