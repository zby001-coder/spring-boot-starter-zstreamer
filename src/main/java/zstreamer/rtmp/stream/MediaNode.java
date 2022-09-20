package zstreamer.rtmp.stream;

import zstreamer.rtmp.example.MemoryMediaNode;
import zstreamer.rtmp.message.messageType.media.DataMessage;
import zstreamer.rtmp.message.messageType.media.MediaMessage;

/**
 * @author 张贝易
 * @date 2022/9/20
 **/
public interface MediaNode {
    boolean hasNext();

    MediaNode getNext();

    void setNext(MemoryMediaNode next);

    MediaMessage getMessage();

    DataMessage getMetaData();

    MediaMessage getAacSequenceHeader();

    MediaMessage getAvcSequenceHeader();

    MediaMessage getSei();
}