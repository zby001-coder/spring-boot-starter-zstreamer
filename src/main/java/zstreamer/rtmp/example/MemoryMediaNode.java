package zstreamer.rtmp.example;

import zstreamer.rtmp.message.messageType.media.DataMessage;
import zstreamer.rtmp.message.messageType.media.MediaMessage;
import zstreamer.rtmp.stream.MediaNode;

/**
 * @author 张贝易
 * 媒体节点，内部保存着媒体信息
 * 整个媒体流使用链表，每个节点保存后一个节点
 * 一旦拉到了某个节点，就可以一直往后拉
 */
public class MemoryMediaNode implements MediaNode {
    private MemoryMediaNode next;
    /**
     * 当前tag的信息
     */
    private final MediaMessage message;
    /**
     * 整个流的元信息
     */
    private final DataMessage metaData;

    /**
     * 音频编解码元信息
     */
    private final MediaMessage aacSequenceHeader;
    /**
     * 视频编解码元信息
     */
    private final MediaMessage avcSequenceHeader;

    /**
     * 视频编码辅助信息，它不是必须的
     */
    private final MediaMessage sei;

    public MemoryMediaNode(DataMessage dataMessage, MediaMessage aac, MediaMessage avc, MediaMessage sei) {
        this.message = null;
        this.metaData = dataMessage;
        this.aacSequenceHeader = aac;
        this.avcSequenceHeader = avc;
        this.sei = sei;
    }

    public MemoryMediaNode(MediaMessage message, DataMessage dataMessage, MediaMessage aac, MediaMessage avc, MediaMessage sei) {
        this.metaData = dataMessage;
        this.message = message;
        this.aacSequenceHeader = aac;
        this.avcSequenceHeader = avc;
        this.sei = sei;
    }

    public MemoryMediaNode(MemoryMediaNode next, MediaMessage message, DataMessage dataMessage, MediaMessage aac, MediaMessage avc, MediaMessage sei) {
        this.metaData = dataMessage;
        this.next = next;
        this.message = message;
        this.aacSequenceHeader = aac;
        this.avcSequenceHeader = avc;
        this.sei = sei;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public MemoryMediaNode getNext() {
        return next;
    }

    @Override
    public void setNext(MemoryMediaNode next) {
        this.next = next;
    }

    @Override
    public MediaMessage getMessage() {
        return message;
    }

    @Override
    public DataMessage getMetaData() {
        return metaData;
    }

    @Override
    public MediaMessage getAacSequenceHeader() {
        return aacSequenceHeader;
    }

    @Override
    public MediaMessage getAvcSequenceHeader() {
        return avcSequenceHeader;
    }

    @Override
    public MediaMessage getSei() {
        return sei;
    }
}