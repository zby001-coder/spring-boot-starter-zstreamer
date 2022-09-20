package zstreamer.rtmp.stream;

import zstreamer.rtmp.example.MemoryMediaNode;
import zstreamer.rtmp.message.messageType.media.MediaMessage;

import java.util.Enumeration;
import java.util.Set;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public interface MediaMessagePool {
    void pushMediaMessage(String roomName, MediaMessage message, Streamer streamer);

    Set<String> getRoomNames();

    Streamer getStreamer(String roomName);

    void createRoom(String roomName, Streamer streamer);

    void closeRoom(String roomName);

    boolean hasRoom(String roomName);

    MemoryMediaNode pullMediaMessage(String roomName, int timeStamp) throws Exception;

    MemoryMediaNode pullTailMessage(String roomName) throws Exception;
}
