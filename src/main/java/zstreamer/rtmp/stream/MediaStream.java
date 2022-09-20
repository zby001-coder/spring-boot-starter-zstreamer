package zstreamer.rtmp.stream;

import zstreamer.rtmp.message.messageType.media.DataMessage;
import zstreamer.rtmp.message.messageType.media.MediaMessage;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public interface MediaStream {
    void pushMessage(MediaMessage message, DataMessage metaData, MediaMessage aac, MediaMessage avc, MediaMessage sei);

    MediaNode pullMessage(int time);

    MediaNode getTail();
}
