package zstreamer.rtmp.example;

import zstreamer.rtmp.message.messageType.media.DataMessage;
import zstreamer.rtmp.message.messageType.media.MediaMessage;
import zstreamer.rtmp.stream.MediaStream;

/**
 * @author 张贝易
 * 媒体流，使用链表保存媒体流信息
 * 设置链表为固定大小，限制内存占用
 * 离开窗口的节点并不会释放next，这让已经拉到流的观众可以一直向下拉流
 * 当某个节点离开窗口，而且它和它之前的节点没有观众持有，那就会被回收
 * 这样的模型是为了不用加锁，而且不会内存泄露
 * 如果需要流量控制，可以在puller线程中记录一轮pull中最早的node，在streamer中记录最晚的node，当两个node差距过大时暂停推流
 */
public class MemoryMediaStream implements MediaStream {
    /**
     * head作为哑节点
     */
    private MemoryMediaNode head = new MemoryMediaNode(null, null, null, null);
    private MemoryMediaNode tail = head;
    private static final int WINDOW_SIZE = 30;
    private int presentSize = 0;

    /**
     * 由于只有一个主播推流，所以不用加锁
     *
     * @param message 要推的媒体信息
     */
    @Override
    public void pushMessage(MediaMessage message, DataMessage metaData, MediaMessage aac, MediaMessage avc, MediaMessage sei) {
        tail.setNext(new MemoryMediaNode(message, metaData, aac, avc, sei));
        tail = tail.getNext();
        if (presentSize == WINDOW_SIZE) {
            head = head.getNext();
            presentSize--;
        }
        presentSize++;
    }

    /**
     * 将当前窗口内最接近timeStamp的媒体节点返回
     * 由于观众拉流不需要精准的窗口，所以可以不用加锁，即在一个观众拉流的过程中，窗口是可以变化的
     *
     * @param time 期望的时间戳
     * @return 拉到的媒体节点
     */
    @Override
    public MemoryMediaNode pullMessage(int time) {
        if (presentSize == 0) {
            return head;
        }
        //获取当前窗口内部里大于等于time的第一个节点，如果没有就返回tail
        MemoryMediaNode now = head;
        MemoryMediaNode end = tail;
        while (now != end) {
            now = now.getNext();
            if (now.getMessage().getTimeStamp() >= time) {
                break;
            }
        }
        return now;
    }

    @Override
    public MemoryMediaNode getTail() {
        return tail;
    }
}