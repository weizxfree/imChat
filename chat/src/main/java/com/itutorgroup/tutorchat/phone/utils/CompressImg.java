package com.itutorgroup.tutorchat.phone.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CompressImg {


    public static byte[] saveCompressImg(String path,boolean isLargeImage){
//        WeakReference<Bitmap> weakReference;
        Bitmap bitmap = null;
        String newPath = null;
        //图片参数。
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只计算几何尺寸，不返回bitmap，不占内存。
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //宽、高。
        int w = options.outWidth;
        int h = options.outHeight;
        float ww = 720;
        float hh = 1280;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int rate = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            rate = (int) (options.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            rate = (int) (options.outHeight / hh);
        }
        if (rate <= 0)
            rate = 1;
        //设置压缩参数。
        options.inSampleSize = rate;
        options.inJustDecodeBounds = false;
        //压缩。
        bitmap = BitmapFactory.decodeFile(path, options);
        byte[] result = null;
        if(isLargeImage){
            result = bitmap2Bytes(bitmap,100);
        }else{
            result = bitmap2Bytes(bitmap,60);
        }
        newPath = FileUtils.CreateNewPath(MD5Util.getMD5String(result));
        FileUtils.byte2File(result,newPath);
        return result;
    }

    public static Bitmap compressImg(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float ww = PixelUtil.dp2px(150);//这里设置宽度为480f
        float hh = PixelUtil.dp2px(200);//这里设置高度为800f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩       
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    public static int getInSampleRate(String path){
        //图片参数。
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只计算几何尺寸，不返回bitmap，不占内存。
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //宽、高。
        int w = options.outWidth;
        int h = options.outHeight;
        float ww = PixelUtil.dp2px(100);//这里设置宽度为480f
        float hh = PixelUtil.dp2px(150);//这里设置高度为800f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int rate = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            rate = (int) (options.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            rate = (int) (options.outHeight / hh);
        }
        if (rate <= 0)
            rate = 1;
        options.inJustDecodeBounds = false;
        return  rate;
    }

    public static byte[] bitmap2Bytes(Bitmap bm,int size) {

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, size, baos);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return baos.toByteArray();
    }


}
