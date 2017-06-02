package com.itutorgroup.tutorchat.phone.utils.kernel;

import com.itutorgroup.tutorchat.phone.utils.StreamUtils;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;

import java.util.concurrent.TimeUnit;

import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by joyinzhao on 2016/10/20.
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = "NettyClientHandler";

    private IHandleMessageListener mMessageListener;
    private IHandleStateListener mStateListener;

    public NettyClientHandler(IHandleMessageListener listener, IHandleStateListener stateListener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null.");
        }
        mMessageListener = listener;
        mStateListener = stateListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof UnpooledHeapByteBuf) {
            UnpooledHeapByteBuf heapBuf = (UnpooledHeapByteBuf) msg;
            byte[] b = new byte[heapBuf.readableBytes()];
            heapBuf.readBytes(b);
//            byte[] opBytes = StreamUtils.subBytes(b, 8, 4);
//            int operation = StreamUtils.bytesToInt(opBytes);
//            mPackageLen = StreamUtils.bytesToInt(StreamUtils.subBytes(b, 0, 4));
//            LogUtil.v(TAG, "operation = " + operation + ", packageLen = " + mPackageLen + ", b.len = " + b.length);
            appendDataToBytes(b);
        }
    }

    private byte[] mBytes = null;

    private void dispatchData(byte[] b) {
        if (mMessageListener != null && b != null && b.length >= 16) {
            mMessageListener.onDataReceived(b);
        }
    }

    private synchronized void appendDataToBytes(byte[] b) {
        byte[] tmp = mBytes;
        if (tmp != null && tmp.length != 0) {
            mBytes = new byte[tmp.length + b.length];
            System.arraycopy(tmp, 0, mBytes, 0, tmp.length);
            System.arraycopy(b, 0, mBytes, tmp.length, b.length);
        } else {
            mBytes = b;
        }

        parseData();
    }

    private void parseData() {
        if (mBytes != null && mBytes.length >= 16) {
            int packageLen = StreamUtils.bytesToInt(StreamUtils.subBytes(mBytes, 0, 4));
            if (packageLen <= mBytes.length) {
                byte[] data = StreamUtils.subBytes(mBytes, 0, packageLen);
                dispatchData(data);
                mBytes = StreamUtils.subBytes(mBytes, packageLen, mBytes.length - packageLen);
                parseData();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        LogUtil.e(TAG, cause.getMessage());
        mStateListener.restartNetty();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        LogUtil.e(TAG, "channel inactive");
        mStateListener.restartNetty();
    }

    //利用写空闲发送心跳检测消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                case ALL_IDLE:
                    LogUtil.d(TAG, e.state() + ", restart.");
                    mStateListener.preShutDown(true);
                    break;
                case WRITER_IDLE:
                    mStateListener.sendHeartBeat();
                    break;
                default:
                    break;
            }
        }
    }

    public interface IHandleMessageListener {
        void onDataReceived(byte[] b);
    }

    public interface IHandleStateListener {
        void preShutDown(boolean isRun);

        void restartNetty();

        void sendHeartBeat();
    }
}
