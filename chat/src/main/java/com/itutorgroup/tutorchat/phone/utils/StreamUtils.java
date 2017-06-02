package com.itutorgroup.tutorchat.phone.utils;

import android.support.annotation.NonNull;

import com.itutorgroup.tutorchat.phone.config.Constant;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by tom_zxzhang on 2016/8/15.
 */
public class StreamUtils {


    /**
     * @param
     * @return 字节数组
     * @throws Exception
     * @方法功能 InputStream 转为 byte
     */
    public static byte[] inputStream2Byte(InputStream inStream)
            throws Exception {
        // ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        // byte[] buffer = new byte[1024];
        // int len = -1;
        // while ((len = inStream.read(buffer)) != -1) {
        // outSteam.write(buffer, 0, len);
        // }
        // outSteam.close();
        // inStream.close();
        // return outSteam.toByteArray();
        int count = 0;
        while (count == 0) {
            count = inStream.available();
        }
        byte[] b = new byte[count];
        inStream.read(b);
        return b;
    }

    @NonNull
    public static String printBytesToString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        if (b != null) {
            int len = b.length;
            for (int i = 0; i < len; i++) {
                sb.append(b[i] & 0xFF).append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * @param //字节数组
     * @return InputStream
     * @throws Exception
     * @方法功能 byte 转为 InputStream
     */
    public static InputStream byte2InputStream(byte[] b) throws Exception {
        InputStream is = new ByteArrayInputStream(b);
        return is;
    }

    /**
     * @param //短整型
     * @return 两位的字节数组
     * @功能 短整型与字节的转换
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * @param //两位的字节数组
     * @return 短整型
     * @功能 字节的转换与短整型
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * @param //整型
     * @return 四位的字节数组
     * @方法功能 整型与字节数组的转换
     */
    public static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    /**
     * @param //字节数组
     * @return 整型
     * @方法功能 字节数组和整型的转换
     */
    public static int bytesToInt(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    /**
     * @param //字节数组
     * @return 长整型
     * @方法功能 字节数组和长整型的转换
     */
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();
            // 将最低位保存在最低位
            temp = temp >> 8;
            // 向右移8位
        }
        return b;
    }

    /**
     * @param //字节数组
     * @return 长整型
     * @方法功能 字节数组和长整型的转换
     */
    public static long byteToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff; // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) bs[i - begin] = src[i];
        return bs;
    }

    public static <T> byte[] postBytesBuild(int operation, T obj) {
        return postBytesBuild(operation, obj, 1, 0, null);
    }

    public static <T> byte[] postBytesBuild(int operation, T obj, int aesKey, String aesValue) {
        return postBytesBuild(operation, obj, 2, aesKey, aesValue);
    }

    private static <T> byte[] postBytesBuild(int operation, T obj, int version, int aesKey, String aesValue) {
        byte[] bytes = null;
        if (obj != null) {
            bytes = ProtoStuffSerializerUtil.serialize(obj);
        }
        if (aesValue != null && bytes != null) {
            bytes = AesEncryptionUtil.encrypt(bytes, aesValue, Constant.AES_IV);
        }
        int headLen = aesValue == null ? 16 : 20;
        int objLen = bytes == null ? 0 : bytes.length;
        int packageLen = headLen + objLen;
        byte[] bs = new byte[packageLen];
        byte[] bytes1 = intToByte(packageLen);
        byte[] bytes2 = shortToByte((short) headLen);
        byte[] bytes3 = shortToByte((short) version);
        byte[] bytes4 = intToByte(operation);
        byte[] bytes5 = intToByte(1);
        System.arraycopy(bytes1, 0, bs, 0, 4); // package len
        System.arraycopy(bytes2, 0, bs, 4, 2); // head len
        System.arraycopy(bytes3, 0, bs, 6, 2); // protocol version
        System.arraycopy(bytes4, 0, bs, 8, 4); // operation
        System.arraycopy(bytes5, 0, bs, 12, 4); // seq
        if (aesValue != null) {
            byte[] bytes6 = intToByte(aesKey);
            System.arraycopy(bytes6, 0, bs, 16, 4); // AES key
        }
        if (bytes != null) {
            System.arraycopy(bytes, 0, bs, headLen, bytes.length);
        }
        return bs;
    }

    public static <T> byte[] buildTcpBytes(int operation, T obj, int version, String key) {
        byte[] bytes = null;
        if (obj != null) {
            bytes = ProtoStuffSerializerUtil.serialize(obj);
        }
        if (key != null && bytes != null) {
            bytes = AesEncryptionUtil.encrypt(bytes, key, Constant.TCP_AES_IV);
        }
        int headLen = 16;
        int objLen = bytes == null ? 0 : bytes.length;
        int packageLen = headLen + objLen;
        byte[] bs = new byte[packageLen];
        byte[] bytes1 = intToByte(packageLen);
        byte[] bytes2 = shortToByte((short) headLen);
        byte[] bytes3 = shortToByte((short) version);
        byte[] bytes4 = intToByte(operation);
        byte[] bytes5 = intToByte(1);
        System.arraycopy(bytes1, 0, bs, 0, 4); // package len
        System.arraycopy(bytes2, 0, bs, 4, 2); // head len
        System.arraycopy(bytes3, 0, bs, 6, 2); // protocol version
        System.arraycopy(bytes4, 0, bs, 8, 4); // operation
        System.arraycopy(bytes5, 0, bs, 12, 4); // seq
        if (bytes != null) {
            System.arraycopy(bytes, 0, bs, headLen, bytes.length);
        }
        return bs;
    }
}
