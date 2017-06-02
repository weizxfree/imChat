package com.itutorgroup.tutorchat.phone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.ChatDetailActivity;
import com.itutorgroup.tutorchat.phone.activity.chat.MessageRecipientsListActivity;
import com.itutorgroup.tutorchat.phone.activity.image.ImagePreviewActivity;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.db.dao.MessageDao;
import com.itutorgroup.tutorchat.phone.domain.db.model.MessageModel;
import com.itutorgroup.tutorchat.phone.domain.db.model.UserInfo;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageStatus;
import com.itutorgroup.tutorchat.phone.domain.inter.MessageType;
import com.itutorgroup.tutorchat.phone.domain.inter.OnHeadImgLongClickListener;
import com.itutorgroup.tutorchat.phone.domain.inter.OnRetrySendClickLintener;
import com.itutorgroup.tutorchat.phone.ui.MessageStatusView;
import com.itutorgroup.tutorchat.phone.utils.CompressImg;
import com.itutorgroup.tutorchat.phone.utils.FileUtils;
import com.itutorgroup.tutorchat.phone.utils.PixelUtil;
import com.itutorgroup.tutorchat.phone.utils.TimeUtils;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.common.LogUtil;
import com.itutorgroup.tutorchat.phone.utils.manager.AccountManager;
import com.itutorgroup.tutorchat.phone.utils.manager.AudioSensorManager;
import com.itutorgroup.tutorchat.phone.utils.manager.CacheManager;
import com.itutorgroup.tutorchat.phone.utils.manager.FileManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.MyClipboardManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserInfoManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ChatImageTransformation;
import com.itutorgroup.tutorchat.phone.utils.ui.ChatUIUtils;
import com.itutorgroup.tutorchat.phone.utils.ui.ListDialogHelper;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;
import com.itutorgroup.tutorchat.phone.utils.ui.UserInfoHelper;
import com.itutorgroup.tutorchat.phone.utils.voice.MediaManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.salesuite.saf.adapter.SAFAdapter;
import cn.salesuite.saf.utils.Preconditions;
import cn.salesuite.saf.utils.StringUtils;

public class ChatMsgAdapter extends SAFAdapter<MessageModel> {

    public static final int MESSAGE_DIRECT_RECEIVE = 1;
    public static final int MESSAGE_DIRECT_SEND = 0;
    public static final int TYPE_RECEIVE_TEXT = 0;
    public static final int TYPE_RECEIVE_IMAGE = 1;
    public static final int TYPE_RECEIVE_VOICE = 2;
    public static final int TYPE_SEND_TEXT = 3;
    public static final int TYPE_SEND_IMAGE = 4;
    public static final int TYPE_SEND_VOICE = 5;
    private boolean isSingleChat, isServiceAccount;
    private LayoutInflater mInflater;
    private Context mContext;
    public OnHeadImgLongClickListener mHeadImgLongClickListener;
    private Map<String, UserInfo> userInfoMap;
    private Map<String, String> systemContentMap;
    public OnRetrySendClickLintener mRetrySendClickLintener;
    private TreeMap<Long, String> treeMap;
    private ImageView voiceAnimView;
    private boolean upReturn;


    public ChatMsgAdapter(Context context, List<MessageModel> list) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mRetrySendClickLintener = (OnRetrySendClickLintener) context;
        mHeadImgLongClickListener = (OnHeadImgLongClickListener) context;
        userInfoMap = new HashMap<>();
        systemContentMap = new HashMap<>();
        mList = list;
    }

    public ChatMsgAdapter(Context context, List<MessageModel> list, boolean isSingleChat) {
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
        mRetrySendClickLintener = (OnRetrySendClickLintener) context;
        userInfoMap = new HashMap<>();
        systemContentMap = new HashMap<>();
        mList = list;
        this.isSingleChat = isSingleChat;
    }


    public void addMsgListToTop(List<MessageModel> listMessages) {
        if (Preconditions.isBlank(listMessages))
            return;
        mList.addAll(0, listMessages);
        notifyDataSetChanged();
    }


    public void addMsgToBottom(MessageModel msg) {
        if (StringUtils.isBlank(msg))
            return;
        mList.add(msg);
        notifyDataSetChanged();
    }

    public void addMsgListToBottom(List<MessageModel> listMessages) {
        if (Preconditions.isBlank(listMessages))
            return;
        mList.addAll(listMessages);
        if (Preconditions.isNotBlank(mList) && mList.size() >= 2) {
            if (mList.get(mList.size() - 1).CreateTime < mList.get(mList.size() - 2).CreateTime) {
                Collections.swap(mList, mList.size() - 2, mList.size() - 1);
            }
        }
        notifyDataSetChanged();
    }

    private int getDirect(MessageModel message) {
        /**
         *
         * 发送者是自己
         */
        if (1 == message.IsSelf) {
            return MESSAGE_DIRECT_SEND;
        } else {
            /**
             * 别人发送的
             */
            return MESSAGE_DIRECT_RECEIVE;
        }
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = (MessageModel) getItem(position);
        if (message.Type == MessageType.TEXT) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_TEXT
                    : TYPE_SEND_TEXT;
        }
        if (message.Type == MessageType.PIC) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_IMAGE
                    : TYPE_SEND_IMAGE;

        }
        if (message.Type == MessageType.VOICE) {
            return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_VOICE
                    : TYPE_SEND_VOICE;
        }

        return -1;
    }

    private View.OnClickListener mOnUnreadTipClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MessageModel message = (MessageModel) v.getTag(R.id.tag_bean);
            mContext.startActivity(new Intent(mContext, MessageRecipientsListActivity.class).putExtra("message", message));
        }
    };

    public int getViewTypeCount() {
        return 6;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final MessageModel message = (MessageModel) getItem(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = createViewByMessage(message, position);

            switch (message.Type) {
                case MessageType.TEXT:
                case MessageType.GROUPANNOUNCEMENT:
                    viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    viewHolder.mNoUnderlineSpan = new NoUnderlineSpan();
                    ChatUIUtils.setMaxWidth(viewHolder.tvContent, mContext);
                    break;
                case MessageType.PIC:
                    viewHolder.mChatImgView = (ImageView) convertView.findViewById(R.id.chat_img);
                    break;
                case MessageType.VOICE:
                    viewHolder.voiceTime = (TextView) convertView.findViewById(R.id.voiceTime);
                    viewHolder.voiceView = (FrameLayout) convertView.findViewById(R.id.voiceView);
                    viewHolder.voiceAnim = (ImageView) convertView.findViewById(R.id.voiceAnim);
                    if (!isSend(message)) {
                        viewHolder.readStatus = (TextView) convertView.findViewById(R.id.readStatus);
                    }
                    break;
            }

            if (!isSend(message) && MessageTypeIsNotSystem(message) && !isSingleChat) {
                viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.tv_name.setVisibility(View.VISIBLE);
                viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.tv_title.setVisibility(View.VISIBLE);
            }

            if (MessageTypeIsNotSystem(message)) {
                viewHolder.messageStatusView = (MessageStatusView) convertView.findViewById(R.id.messageStatusView);
                viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
                viewHolder.headImage = (ImageView) convertView.findViewById(R.id.headImage);
                if (viewHolder.headImage != null) {
                    viewHolder.headImage.setTag(R.id.tag_default, R.drawable.head_personal_blue);
                }
            } else {
                viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
                viewHolder.tv_prompt = (TextView) convertView.findViewById(R.id.tv_prompt);
                ChatUIUtils.setMaxWidth(viewHolder.tv_prompt, mContext);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ViewHolder myViewHolder = viewHolder;
        switch (message.Type) {
            // 根据消息type显示item
            case MessageType.TEXT:
            case MessageType.GROUPANNOUNCEMENT:
                if (message.Type == MessageType.GROUPANNOUNCEMENT && StringUtils.isNotEmpty(message.Content)) {
                    viewHolder.tvContent.setText(LPApp.getInstance().getString(R.string.msg_content_alt_all, message.Content));
                    if (!isSend(message) && !isSingleChat) {
                        try {
                            boolean GroupAnnouncementIsReadMessage = MessageDao.getInstance().CheckGroupAnnouncementIsReadMessage(message);
                            if (GroupAnnouncementIsReadMessage) {
                                myViewHolder.messageStatusView.setBackgroudNull();
                            } else {
                                myViewHolder.messageStatusView.setBackgroundRed();
                                viewHolder.tvContent.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myViewHolder.messageStatusView.setBackgroudNull();
                                        myViewHolder.tvContent.setEnabled(false);
                                        MessageManager.getInstance().setGroupAnnouncementIsReadFromReceiverID(message);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (StringUtils.isNotEmpty(message.Content)) {
                    if (isSend(message)) {
                        MessageManager.getInstance().loadEmojiText(mContext, viewHolder.tvContent, message.Content, 80);
                    } else {
                        MessageManager.getInstance().loadEmojiText(mContext, viewHolder.tvContent, message.Content, 80, viewHolder.mNoUnderlineSpan);
                    }
                }
                viewHolder.tvContent.setTag(R.id.tag_bean, message);
                viewHolder.tvContent.setOnTouchListener(onTouchListener);
                viewHolder.tvContent.setOnLongClickListener(longClickListener);
                break;

            case MessageType.PIC:
                viewHolder.messageStatusView.setViewByMessageStatus(message.MessageSendStatus);
                viewHolder.mChatImgView.setTag(R.id.tag_bean, message);
                viewHolder.mChatImgView.setOnLongClickListener(longClickListener);
                final ViewHolder picViewHolder = viewHolder;
                final String path = FileManager.getInstance().getPathByFileId(message.Content);
                File tmpfile = new File(path);
                if (!tmpfile.exists()) {
                    if (!isSend(message)) {
                        Glide.with(mContext).load(R.drawable.ic_launcher).asBitmap().transform(new ChatImageTransformation(mContext, isSend(message) ? 0 : 1)).listener(new RequestListener<Integer, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Integer model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Integer model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                downLoadPic(picViewHolder, message);
                                return false;
                            }
                        }).into(viewHolder.mChatImgView);
                    }
                } else {
                    initImage(viewHolder, message, path);
                }
                break;

            case MessageType.VOICE:

                File voiceTmp = new File(FileManager.getInstance().getVoicePathByFileId(message.Content));
                if (!voiceTmp.exists() || message.VoiceTime == 0.0f) {
                    downloadVoice(viewHolder, message);
                } else {
                    initVoice(myViewHolder, message);
                }
                break;
            case MessageType.SYSTEM_MESSAGE:
                String content = systemContentMap.get(message.MessageID);
                if (StringUtils.isNotEmpty(content)) {
                    myViewHolder.tv_prompt.setVisibility(View.VISIBLE);
                    myViewHolder.tv_prompt.setText(content);
                } else {
                    MessageManager.getInstance().getSystemMessage(message, new CommonLoadingListener<String>() {
                        @Override
                        public void onResponse(final String s) {
                            systemContentMap.put(message.MessageID, s);
                            LPApp.getInstance().mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    myViewHolder.tv_prompt.setVisibility(View.VISIBLE);
                                    myViewHolder.tv_prompt.setText(s);
                                }
                            });

                        }
                    });
                }
                break;

            case MessageType.WITH_DRAWAL:
                viewHolder.tv_prompt.setVisibility(View.VISIBLE);
                String cache = CacheManager.getInst().getString(message.Content);
                if (!TextUtils.isEmpty(cache)) {
                    myViewHolder.tv_prompt.setText(cache);
                }
                MessageManager.getInstance().getWithDrawalMessageContent(message, new CommonLoadingListener<String>() {
                    @Override
                    public void onResponse(String s) {
                        myViewHolder.tv_prompt.setText(s);
                        CacheManager.getInst().save(message.Content, s);
                    }
                });
                break;

        }
        ChatUIUtils.showTime(viewHolder.tvSendTime, message, mList, position);
        if (MessageTypeIsNotSystem(message) && isSend(message)) {
            viewHolder.headImage.setImageResource(R.drawable.head_personal_blue);
            UserInfoHelper.showAvatar(AccountManager.getInstance().getCurrentUser(), viewHolder.headImage);
            viewHolder.headImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, ChatDetailActivity.class).putExtra("user_info", AccountManager.getInstance().getCurrentUser()));
                }
            });
            viewHolder.messageStatusView.setViewByMessageStatus(message.MessageSendStatus);
            if (MessageStatus.MESSAGE_SNED_ERROE == message.MessageSendStatus && message.Type != MessageType.GROUPANNOUNCEMENT) {
                viewHolder.messageStatusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MessageStatusView) v).setViewByMessageStatus(MessageStatus.MESSAGE_SNEDING);
                        if (mRetrySendClickLintener != null) {
                            mRetrySendClickLintener.retrySend(message);
                        }
                    }
                });
            } else if (message.Type == MessageType.GROUPANNOUNCEMENT) {
                viewHolder.messageStatusView.setViewByMessageStatus(MessageStatus.MESSAGE_SEND_OK);
            }
        } else if (MessageTypeIsNotSystem(message) && !isSend(message)) {
            final UserInfo userInfo = userInfoMap.get(message.PosterID);
            if (StringUtils.isBlank(userInfo)) {
                UserInfoManager.getInstance().getUserInfo(message.PosterID, new CommonLoadingListener<UserInfo>() {
                    @Override
                    public void onResponse(final UserInfo userInfoResponse) {
                        if (StringUtils.isNotBlank(userInfoResponse)) {
                            userInfoMap.put(message.PosterID, userInfoResponse);
                        }
                        setUserInfo2Ui(myViewHolder, userInfoResponse);
                    }
                });
            } else {
                setUserInfo2Ui(myViewHolder, userInfo);
            }

            if (message.Type != MessageType.GROUPANNOUNCEMENT) {
                viewHolder.messageStatusView.setTag(R.id.tag_holder, viewHolder);
                viewHolder.messageStatusView.setTag(R.id.tag_bean, message);
                viewHolder.messageStatusView.setOnClickListener(ReceiverMessageRetrySendOnClickListener);
            }

        }
        return convertView;
    }

    private View createViewByMessage(MessageModel message, int position) {
        switch (message.Type) {
            case MessageType.TEXT:
            case MessageType.GROUPANNOUNCEMENT:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? mInflater
                        .inflate(R.layout.chatting_item_msg_text_left, null)
                        : mInflater.inflate(R.layout.chatting_item_msg_text_right, null);

            case MessageType.PIC:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? mInflater
                        .inflate(R.layout.chatting_item_msg_pic_left, null)
                        : mInflater.inflate(R.layout.chatting_item_msg_pic_right, null);

            case MessageType.VOICE:
                return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? mInflater
                        .inflate(R.layout.chatting_item_msg_voice_left, null)
                        : mInflater.inflate(R.layout.chatting_item_msg_voice_right, null);

            case MessageType.WITH_DRAWAL:
            case MessageType.SYSTEM_MESSAGE:
                return mInflater.inflate(R.layout.chatting_item_prompt, null);
        }
        return null;
    }


    class ViewHolder {
        public TextView tvSendTime;
        public TextView tvContent;
        public TextView voiceTime;
        public TextView tv_name;
        public FrameLayout voiceView;
        public ImageView mChatImgView;
        public MessageStatusView messageStatusView;
        public ImageView headImage;
        public ImageView voiceAnim;
        public TextView readStatus;
        public TextView tv_title;
        public TextView tv_prompt;
        public NoUnderlineSpan mNoUnderlineSpan;

    }


    private void setUserInfo2Ui(ViewHolder viewHolder, final UserInfo userInfo) {
        if (!isSingleChat) {
            viewHolder.tv_name.setText(userInfo.Name);
            viewHolder.tv_title.setText(userInfo.Title);
        }
        viewHolder.headImage.setImageResource(R.drawable.head_personal_blue);
        UserInfoHelper.showAvatar(userInfo.Image, viewHolder.headImage);
        viewHolder.headImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, ChatDetailActivity.class).putExtra("user_info", userInfo));
            }
        });
        viewHolder.headImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mHeadImgLongClickListener != null) {
                    mHeadImgLongClickListener.headImgLongClick(userInfo);
                }
                return true;
            }
        });
    }

    private void setLayoutParams(String path, ImageView imageView) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int rate = CompressImg.getInSampleRate(path);
        options.inJustDecodeBounds = false;
        int displayWidth = options.outWidth / rate;
        int displayHeight = options.outHeight / rate;
        if (displayWidth > PixelUtil.dp2px(200)) {
            displayWidth = PixelUtil.dp2px(200);
        }
        if (displayHeight > PixelUtil.dp2px(250)) {
            displayHeight = PixelUtil.dp2px(250);
        }
        imageView.getLayoutParams().width = displayWidth;
        imageView.getLayoutParams().height = displayHeight;
    }

    View.OnClickListener ImagePreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MessageModel messageModel = (MessageModel) v.getTag(R.id.tag_bean);
            ArrayList pathList = new ArrayList();
            for (String path : treeMap.values()) {
                pathList.add(path);
            }
            ImagePreviewActivity.startNewActivity((Activity) mContext, v, pathList, FileManager.getInstance().getPathByFileId(messageModel.Content));
        }
    };


    private void initImage(final ViewHolder tmpHolder, final MessageModel message, String path) {
        setLayoutParams(path, tmpHolder.mChatImgView);
        if (treeMap == null) {
            treeMap = new TreeMap<>(new Comparator<Long>() {
                @Override
                public int compare(Long lhs, Long rhs) {
                    return (int) (lhs - rhs);
                }
            });
        }
        treeMap.put(message.CreateTime, path);
        Glide.with(mContext).load(path).asBitmap().transform(new ChatImageTransformation(mContext, isSend(message) ? 0 : 1)).listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                LogUtil.exception(e);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (!isSend(message)) {
                    tmpHolder.messageStatusView.setViewByMessageStatus(MessageStatus.MESSAGE_SEND_OK);
                    message.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                    MessageDao.getInstance().updateMessageSendStatus(message.MessageID, MessageStatus.MESSAGE_SEND_OK);
                }
                return false;
            }
        }).into(tmpHolder.mChatImgView);

        tmpHolder.mChatImgView.setTag(R.id.tag_bean, message);
        tmpHolder.mChatImgView.setOnClickListener(ImagePreviewClickListener);
    }


    private void initVoice(final ViewHolder tmpHolder, final MessageModel message) {

        final boolean isSend = (MESSAGE_DIRECT_SEND == getDirect(message));
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        ViewGroup.LayoutParams lp = tmpHolder.voiceView.getLayoutParams();
        lp.width = (int) (PixelUtil.dp2px(100) + ((width / 3) / 60f) * Math.rint(message.VoiceTime));
        tmpHolder.voiceTime.setText((int) Math.ceil(message.VoiceTime) + "\"");

        if (!isSend) {
            if (message.GroupAnnouncementIsRead == 1) {
                tmpHolder.readStatus.setVisibility(View.GONE);
            } else {
                tmpHolder.readStatus.setVisibility(View.VISIBLE);
            }
            tmpHolder.messageStatusView.setViewByMessageStatus(MessageStatus.MESSAGE_SEND_OK);
            message.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
            MessageDao.getInstance().updateMessageSendStatus(message.MessageID, MessageStatus.MESSAGE_SEND_OK);
        }


        tmpHolder.voiceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MediaManager.isPlaying() && voiceAnimView != null && voiceAnimView.getTag(R.id.tag_holder) == message.MessageID) {
                    if (isSend) {
                        voiceAnimView.setBackgroundResource(R.drawable.v_anim_default);
                    } else {
                        voiceAnimView.setBackgroundResource(R.drawable.voice_left_anim3);
                    }
                    voiceAnimView = null;
                    MediaManager.reset();
                    AudioSensorManager.getInstance().unRegister();
                    return;
                }
                if (!isSend && message.GroupAnnouncementIsRead == 0) {
                    MessageDao.getInstance().UpdateVoiceReadStatus(message.MessageID);
                }
                AudioSensorManager.getInstance().register();
                if (voiceAnimView != null) {
                    if ((int) voiceAnimView.getTag(R.id.tag_default) == 1) {
                        voiceAnimView.setBackgroundResource(R.drawable.v_anim_default);
                    } else {
                        voiceAnimView.setBackgroundResource(R.drawable.voice_left_anim3);
                    }
                    voiceAnimView = null;
                }
                voiceAnimView = tmpHolder.voiceAnim;
                voiceAnimView.setTag(R.id.tag_holder, message.MessageID);
                if (isSend) {
                    voiceAnimView.setTag(R.id.tag_default, 1);
                    voiceAnimView.setBackgroundResource(R.drawable.play_right_anim);
                } else {
                    voiceAnimView.setTag(R.id.tag_default, 0);
                    message.GroupAnnouncementIsRead = 1;
                    tmpHolder.readStatus.setVisibility(View.GONE);
                    voiceAnimView.setBackgroundResource(R.drawable.play_left_anim);
                }
                AnimationDrawable animation = (AnimationDrawable) tmpHolder.voiceAnim.getBackground();
                animation.start();
                MediaManager.playSound(FileManager.getInstance().getVoicePathByFileId(message.Content), new MediaPlayer.OnCompletionListener() {

                    public void onCompletion(MediaPlayer mp) {
                        AudioSensorManager.getInstance().unRegister();
                        if (isSend) {
                            voiceAnimView.setBackgroundResource(R.drawable.v_anim_default);
                        } else {
                            voiceAnimView.setBackgroundResource(R.drawable.voice_left_anim3);
                        }
                    }

                });
            }
        });
        tmpHolder.voiceView.setTag(R.id.tag_bean, message);
        tmpHolder.voiceView.setOnLongClickListener(longClickListener);

    }


    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return upReturn;
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                upReturn = false;
            }
            return false;
        }
    };

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
            upReturn = true;
            final MessageModel item = (MessageModel) v.getTag(R.id.tag_bean);
            final List<Runnable> actionList = new ArrayList<>();
            List<String> items = new ArrayList<>();
            items.add(mContext.getString(R.string.msg_operation_delete));
            actionList.add(new Runnable() {
                @Override
                public void run() {
                    Iterator it = mList.iterator();
                    while (it.hasNext()) {
                        MessageModel messageModel1 = (MessageModel) it.next();
                        if (messageModel1.MessageID.equals(item.MessageID)) {
                            it.remove();
                            notifyDataSetChanged();
                            if (messageModel1.Type == MessageType.VOICE) {
                                AudioSensorManager.getInstance().unRegister();
                            }
                            MessageManager.getInstance().removeMessageById(item.MessageID);
                        }
                    }
                }
            });

            if (isSend(item) && !isServiceAccount) {
                items.add(mContext.getString(R.string.msg_operation_cancel));
                actionList.add(new Runnable() {
                    @Override
                    public void run() {
                        withDrawalMessage(item);
                    }
                });
            }
            if (isHavePermissionAccessRead(item)) {
                items.add(mContext.getString(R.string.msg_operation_read));
                actionList.add(new Runnable() {
                    @Override
                    public void run() {
                        mContext.startActivity(new Intent(mContext, MessageRecipientsListActivity.class).putExtra("message", item));
                    }
                });
            }
            switch (v.getId()) {
                case R.id.tv_chatcontent:
                    items.add(mContext.getString(R.string.msg_operation_copy));
                    actionList.add(new Runnable() {
                        @Override
                        public void run() {
                            MyClipboardManager.copyToClipboard(mContext, ((TextView) v).getText().toString());
//                            AppUtils.Paste2Clipboard(((TextView) v).getText().toString());
                            ToastUtil.show(R.string.copy_clipboard_done);
                        }
                    });
                    break;

                case R.id.chat_img:
                    items.add(mContext.getString(R.string.msg_operation_pic_save));
                    actionList.add(new Runnable() {
                        @Override
                        public void run() {
                            final String path = FileManager.getInstance().getPathByFileId(item.Content);
                            String newName = System.currentTimeMillis() + ".jpg";
                            FileUtils.copyFile(path, FileUtils.getSavePath() + newName);
                            FileUtils.saveImageToGallery(mContext, FileUtils.getSavePath(), newName);
                            ToastUtil.show(mContext.getString(R.string.common_successful_file_copy, FileUtils.getSavePath()));
                        }
                    });
                    break;
                case R.id.voiceView:
                    MessageModel messageModel = (MessageModel) v.getTag(R.id.tag_bean);
                    if (messageModel.MessageID.equals(item.getMessageID()) && MediaManager.isPlaying()) {
                        MediaManager.destroy();
                        stopVoiceAnim();
                    }
                    final AudioManager audioManager = (AudioManager) LPApp.getInstance().getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                        items.add(LPApp.getInstance().getString(R.string.msg_operation_voice_normal));
                    } else if (audioManager.getMode() == AudioManager.MODE_NORMAL) {
                        items.add(LPApp.getInstance().getString(R.string.msg_operation_voice_mode_incall));
                    }
                    actionList.add(new Runnable() {
                        @Override
                        public void run() {
                            if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                                audioManager.setMode(AudioManager.MODE_NORMAL);
                                audioManager.setSpeakerphoneOn(true);
                                ToastUtil.show(mContext.getString(R.string.msg_operation_voice_normal));
                            } else if (audioManager.getMode() == AudioManager.MODE_NORMAL) {
                                audioManager.setSpeakerphoneOn(false);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                } else {
                                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                                }
                                ToastUtil.show(mContext.getString(R.string.msg_operation_voice_mode_incall));
                            }
                        }
                    });
                    break;
            }

            new ListDialogHelper(mContext, items, actionList).show();
            return true;
        }
    };


    private void withDrawalMessage(MessageModel item) {
        if (!TimeUtils.isIn2minute(System.currentTimeMillis(), item.CreateTime)) {
            new AlertDialog.Builder(mContext, R.style.MyAlertDialogStyle)
                    .setMessage(R.string.msg_over_time_info)
                    .setPositiveButton(R.string.dialog_message_ok, null)
                    .show();
            return;
        }
        if (mRetrySendClickLintener != null) {
            mRetrySendClickLintener.cancelSend(item);
        }
    }


    private boolean MessageTypeIsNotSystem(MessageModel messageModel) {
        return messageModel.Type == MessageType.TEXT || messageModel.Type == MessageType.PIC || messageModel.Type == MessageType.VOICE || messageModel.Type == MessageType.GROUPANNOUNCEMENT;
    }


    private boolean isSend(MessageModel messageModel) {
        return messageModel.IsSelf == 1 ? true : false;
    }


    private boolean isHavePermissionAccessRead(MessageModel messageModel) {
        return messageModel.IsHavePermissionAccessReadStatus == 1 ? true : false;
    }


    private void downLoadPic(final ViewHolder viewHolder, final MessageModel messageModel) {

        FileManager.getInstance().downloadImageFile(messageModel.Content, new CommonLoadingListener<Boolean>() {
            @Override
            public void onResponse(Boolean o) {
                if (o) {
                    String path = FileManager.getInstance().getPathByFileId(messageModel.Content);
                    initImage(viewHolder, messageModel, path);
                } else {
                    messageModel.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE;
                    viewHolder.messageStatusView.setViewByMessageStatus(MessageStatus.MESSAGE_SNED_ERROE);
                    MessageDao.getInstance().updateMessageSendStatus(messageModel.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
                }

            }
        });
    }

    private void downloadVoice(final ViewHolder viewHolder, final MessageModel message) {
        FileManager.getInstance().downloadVoiceFile(message.Content, new CommonLoadingListener<Integer>() {
            @Override
            public void onResponse(Integer integer) {
                if (integer != -1) {
                    message.VoiceTime = integer;
                    initVoice(viewHolder, message);
                    MessageDao.getInstance().UpdateVoiceTime(integer, message.MessageID);
                } else {
                    message.MessageSendStatus = MessageStatus.MESSAGE_SNED_ERROE;
                    viewHolder.messageStatusView.setViewByMessageStatus(MessageStatus.MESSAGE_SNED_ERROE);
                    MessageDao.getInstance().updateMessageSendStatus(message.MessageID, MessageStatus.MESSAGE_SNED_ERROE);
                }
            }
        });
    }


    View.OnClickListener ReceiverMessageRetrySendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MessageStatusView) v).setViewByMessageStatus(MessageStatus.MESSAGE_SNEDING);
            MessageModel messageModel = (MessageModel) v.getTag(R.id.tag_bean);
            ViewHolder viewHolder = (ViewHolder) v.getTag(R.id.tag_holder);
            switch (messageModel.Type) {
                case MessageType.PIC:
                    downLoadPic(viewHolder, messageModel);
                    break;
                case MessageType.VOICE:
                    downloadVoice(viewHolder, messageModel);
                    break;
            }
        }
    };

    public void stopVoiceAnim() {
        if (voiceAnimView != null) {
            if ((int) voiceAnimView.getTag(R.id.tag_default) == 1) {
                voiceAnimView.setBackgroundResource(R.drawable.v_anim_default);
            } else {
                voiceAnimView.setBackgroundResource(R.drawable.voice_left_anim3);
            }
            voiceAnimView = null;
        }
    }

    public void setIsServiceAccount(boolean isServiceAccount) {
        this.isServiceAccount = isServiceAccount;
    }


    public static class NoUnderlineSpan extends UnderlineSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false);
        }
    }

    public void onMessageSendSuccess(String oldId, MessageModel message) {
        int count = getCount();
        if (count > 0) {
            for (int i = count - 1; i >= 0; i--) {
                MessageModel item = (MessageModel) getItem(i);
                if (item != null && message != null
                        && !TextUtils.isEmpty(oldId)
                        && TextUtils.equals(oldId, item.MessageID)) {
                    item.MessageID = message.MessageID;
                    item.MessageSendStatus = MessageStatus.MESSAGE_SEND_OK;
                    item.IsHavePermissionAccessReadStatus = message.IsHavePermissionAccessReadStatus;
                    break;
                }
            }
        }
    }
}
