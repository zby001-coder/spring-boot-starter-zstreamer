package zstreamer.rtmp.example;

import zstreamer.rtmp.message.messageType.media.MediaMessage;
import zstreamer.rtmp.stream.MediaMessagePool;
import zstreamer.rtmp.stream.Streamer;

import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张贝易
 * 保存各个主播的媒体流
 */
public class MemoryMediaMessagePool implements MediaMessagePool {
    /**
     * 这里使用ConcurrentHashMap提升并发效率
     * key是房间id，value是它的流
     */
    private static final ConcurrentHashMap<String, MemoryMediaStream> POOL = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Streamer> STREAMER = new ConcurrentHashMap<>();

    /**
     * 由于只有一个主播可以推流，所以这里不用加锁
     *
     * @param roomName 主播的房间号(串流id)
     * @param message  媒体信息
     */
    @Override
    public void pushMediaMessage(String roomName, MediaMessage message, Streamer streamer) {
        POOL.get(roomName).pushMessage(message,
                streamer.getMetaData(),
                streamer.getAac(),
                streamer.getAvc(),
                streamer.getSei());
    }

    @Override
    public Set<String> getRoomNames() {
        return POOL.keySet();
    }

    @Override
    public Streamer getStreamer(String roomName) {
        return STREAMER.get(roomName);
    }

    @Override
    public void createRoom(String roomName, Streamer streamer) {
        POOL.put(roomName, new MemoryMediaStream());
        STREAMER.put(roomName, streamer);
    }

    @Override
    public void closeRoom(String roomName) {
        POOL.remove(roomName);
        Streamer streamer = STREAMER.get(roomName);
        if (streamer != null) {
            streamer.doCloseRoom();
        }
        STREAMER.remove(roomName);
    }

    @Override
    public boolean hasRoom(String roomName) {
        return POOL.containsKey(roomName);
    }

    @Override
    public MemoryMediaNode pullMediaMessage(String roomName, int timeStamp) throws Exception {
        if (hasRoom(roomName)) {
            MemoryMediaStream mediaStream = POOL.get(roomName);
            //这里用双重判定也是为了处理同名streamer互相顶掉的问题
            if (mediaStream != null) {
                return mediaStream.pullMessage(timeStamp);
            }
        }
        throw new Exception("Room Doesnt Exist!");
    }

    @Override
    public MemoryMediaNode pullTailMessage(String roomName) throws Exception {
        if (hasRoom(roomName)) {
            MemoryMediaStream mediaStream = POOL.get(roomName);
            //这里用双重判定也是为了处理同名streamer互相顶掉的问题
            if (mediaStream != null) {
                return mediaStream.getTail();
            }
        }
        throw new Exception("Room Doesnt Exist!");
    }
}
