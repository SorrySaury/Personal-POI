package com.sorry.personalpoi.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import com.sorry.personalpoi.AlbumConfig;
import com.sorry.personalpoi.bean.AlbumData;
import com.sorry.personalpoi.bean.MaterialBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @anthor sorry
 * @time 2019/5/17
 * @class 相册数据读取类
 */
public class AlbumUtils {
    private ContentResolver resolver;
    public  ExifInterface exif;
    public  Double Latitude = new Double(0);
    public  Double Longitude = new Double(0);
    public  String LATITUDE;
    public  String LATITUDE_REF ;
    public  String LONGITUDE ;
    public  String LONGITUDE_REF;
    public AlbumUtils(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public AlbumUtils() {
    }

    /**
     * 获取本地所有的视频
     *
     * @return list
     */
    private List<MaterialBean> getAllLocalVideos() {
        List<MaterialBean> list = new ArrayList<>();
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return list;
        }
            exif = null;
        try {
            while (cursor.moveToNext()) {
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)); // 大小
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)); // 路径
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)); // 时长
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String dec = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                String Sdate = DateUtil.formatDate(date);
                String formatDur = formatTime(duration);
                getgps(path);
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                MaterialBean materialBean = new MaterialBean(path, Sdate, id, dec, name, 2, size, date, duration, formatDur,Longitude,Latitude);
                list.add(materialBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * 获取本地所有的图片
     *
     * @return list
     */
    private List<MaterialBean> getAllLocalPhotos() {
        List<MaterialBean> list = new ArrayList<>();
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_TAKEN + " desc");
        if (cursor != null && cursor.moveToFirst()) {
            exif = null;
            do {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String dec = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String Sdate = DateUtil.formatDate(date);
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                getgps(path);
                Log.i("gpsinfo",String.valueOf(Longitude));
                MaterialBean materialBean = new MaterialBean(path, Sdate, id, dec, name, 1, size, date, 0, null, Longitude, Latitude);
                list.add(materialBean);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<AlbumData> getFormatData(ArrayList<MaterialBean> beans, String type) {
        ArrayList<AlbumData> datas = new ArrayList<>();
        ArrayList<MaterialBean> list = new ArrayList<>();
        List<String> photoDates = new ArrayList<>();
        String dateTag = "";
        if (beans.size() == 0) {
            dateTag = DateUtil.formatDate(System.currentTimeMillis());
            MaterialBean materialBean = new MaterialBean(null, null, 0, null, null, 3, 0, 0, 0, null,0,0);
            list.add(materialBean);
            AlbumData data = new AlbumData();
            data.setDate(dateTag);
            data.setList(list);
            datas.add(data);
        } else {
            for (int i = 0; i < beans.size(); i++) {
                if (!photoDates.contains(beans.get(i).getFormatDate())) {
                    if (list != null && list.size() > 0) {
                        ArrayList<MaterialBean> mList = new ArrayList<>();
                        mList.addAll(list);
                        AlbumData data = new AlbumData();
                        data.setDate(dateTag);
                        data.setList(mList);
                        datas.add(data);
                        list.clear();
                    }
                    photoDates.add(beans.get(i).getFormatDate());
                    dateTag = beans.get(i).getFormatDate();
                }
                if (dateTag.equals(beans.get(i).getFormatDate())) {//添加相机位
                    if (i == 0 && type.equals(AlbumConfig.ADD_CAMERA)) {
                        MaterialBean materialBean = new MaterialBean(null, null, 0, null, null, 3, 0, 0, 0, null,0,0);
                        list.add(materialBean);
                    }
                    list.add(beans.get(i));
                }
                if (i == beans.size() - 1) {
                    ArrayList<MaterialBean> mList = new ArrayList<>();
                    mList.addAll(list);
                    AlbumData data = new AlbumData();
                    data.setDate(dateTag);
                    data.setList(mList);
                    datas.add(data);
                }
            }
        }
        return datas;
    }





    public void getgps(String path){
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
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
            double[] LatLng = CoordinateTransformUtil.wgs84tobd09(Longitude,Latitude);
        }
        else{
            Longitude = 0.0;Latitude = 0.0;
        }

    }
    /**
     * 获取视频缩略图
     *
     * @return bitmap
     * 此方式获取某些格式第一帧为空，故暂且用Glide加载
     */
    public static Bitmap getThumbnail(String path) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据文件路径获取缩略图
            retriever.setDataSource(path);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 视频时间格式化
     *
     * @return String
     */
    private String formatTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    public ArrayList<MaterialBean> getSortData() {
        ArrayList<MaterialBean> beans = new ArrayList<>();
        beans.addAll(getAllLocalPhotos());
        beans.addAll(getAllLocalVideos());
        Collections.sort(beans, new Comparator<MaterialBean>() {
            @Override
            public int compare(MaterialBean bean1, MaterialBean bean2) {
                return Long.valueOf(bean2.getDate()).compareTo(Long.valueOf(bean1.getDate()));
            }
        });
        return beans;
    }

    public ArrayList<MaterialBean> getSortSite(){
        ArrayList<MaterialBean> beans = new ArrayList<>();
        beans.addAll(getAllLocalPhotos());
        beans.addAll(getAllLocalVideos());
        for(MaterialBean m:beans){
        }
        return beans;
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
