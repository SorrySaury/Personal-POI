package com.sorry.personalpoi.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sorry.personalpoi.bean.LocalPictrue;


public class SdCardFileTool {

    public static List<File> filelist = new ArrayList<File>();


    public static boolean getImageFile(String fName){
        boolean re;
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();

        if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            re = true;
        } else {
            re = false;
        }
        return re;
    }



    //通过ContentProvider获取系统相册中所有相册的图片文件
    public static ArrayList<LocalPictrue> getPhotoData(Context context) {

        ArrayList<LocalPictrue> photoList = new ArrayList<LocalPictrue>();
        ContentResolver cr = context.getContentResolver();

        // 获取SD卡上的图片
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = cr.query(mImageUri,projection,MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?", new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        readCursor(context, cursor, photoList);
        return photoList;
    }

    private static void readCursor(Context context, Cursor cursor, List<LocalPictrue> photoList) {

        if (null == cursor) {
            return;
        }
        ExifInterface exif = null;
        float[] latLong = new float[2];
        int height = DensityUtil.dip2px(context, 88);
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

                String url = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));

                try {
                    exif = new ExifInterface(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                // your Final lat Long Values
                Double Latitude = new Double(0);
                Double Longitude = new Double(0);


                if ((LATITUDE != null)
                        && (LATITUDE_REF != null)
                        && (LONGITUDE != null)
                        && (LONGITUDE_REF != null)) {

                    if (LATITUDE_REF.equals("N")) {
                        Latitude = convertToDegree(LATITUDE);
                    } else {
                        Latitude = 0 - convertToDegree(LATITUDE);
                    }

                    if (LONGITUDE_REF.equals("E")) {
                        Longitude = convertToDegree(LONGITUDE);
                    } else {
                        Longitude = 0 - convertToDegree(LONGITUDE);
                    }

                } else {
                    continue;
                }

                double[] LatLng = CoordinateTransformUtil.wgs84tobd09(Longitude,Latitude);
                if(LatLng.length == 2 ) {
                    Log.i("locationfoto;",String.valueOf(LatLng[1])+String.valueOf(LatLng[0])+"###"+title+"###"+url+"###"+String.valueOf(photoList.size()));//测试得到的有经纬度的图片
                    photoList.add(new LocalPictrue(title, url, height, LatLng[1], LatLng[0]));
                }
            } while (cursor.moveToNext());

            if (cursor != null) {
                cursor.close();
            }
        }
    }


    //获取照片列表
    public static List<File> getFileList(String strPath){
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if(files != null){
            for(File f:files){
                String fileName = f.getName();
                if(f.isDirectory()){
                    continue;
                }else if(getImageFile(f.getPath())){
                    String strFileName = f.getAbsolutePath();
                    filelist.add(f);
                }else{
                    continue;
                }
            }
        }
        return filelist;
    }


    private static Double convertToDegree(String stringDMS) {
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;
        result = new Double(FloatD + (FloatM / 60) + (FloatS / 3600));
        return result;
    }
}
