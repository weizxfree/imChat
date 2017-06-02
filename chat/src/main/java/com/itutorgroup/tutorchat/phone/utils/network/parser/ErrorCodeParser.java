package com.itutorgroup.tutorchat.phone.utils.network.parser;

import android.text.TextUtils;
import android.util.SparseArray;
import android.util.Xml;

import com.itutorgroup.tutorchat.phone.app.LPApp;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by joyinzhao on 2016/8/26.
 */
public class ErrorCodeParser {

    public static final String CODE_CONFIG_FILE_NAME = "configs/return_codes_%s.xml";

    public static SparseArray<String> getErrorCodeMessageMap() throws IOException, XmlPullParserException {
        SparseArray<String> array = null;

        XmlPullParser parser = Xml.newPullParser();
        String language = LPApp.getInstance().getResources().getConfiguration().locale.getLanguage();
        if (!TextUtils.equals("zh", language) && !TextUtils.equals("en", language)) {
            language = "en";
        }
        InputStream in = LPApp.getInstance().getAssets().open(String.format(CODE_CONFIG_FILE_NAME, language));
        parser.setInput(in, "UTF-8");
        int eventType = parser.getEventType();

        int key = 0;
        String value = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String nodeName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    array = new SparseArray<>();
                    break;
                case XmlPullParser.START_TAG:
                    if (nodeName.equals("code")) {
                        key = new Integer(parser.nextText());
                    } else if (nodeName.equals("data")) {
                        value = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (nodeName.equals("item") && !TextUtils.isEmpty(value)) {
                        array.put(key, value);
                    }
                    break;
            }
            eventType = parser.next();
        }
        return array;
    }
}
