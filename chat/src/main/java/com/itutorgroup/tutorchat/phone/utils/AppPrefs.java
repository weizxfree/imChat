/**
 * 
 */
package com.itutorgroup.tutorchat.phone.utils;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.salesuite.saf.prefs.BasePrefs;

/**
 * app信息本地缓存
 * 
 * @author shzhujian
 *
 */
public class AppPrefs extends BasePrefs {

	private static final String PREFS_NAME = "UserPrefs";

	private static final String USER_LIST = "user_list";

	private AppPrefs(Context context) {
		super(context, PREFS_NAME);
	}

	public static AppPrefs get(Context context) {
		return new AppPrefs(context);
	}

	@Override
	public Object getObject(String arg0) {
		return super.getObject(arg0);
	}

	@Override
	public void putObject(String arg0, Object arg1) {
		super.putObject(arg0, arg1);
	}

	@Override
	public String getString(String key, String defValue) {
		return super.getString(key, defValue);
	}

	@Override
	public void putString(String key, String v) {
		super.putString(key, v);
	}


	@Override
	public int getInt(String key, int defValue) {
		return super.getInt(key, defValue);
	}

	@Override
	public void putInt(String key, int v) {
		super.putInt(key, v);
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		return super.getBoolean(key, defValue);
	}

	@Override
	public void putBoolean(String key, boolean v) {
		super.putBoolean(key, v);
	}

	public static String writeObject(Object obj) {
	        try {  
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	            ObjectOutputStream oos = new ObjectOutputStream(baos);  
	            oos.writeObject(obj);  
	            String stringBase64 = new String(Base64.encodeBase64(baos.toByteArray()));  
	            return stringBase64;
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	        return "";
	    }

	
	
	
	 public static Object getObjectFromString(String stringBase64) {
			try {
				if (TextUtils.isEmpty(stringBase64))
					return null;
				
				byte[] base64Bytes = Base64.decodeBase64(stringBase64.getBytes());
				ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
				ObjectInputStream ois = new ObjectInputStream(bais);
				return ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
	    }
	

}
