package com.itutorgroup.tutorchat.phone.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;

import com.itutorgroup.tutorchat.phone.R;
import com.itutorgroup.tutorchat.phone.app.LPApp;
import com.itutorgroup.tutorchat.phone.domain.beans.ChatEmoji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.salesuite.saf.utils.SAFUtils;
import cn.salesuite.saf.utils.StringUtils;


public class FaceConversionUtil {

    /**
     * 每一页表情的个数
     */
    private int pageSize = 27;

    private static FaceConversionUtil mFaceConversionUtil;

    /**
     * 保存于内存中的表情HashMap
     */
    private HashMap<String, String> emojiMapZH;

    private HashMap<String, String> emojiMapUS;

    private HashMap<String, String> emojiMap;

    /**
     * 保存于内存中的表情集合
     */
    private List<ChatEmoji> emojis;

    /**
     * 表情分页的结果集合
     */
    public List<List<ChatEmoji>> emojiLists;

    private Locale mCurLocale;

    private FaceConversionUtil() {

    }

    public static FaceConversionUtil getInstace() {
        if (mFaceConversionUtil == null) {
            mFaceConversionUtil = new FaceConversionUtil();
        }
        return mFaceConversionUtil;
    }

    public SpannableString getExpressionString(Context context, String str, int size) {
        if (TextUtils.isEmpty(str)) {
            return new SpannableString("");
        }
        SpannableString spannableString = new SpannableString(getExpressionStringFromServer(str));
        String zhengze = "\\[[^\\]]+\\]";
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        try {
            dealExpression(context, spannableString, sinaPatten, size);
        } catch (Exception e) {
        }
        return spannableString;
    }

    /**
     * 发送 [** 00:0000**]形式
     *
     * @param str
     * @return
     */
    public String sendExpressionStringToServer(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(str);
        String zhengze = "\\[[^\\]]+\\]";
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        Matcher matcher = sinaPatten.matcher(spannableString);
        StringBuffer sb = new StringBuffer();
        try {
            while (matcher.find()) {
                String key = matcher.group();
                String value = emojiMap.get(key);
                if (StringUtils.isEmpty(value)) {
                    value = emojiMapZH.get(key);
                }
                if (TextUtils.isEmpty(value)) {
                    continue;
                }
                matcher.appendReplacement(sb, getEmojiCodeByValue(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 发送中文[可爱]到服务端
     *
     * @param str
     * @return
     */
    public String sendExpressionZhToServer(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(str);
        String zhengze = "\\[[^\\]]+\\]";
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        Matcher matcher = sinaPatten.matcher(spannableString);
        StringBuffer sb = new StringBuffer();
        try {
            while (matcher.find()) {
                String key = matcher.group();
                String value = emojiMap.get(key);
                if (TextUtils.isEmpty(value)) {
                    String tmp[] = key.split(":");
                    String emojiGroup = String.format("%02d", Integer.parseInt(tmp[0].subSequence(tmp[0].lastIndexOf("*") + 1, tmp[0].length()).toString()));
                    int emojiElement = Integer.parseInt(tmp[1].subSequence(0, tmp[1].indexOf("*")).toString());
                    key = getEmojiExpressionByFileName(emojiMapZH, "emoji_" + emojiGroup + "_" + emojiElement);
                } else if (!mCurLocale.getLanguage().equals(Locale.CHINA.getLanguage())) {
                    key = getEmojiExpressionByFileName(emojiMapZH, value);
                }
                matcher.appendReplacement(sb, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * ** -> [笑脸]/[smile]
     * 根据语言环境得到 [笑脸]/[smile]
     * @param str
     * @return
     */
    public String getExpressionZhFromServer(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(str);
        String zhengze = "\\[[^\\]]+\\]";
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        Matcher matcher = sinaPatten.matcher(spannableString);
        StringBuffer sb = new StringBuffer();
        try {
            while (matcher.find()) {
                String key = matcher.group();
                String value = emojiMap.get(key);
                if (StringUtils.isEmpty(value)) {
                    value = emojiMapZH.get(key);
                    if(StringUtils.isNotEmpty(value)){
                        key = getEmojiExpressionByFileName(emojiMap, value);
                    }else{
                        String tmp[] = key.split(":");
                        String emojiGroup = String.format("%02d", Integer.parseInt(tmp[0].subSequence(tmp[0].lastIndexOf("*") + 1, tmp[0].length()).toString()));
                        int emojiElement = Integer.parseInt(tmp[1].subSequence(0, tmp[1].indexOf("*")).toString());
                        key = getEmojiExpressionByFileName(emojiMap, "emoji_" + emojiGroup + "_" + emojiElement);
                    }
                }
                matcher.appendReplacement(sb, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        matcher.appendTail(sb);
        return sb.toString();


    }


    public String getExpressionStringFromServer(String str) {
        if (TextUtils.isEmpty(str))
            return null;
//        String zhengze = "\\[[^\\]]+\\]";
        String zhengze = "\\[[^\\]]+\\*\\*\\]";
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        Matcher matcher = sinaPatten.matcher(str);
        StringBuffer sb = new StringBuffer();
        try {
            while (matcher.find()) {
                String key = matcher.group();
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                String tmp[] = key.split(":");
                String emojiGroup = String.format("%02d", Integer.parseInt(tmp[0].subSequence(tmp[0].lastIndexOf("*") + 1, tmp[0].length()).toString()));
                int emojiElement = Integer.parseInt(tmp[1].subSequence(0, tmp[1].indexOf("*")).toString());
                matcher.appendReplacement(sb, getEmojiExpressionByFileName(emojiMap, "emoji_" + emojiGroup + "_" + emojiElement));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    /**
     * 添加表情
     *
     * @param context
     * @param imgId
     * @param spannableString
     * @return
     */
    public SpannableString addFace(Context context, int imgId,
                                   String spannableString) {
        if (TextUtils.isEmpty(spannableString)) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                imgId);
        bitmap = Bitmap.createScaledBitmap(bitmap, SAFUtils.dip2px(context, 25), SAFUtils.dip2px(context, 25), true);
        ImageSpan imageSpan = new ImageSpan(context, bitmap);
        SpannableString spannable = new SpannableString(spannableString);
        spannable.setSpan(imageSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
     *
     * @param context
     * @param spannableString
     * @param patten
     * @throws Exception
     */
    private void dealExpression(Context context,
                                SpannableString spannableString, Pattern patten, int size)
            throws Exception {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            String value = emojiMap.get(key);
            if (StringUtils.isEmpty(value)) {
                value = emojiMapZH.get(key);
                if (StringUtils.isEmpty(value)) {
                    value = emojiMapUS.get(key);
                }
            }
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());
            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(
                        context.getResources(), resId);
                bitmap = Bitmap.createScaledBitmap(bitmap, SAFUtils.dip2px(context, size), SAFUtils.dip2px(context, size), true);
                // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
                ImageSpan imageSpan = new ImageSpan(bitmap);
                int end = matcher.start() + key.length();
                // 将该图片替换字符串中规定的位置中
                spannableString.setSpan(imageSpan, matcher.start(), end,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

    }

    public synchronized void getFileText(Context context) {
        emojiMap = new HashMap<>();
        emojiMapZH = new HashMap<>();
        emojiMapUS = new HashMap<>();
        emojis = new ArrayList<>();
        emojiLists = new ArrayList<>();
        ParseData(FileUtils.getEmojiFile(context), context);
    }

    /**
     * 解析字符
     *
     * @param data
     */
    private void ParseData(List<String> data, Context context) {
        if (data == null) {
            return;
        }
        try {
            for (String str : data) {
                String[] text = str.split(",");
                String fileName = text[0]
                        .substring(0, text[0].lastIndexOf("."));
                int emojiName = context.getResources().getIdentifier(text[1],
                        "string", context.getPackageName());
                emojiMap.put(LPApp.getInstance().getString(emojiName), fileName);
                mCurLocale = LPApp.getInstance().getResources().getConfiguration().locale;
                emojiMapZH.put(getResourcesByLocale(Locale.CHINA, LPApp.getInstance().getResources()).getString(emojiName), fileName);
                resetLocale(mCurLocale, LPApp.getInstance().getResources());
                emojiMapUS.put(getResourcesByLocale(Locale.ENGLISH, LPApp.getInstance().getResources()).getString(emojiName), fileName);
                resetLocale(mCurLocale, LPApp.getInstance().getResources());
                int resID = context.getResources().getIdentifier(fileName,
                        "drawable", context.getPackageName());
                if (resID != 0) {
                    ChatEmoji emojEentry = new ChatEmoji();
                    emojEentry.setId(resID);
                    emojEentry.setCharacter(LPApp.getInstance().getString(emojiName));
                    emojEentry.setFaceName(fileName);
                    emojis.add(emojEentry);
                }
            }
            int pageCount = (int) Math.ceil(emojis.size() / pageSize + 0.1);
            for (int i = 0; i < pageCount; i++) {
                try {
                    emojiLists.add(getData(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分页数据
     *
     * @param page
     * @return
     */
    private List<ChatEmoji> getData(int page) {
        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > emojis.size()) {
            endIndex = emojis.size();
        }
        List<ChatEmoji> list = new ArrayList<ChatEmoji>();
        try {
            list.addAll(emojis.subList(startIndex, endIndex));
        } catch (Exception e) {
        }
        if (list.size() < pageSize) {
            for (int i = list.size(); i < pageSize; i++) {
                ChatEmoji object = new ChatEmoji();
                list.add(object);
            }
        }
        if (list.size() == pageSize) {
            ChatEmoji object = new ChatEmoji();
            object.setId(R.drawable.delete_emoji);
            list.add(object);
        }
        return list;
    }

    /**
     * emoji_00_4 -> [**00:0000**]
     *
     * @param fileName
     * @return
     */
    public String getEmojiCodeByValue(String fileName) {
        if (StringUtils.isEmpty(fileName))
            return null;
        StringBuilder builder = new StringBuilder();
        String[] number = fileName.split("_");
        builder.append("[**" + String.format("%02d", Integer.parseInt(number[1])) + ":" + String.format("%04d", Integer.parseInt(number[2])) + "**]");
        return builder.toString();
    }

    /**
     * emoji_00_4 -> [笑脸]/[smile]
     *
     * @param hashMap  key [笑脸]/[smile] value  emoji_00_4
     * @param fileName
     * @return
     */
    public String getEmojiExpressionByFileName(HashMap<String, String> hashMap, String fileName) {
        Iterator<String> it = hashMap.keySet().iterator();
        while (it.hasNext()) {
            String keyString = it.next();
            if (hashMap.get(keyString).equals(fileName))
                return keyString;
        }
        return null;
    }


    private Resources getResourcesByLocale(Locale locale, Resources resources) {
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);
        return resources;
    }

    private void resetLocale(Locale mCurLocale, Resources resources) {
        Configuration config = resources.getConfiguration();
        config.locale = mCurLocale;
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);
    }


}