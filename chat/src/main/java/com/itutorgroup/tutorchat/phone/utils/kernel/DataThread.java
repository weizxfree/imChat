package com.itutorgroup.tutorchat.phone.utils.kernel;

import android.content.Intent;
import android.text.TextUtils;

import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.APIConstant;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.event.ConversationEvent;
import com.itutorgroup.tutorchat.phone.domain.event.TcpStateChangeEvent;
import com.itutorgroup.tutorchat.phone.domain.request.TCPAuthRequest;
import com.itutorgroup.tutorchat.phone.receiver.MainGlobalReceiver;
import com.itutorgroup.tutorchat.phone.receiver.PushReceiver;
import com.itutorgroup.tutorchat.phone.utils.AppUtils;
import com.itutorgroup.tutorchat.phone.utils.EventBusManager;
import com.itutorgroup.tutorchat.phone.utils.StreamUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonUtil;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.network.Operation;
import com.itutorgroup.tutorchat.phone.utils.tcp.dispatcher.DataDispatcher;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by joyinzhao on 2016/10/21.
 */
public class DataThread extends Thread implements NettyClientHandler.IHandleStateListener, DataDispatcher.IDataListener {

    public static final String TAG = "tutor-chat-conn";
    private EventLoopGroup mWorkGroup;
    private SocketChannel mChannel;
    private NettyClientHandler.IHandleMessageListener mDataListener;

    private String mAESKey;

    public static boolean sRun = true;

    private static final int MAX_RETRY_TIME = 10;
    private int mRetryTime = 0;

    private static final Object mObject = new Object();

    public DataThread() {
        mDataListener = new DataListenerWrapper();
        sRun = true;
    }

    @Override
    public void run() {
        sRun = true;
        startNetty();
    }

    public void startNetty() {
        Observable.just(this)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<DataThread>() {
                    @Override
                    public void call(DataThread dataThread) {
                        if (!isTcpConnect()) {
                            init();
                        }
                    }
                }, CommonUtil.ACTION_EXCEPTION);
    }

    private synchronized void init() {
        try {
            if (isTcpConnect() || !AppUtils.hasNetwork()) {
                return;
            }
            EventBusManager.getInstance().post(TcpStateChangeEvent.getInstance());
            LogUtil.d(TAG, "pre start: " + sRun + (sRun && mRetryTime > 0 ? ", retry time = " + mRetryTime : ""));
            if (sRun && startUp()) {
                LogUtil.d(TAG, "startUp success");
                mIsTcpOnline = false;
                EventBusManager.getInstance().post(TcpStateChangeEvent.getInstance());
                if (AccountManager.getInstance().loadLoginData()) {
                    mIsTcpOnline = false;
                    LogUtil.d(TAG, "user data exists, connect and auth");
                    TCPAuthRequest request = new TCPAuthRequest();
                    request.init();
                    mAESKey = Constant.TCP_AES_LOGIN_KEY;
                    sendRequest(Operation.TCP_AUTH, request);
                }
            }
        } catch (Exception e) {
        }
    }

    private boolean startUp() throws InterruptedException {
        mWorkGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(mWorkGroup);
        bootstrap.remoteAddress(APIConstant.TCP_SERVER_HOST, APIConstant.TCP_SERVER_PORT);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(APIConstant.TCP_TIME_OUT_IDLE_READ, APIConstant.TCP_TIME_OUT_IDLE_WRITE, APIConstant.TCP_TIME_OUT_IDLE_ALL));
                //加密和解密。服务器和客户端需要统一，不然会报错。
                /*socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));*/
                socketChannel.pipeline().addLast(new NettyClientHandler(mDataListener, DataThread.this));
            }
        });
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(APIConstant.TCP_SERVER_HOST, APIConstant.TCP_SERVER_PORT)).sync();
            if (future.isSuccess()) {
                mChannel = (SocketChannel) future.channel();
                return true;
            } else {
                LogUtil.e(TAG, "Channel Future is not success.");
                restartNetty();
                return false;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "connect failed. wait to restart.");
            LogUtil.exception(e);
            restartNetty();
            return false;
        }
    }

    @Override
    public void restartNetty() {
        mIsTcpOnline = false;
        if (mRetryTime++ >= MAX_RETRY_TIME) {
            preShutDown(false);
            LogUtil.d(TAG, "over retry time, abort.");
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {

        }
        startNetty();
    }

    @Override
    public void sendHeartBeat() {
        String token = AccountManager.getInstance().getToken();
        if (TextUtils.isEmpty(token)) {
            LogUtil.d(TAG, "token is null, abort.");
        } else {
            LogUtil.d(TAG, "auto heart beat.");
            sendRequest(Operation.TCP_HEART_BEAT, null);
        }
    }

    @Override
    public String getAESKey(int operation) {
        switch (operation) {
            case Operation.TCP_AUTH:
                return Constant.TCP_AES_LOGIN_KEY;
            default:
                return mAESKey;
        }
    }

    @Override
    public void setAESKey(String key) {
        mAESKey = key;
    }

    private Runnable mWaitForOnlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsTcpOnline) {
                LogUtil.d(TAG, "wait 508 failed, shutdown and restart.");
                preShutDown(true);
            }
        }
    };

    private boolean mIsTcpOnline = false;

    public void resumeTcpIfDied() {
        if (!isTcpConnect()) {
            LogUtil.d(TAG, "resume tcp but it's died, restart.");
            mRetryTime = 0;
            sRun = true;
            startNetty();
        }
    }

    public boolean isTcpConnect() {
        return mChannel != null && mChannel.isActive();
    }

    public boolean isTcpOnline() {
        return mIsTcpOnline;
    }

    @Override
    public void waitForOnline(int operation) {
        LPApp.getInstance().mHandler.removeCallbacks(mWaitForOnlineRunnable);
        LPApp.getInstance().mHandler.postDelayed(mWaitForOnlineRunnable, 5000);

        if (operation == Operation.TCP_AUTH) {
            Intent intent = new Intent(MainGlobalReceiver.ACTION_TCP_ONLINE_STATE);
            LPApp.getInstance().sendBroadcast(intent);
        }
    }

    @Override
    public void online() {
        mIsTcpOnline = true;
        mRetryTime = 0;
        LPApp.getInstance().mHandler.removeCallbacks(mWaitForOnlineRunnable);
        Intent intent = new Intent(MainGlobalReceiver.ACTION_TCP_ONLINE_STATE);
        LPApp.getInstance().sendBroadcast(intent);
    }

    @Override
    public void kickOut() {
        LogUtil.d(TAG, "---------------kick out---------------");
        LPApp.getInstance().sendBroadcast(new Intent(MainGlobalReceiver.ACTION_KICK_OUT));
    }

    @Override
    public void updateConversationList() {
        if (isTcpOnline() && isTcpConnect()) {
            Intent intent = new Intent(LPApp.getInstance(), MainGlobalReceiver.class);
            intent.setAction(MainGlobalReceiver.ACTION_REFRESH_CONVERSATION_LIST);
            LPApp.getInstance().sendBroadcast(intent);
        }
    }

    @Override
    public void updateNotification(ArrayList<String> list) {
        // 不能判断tcp是否online，顶多判断connect，因为有离线消息。
        if (list != null && list.size() > 0) {
            Intent intent = new Intent(PushReceiver.CMD_MSG_REFRESH_PUSH_NOTIFICATION);
            intent.putStringArrayListExtra("target_id_list", list);
            LPApp.getInstance().sendBroadcast(intent);
        }
    }

    @Override
    public void onReceiveMessageModel(ArrayList<String> idList) {
        ConversationEvent event = new ConversationEvent(ConversationEvent.STATE_REFRESH);
        event.setRefreshIdList(idList);
        EventBusManager.getInstance().post(event);
    }

    @Override
    public void onPatches(int type, long timeSpan, String id) {
        Intent intent = new Intent(MainGlobalReceiver.ACTION_CMD_OBJECT_UPDATE);
        intent.putExtra("type", type);
        intent.putExtra("timeSpan", timeSpan);
        intent.putExtra("objectId", id);
        LPApp.getInstance().sendBroadcast(intent);
    }

    @Override
    public void preShutDown(boolean isRun) {
        sRun = isRun;
        if (mWorkGroup != null) {
            mWorkGroup.shutdownGracefully();
        }
        if (mChannel != null) {
            mChannel.close();
        }
    }

    public void sendMessage(String stringBase64) {
//        byte[] base64Bytes = Base64.decodeBase64(stringBase64.getBytes());
//        sendMessage(base64Bytes);
    }

    public void writeData(byte[] bytes) {
        if (mChannel != null && sRun) {
            ByteBuf bb = Unpooled.wrappedBuffer(bytes);
            mChannel.writeAndFlush(bb);
        }
    }

    @Override
    public void sendRequest(int operation, Object request) {
        if (request != null) {
            LogUtil.d(TAG, "request: " + request);
        }
        writeData(StreamUtils.buildTcpBytes(operation, request, 1, mAESKey));
    }

    class DataListenerWrapper implements NettyClientHandler.IHandleMessageListener {

        @Override
        public void onDataReceived(byte[] b) {
            byte[] opBytes = StreamUtils.subBytes(b, 8, 4);
            int operation = StreamUtils.bytesToInt(opBytes);
            int packageLen = StreamUtils.bytesToInt(StreamUtils.subBytes(b, 0, 4));
            LogUtil.d(TAG, "operation = " + operation + ", packageLen = " + packageLen + ", b.len = " + b.length);
            if (b.length >= 16) {
                DataDispatcher.getInstance().dispatch(operation, b, DataThread.this);
            }
        }
    }
}
