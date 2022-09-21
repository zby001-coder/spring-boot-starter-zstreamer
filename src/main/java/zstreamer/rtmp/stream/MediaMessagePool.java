package zstreamer.rtmp.stream;

import zstreamer.rtmp.message.messageType.media.MediaMessage;

import java.util.Set;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public interface MediaMessagePool {
    void pushMediaMessage(String roomName, MediaMessage message, Streamer streamer);

    Set<String> getRoomNames();

    Streamer getStreamer(String roomName);

    boolean createRoom(String roomName, Streamer streamer);

    void closeRoom(String roomName);

    boolean hasRoom(String roomName);

    MediaNode pullMediaMessage(String roomName, int timeStamp) throws Exception;

    MediaNode pullTailMessage(String roomName) throws Exception;
}
