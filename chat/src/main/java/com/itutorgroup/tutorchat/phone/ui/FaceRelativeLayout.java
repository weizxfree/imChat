package com.itutorgroup.tutorchat.phone.ui;

/**
 * Created by tom_zxzhang on 2016/8/24.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.activity.chat.DelayedSendActivity;
import com.itutorgroup.tutorchat.phone.activity.image.PhotoPickerActivity;
import com.itutorgroup.tutorchat.phone.adapter.FaceAdapter;
import com.itutorgroup.tutorchat.phone.adapter.ViewPagerAdapter;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.config.Constant;
import com.itutorgroup.tutorchat.phone.domain.beans.ChatEmoji;
import com.itutorgroup.tutorchat.phone.ui.photo.picker.utils.PhotoUtils;
import com.itutorgroup.tutorchat.phone.utils.FaceConversionUtil;
import com.itutorgroup.tutorchat.phone.utils.common.CommonLoadingListener;
import com.itutorgroup.tutorchat.phone.utils.manager.MessageManager;
import com.itutorgroup.tutorchat.phone.utils.manager.UserSettingManager;
import com.itutorgroup.tutorchat.phone.utils.ui.ToastUtil;
import com.itutorgroup.tutorchat.phone.utils.voice.AudioRecorderButton;

import java.util.ArrayList;
import java.util.List;

import cn.salesuite.saf.utils.SAFUtils;
import cn.salesuite.saf.utils.StringUtils;


public class FaceRelativeLayout extends RelativeLayout implements
        AdapterView.OnItemClickListener, View.OnClickListener {

    private Context context;
    private OnCorpusSelectedListener mListener;
    private onClickSendListener clickSendListener;
    private OnInputEqualAtListener inputEqualAtListener;
    private ViewPager vp_face;
    private ArrayList<View> pageViews;
    private LinearLayout layout_point;
    private ArrayList<ImageView> pointViews;
    private List<List<ChatEmoji>> emojis;
    private RelativeLayout emotionLayout;
    private View ll_othersChoose;
    private RadioButton btn_face;
    private CheckBox btn_voice;
    private EditText et_sendmessage;
    private List<FaceAdapter> faceAdapters;
    private int current = 0;
    private List<View> views;
    private AudioRecorderButton btnRecordLayout;
    private View mContentView;
    private LinearLayout text_face_layout;
    private Button btnSend;
    private TextView mSendDelay;
    private TextView mPhoto;
    private TextView mCamera;
    private String targetId, lastText;
    private boolean isGroup;
    private boolean isHavePicAndVoiceRight = true;


    EmotionInputDetector emotionInputDetector;

    public FaceRelativeLayout(Context context) {
        super(context);
        this.context = context;
    }

    public FaceRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void setOnClickSendListener(onClickSendListener listener) {
        clickSendListener = listener;
    }

    public void setTargetID(String targetID, boolean isGroup) {
        this.targetId = targetID;
        this.isGroup = isGroup;
    }

    public void setOnInputEqualAtListener(OnInputEqualAtListener listener) {
        inputEqualAtListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.text_photo:

                Intent openAlbumIntent = new Intent(context, PhotoPickerActivity.class);
                openAlbumIntent.setType("image/*");
                openAlbumIntent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_MULTI);
                openAlbumIntent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, 9);
                ((Activity) context).startActivityForResult(openAlbumIntent, Constant.REQ_FROM_PHOTO);
                break;


            case R.id.text_camera:

                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (openCameraIntent.resolveActivity(LPApp.getInstance().getPackageManager()) != null) {
                    Constant.CAMERA_FILE = PhotoUtils.createFile(LPApp.getInstance());
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(Constant.CAMERA_FILE));
                    ((Activity) context).startActivityForResult(openCameraIntent, Constant.REQ_FROM_CAMERA);
                } else {
                    ToastUtil.show(R.string.picker_msg_no_camera);
                }
                break;


            case R.id.text_send_delay:
                context.startActivity(new Intent(context, DelayedSendActivity.class).putExtra("target_id", targetId).putExtra("is_group", isGroup));
                break;


        }


    }


    public interface OnCorpusSelectedListener {
        void onCorpusSelected(ChatEmoji emoji);
    }

    public interface onClickSendListener {
        void onClickSend();
    }


    public interface OnInputEqualAtListener {
        void equalAt();

        void deleteAltMember(String name);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        emojis = FaceConversionUtil.getInstace().emojiLists;
        onCreate();
    }

    private void onCreate() {
        Init_View();
        Init_viewPager();
        Init_Point();
        Init_Data();
    }


    /**
     * 初始化控件
     */
    private void Init_View() {
        vp_face = (ViewPager) findViewById(R.id.vp_contains);
        et_sendmessage = (EditText) findViewById(R.id.et_sendmessage);
        layout_point = (LinearLayout) findViewById(R.id.iv_image);
        text_face_layout = (LinearLayout) findViewById(R.id.text_face_layout);
        btn_face = (RadioButton) findViewById(R.id.btn_face);
        btn_voice = (CheckBox) findViewById(R.id.btn_voice);
        btnRecordLayout = (AudioRecorderButton) findViewById(R.id.audioRecorderLayout);
        emotionLayout = (RelativeLayout) findViewById(R.id.ll_facechoose);
        ll_othersChoose = findViewById(R.id.ll_othersChoose);
        btnSend = (Button) findViewById(R.id.btnSend);
        mSendDelay = (TextView) findViewById(R.id.text_send_delay);
        mPhoto = (TextView) findViewById(R.id.text_photo);
        mCamera = (TextView) findViewById(R.id.text_camera);
        views = new ArrayList();
        views.add(emotionLayout);
        if (!UserSettingManager.getInstance().checkHasRight(UserSettingManager.USER_RIGHT_SEND_PIC) &&
                !UserSettingManager.getInstance().checkHasRight(UserSettingManager.USER_RIGHT_DELAY_MESSAGE)) {
            btnSend.setBackgroundResource(R.drawable.send);
            btnSend.setTag(R.drawable.send);
            isHavePicAndVoiceRight = false;
        } else {
            btnSend.setTag(R.drawable.more_function);
            if (!UserSettingManager.getInstance().checkHasRight(UserSettingManager.USER_RIGHT_DELAY_MESSAGE)) {
                mSendDelay.setVisibility(View.GONE);
            }
            if (!UserSettingManager.getInstance().checkHasRight(UserSettingManager.USER_RIGHT_SEND_PIC)) {
                mPhoto.setVisibility(View.GONE);
                mCamera.setVisibility(GONE);
            }
        }
        if (!UserSettingManager.getInstance().checkHasRight(UserSettingManager.USER_RIGHT_VOICE)) {
            btn_voice.setVisibility(View.GONE);
        }
        initListener();
    }


    private void Init_viewPager() {
        pageViews = new ArrayList<View>();
        // 左侧添加空页
        View nullView1 = new View(context);
        // 设置透明背景
        nullView1.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView1);

        // 中间添加表情页

        faceAdapters = new ArrayList<FaceAdapter>();
        for (int i = 0; i < emojis.size(); i++) {
            GridView view = new GridView(context);
            view.setGravity(CENTER_VERTICAL);
//            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
            FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));
            view.setAdapter(adapter);
            faceAdapters.add(adapter);
            view.setOnItemClickListener(this);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
//            view.setBackgroundColor(getResources().getColor(R.color.app_blackground));
            view.setHorizontalSpacing(SAFUtils.dip2px(context, 5));
            view.setVerticalSpacing(SAFUtils.dip2px(context, 5));
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(SAFUtils.dip2px(context, 3), SAFUtils.dip2px(context, 20), SAFUtils.dip2px(context, 3), 0);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            pageViews.add(view);
        }

        // 右侧添加空页面
        View nullView2 = new View(context);
        // 设置透明背景
        nullView2.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView2);
    }

    /**
     * 初始化游标
     */
    private void Init_Point() {

        pointViews = new ArrayList<ImageView>();
        ImageView imageView;
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.ic_pager_indicator_normal);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = SAFUtils.dip2px(context, 5);
            layoutParams.rightMargin = SAFUtils.dip2px(context, 5);
            layoutParams.width = SAFUtils.dip2px(context, 4);
            layoutParams.height = SAFUtils.dip2px(context, 4);
            layout_point.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.ic_pager_indicator_selected);
            }
            pointViews.add(imageView);

        }
    }

    /**
     * 填充数据
     */
    private void Init_Data() {
        vp_face.setAdapter(new ViewPagerAdapter(pageViews));
        vp_face.setCurrentItem(1);
        current = 0;
        vp_face.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                current = arg0 - 1;
                draw_Point(arg0);
                if (arg0 == pointViews.size() - 1 || arg0 == 0) {
                    if (arg0 == 0) {
                        vp_face.setCurrentItem(arg0 + 1);
                        pointViews.get(1).setBackgroundResource(R.drawable.ic_pager_indicator_selected);
                    } else {
                        vp_face.setCurrentItem(arg0 - 1);
                        pointViews.get(arg0 - 1).setBackgroundResource(
                                R.drawable.ic_pager_indicator_selected);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    /**
     * 绘制游标背景
     */
    public void draw_Point(int index) {
        for (int i = 1; i < pointViews.size(); i++) {
            if (index == i) {
                pointViews.get(i).setBackgroundResource(R.drawable.ic_pager_indicator_selected);
            } else {
                pointViews.get(i).setBackgroundResource(R.drawable.ic_pager_indicator_normal);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        ChatEmoji emoji = (ChatEmoji) faceAdapters.get(current).getItem(arg2);
        if (emoji.getId() == R.drawable.delete_emoji) {
            int selection = et_sendmessage.getSelectionStart();
            String text = et_sendmessage.getText().toString();
            if (selection > 0) {
                String text2 = text.substring(selection - 1);
                if ("]".equals(text2)) {
                    int start = text.lastIndexOf("[");
                    int end = selection;
                    et_sendmessage.getText().delete(start, end);
                    return;
                }
                et_sendmessage.getText().delete(selection - 1, selection);
            }
        }
        if (!TextUtils.isEmpty(emoji.getCharacter())) {
            if (mListener != null)
                mListener.onCorpusSelected(emoji);
            SpannableString spannableString = FaceConversionUtil.getInstace()
                    .addFace(getContext(), emoji.getId(), emoji.getCharacter());
            et_sendmessage.append(spannableString);
        }

    }


    private TextWatcher mInputTextWatcher = new TextWatcher() {
        int endPostion = 0;
        int startPostion = 0;
        boolean isdelete = false;
        CharSequence inputCharSequence = null;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            String deleText = s.toString().substring(start, start + count);
            inputCharSequence = s;
            String text = s.toString();
            if (StringUtils.isNotEmpty(text)) {
                endPostion = text.length() - 1;
                if (deleText.equals(" ")) {
                    startPostion = start;
                    for (int i = startPostion - 1; i >= 0; i--) {
                        if ('@' == text.charAt(i)) {
                            endPostion = i;
                            isdelete = true;
                            break;
                        }
                    }
                }
            }

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String addText = s.toString().substring(start, start + count);
            Editable editable = et_sendmessage.getText();
            int len = editable.length();
            int maxLen = getResources().getInteger(R.integer.max_length_input_chat_message);
            if (len > maxLen) {
                ToastUtil.show(context.getString(R.string.chat_content_over_max));
                int selEndIndex = Selection.getSelectionEnd(editable);
                String str = editable.toString();
                String newStr = str.substring(0, maxLen);
                et_sendmessage.setText(newStr);
                editable = et_sendmessage.getText();
                int newLen = editable.length();
                if (selEndIndex > newLen) {
                    selEndIndex = editable.length();
                }
                Selection.setSelection(editable, selEndIndex);
            }


            if (addText.equals("@")) {
                if (inputEqualAtListener != null)
                    inputEqualAtListener.equalAt();
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (StringUtils.isEmpty(lastText) && StringUtils.isNotEmpty(s.toString()) && isHavePicAndVoiceRight && (int) btnSend.getTag() == R.drawable.more_function) {
                btnSend.setBackgroundResource(R.drawable.send);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.chat_send_btn_out);
                btnSend.setTag(R.drawable.send);
                btnSend.startAnimation(animation);
            } else if (StringUtils.isNotEmpty(lastText) && StringUtils.isEmpty(s.toString()) && isHavePicAndVoiceRight && (int) btnSend.getTag() == R.drawable.send) {
                btnSend.setBackgroundResource(R.drawable.more_function);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.chat_send_btn_out);
                btnSend.startAnimation(animation);
                btnSend.setTag(R.drawable.more_function);
            }
            lastText = s.toString();
            et_sendmessage.removeTextChangedListener(this);
            if (StringUtils.isNotEmpty(s.toString()) && isdelete && endPostion <= s.toString().length()) {
                String delName = s.toString().subSequence(endPostion, startPostion).toString();
                s.delete(endPostion, startPostion);
                if (inputEqualAtListener != null) {
                    inputEqualAtListener.deleteAltMember(delName.substring(1, delName.length()));
                }
                isdelete = false;
                Editable ea = et_sendmessage.getText();
                Selection.setSelection(ea, ea.length());
            }
            et_sendmessage.addTextChangedListener(this);
        }
    };

    private void initListener() {
        mSendDelay.setOnClickListener(this);
        mPhoto.setOnClickListener(this);
        mCamera.setOnClickListener(this);
        et_sendmessage.addTextChangedListener(mInputTextWatcher);
    }

    public void restoreTextWithoutWatcher(final String text) {
        et_sendmessage.removeTextChangedListener(mInputTextWatcher);
        MessageManager.getInstance().loadEmojiText(context, et_sendmessage, text, 60, new CommonLoadingListener<Void>() {
            @Override
            public void onResponse(Void aVoid) {
                if (!TextUtils.isEmpty(text)) {
                    btnSend.setBackgroundResource(R.drawable.send);
                    btnSend.setTag(R.drawable.send);
                    lastText = text;
                }
                et_sendmessage.addTextChangedListener(mInputTextWatcher);
            }
        });

    }

    public void setContentView(View view) {
        mContentView = view;
        emotionInputDetector = EmotionInputDetector.with((Activity) context)
                .setEmotionView(emotionLayout)
                .setAddOthersView(ll_othersChoose)
                .bindToOthersButton(btnSend, clickSendListener)
                .bindToVoiceButton(btn_voice, text_face_layout, btnRecordLayout, isHavePicAndVoiceRight)
                .bindToContent(mContentView)
                .bindToEditText(et_sendmessage)
                .bindToEmotionButton(btn_face)
                .build();
    }


}

