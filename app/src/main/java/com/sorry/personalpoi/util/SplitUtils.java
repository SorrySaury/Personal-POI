package com.sorry.personalpoi.util;

import com.sorry.personalpoi.AlbumConfig;
import com.sorry.personalpoi.bean.AlbumData;
import com.sorry.personalpoi.bean.AlbumSplitData;
import com.sorry.personalpoi.bean.MaterialBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @anthor sorry
 * @time 2019/5/17
 * @class 拆分工具类
 */
public class SplitUtils {
    public static ArrayList<AlbumSplitData> split(List<MaterialBean> checkList, String splitType, boolean isAddImg, MaterialBean addVideo, List<MaterialBean> addImg) {
        if (AlbumConfig.DEFAULT_SPLIT.equals(splitType)) {
            return defaultSplit(checkList, isAddImg, addVideo, addImg);
        } else if (AlbumConfig.TIME_SPLIT.equals(splitType)) {
            return timeSplit(checkList, isAddImg);
        }
        return null;
    }

    private static ArrayList<AlbumSplitData> timeSplit(List<MaterialBean> checkList, boolean isAddImg) {
        ArrayList<MaterialBean> data = (ArrayList<MaterialBean>) checkList;
        Collections.sort(data, new Comparator<MaterialBean>() {
            @Override
            public int compare(MaterialBean bean1, MaterialBean bean2) {
                return Long.valueOf(bean2.getDate()).compareTo(Long.valueOf(bean1.getDate()));
            }
        });
        AlbumUtils albumUtils = new AlbumUtils();
        List<AlbumData> list = albumUtils.getFormatData(data, AlbumConfig.TIME_SPLIT);
        return getTimeSplitdata(list, isAddImg);
    }

    private static ArrayList<AlbumSplitData> defaultSplit(List<MaterialBean> checkList, boolean isAddImg, MaterialBean addVideo, List<MaterialBean> addImg) {
        List data = getVandP(checkList);
        List<MaterialBean> video = (List<MaterialBean>) data.get(0);
        List<MaterialBean> photo = (List<MaterialBean>) data.get(1);
        if (addVideo != null && addVideo.getType() == 2) {
            video.add(0, addVideo);
        }
        if (addImg != null && addImg.size() > 0) {
            photo.addAll(0, addImg);
        }
        return getSplitdata(video, photo, isAddImg);
    }

    private static List<List<MaterialBean>> getVandP(List<MaterialBean> checkList) {
        List<MaterialBean> video = new ArrayList<>();
        List<MaterialBean> photo = new ArrayList<>();
        for (int i = 0; i < checkList.size(); i++) {
            if (checkList.get(i).getType() == 1) {
                photo.add(checkList.get(i));
            } else if (checkList.get(i).getType() == 2) {
                video.add(checkList.get(i));
            }

        }
        List<List<MaterialBean>> list = new ArrayList<>();
        list.add(video);
        list.add(photo);
        return list;
    }

    private static ArrayList<AlbumSplitData> getSplitdata(List<MaterialBean> video, List<MaterialBean> photo, boolean isAddImg) {
        int index = 0;
        int indeximg = 0;
        int count = 0;
        ArrayList<AlbumSplitData> list = new ArrayList<>();
        while (video.size() > index || photo.size() > indeximg) {
            ArrayList<MaterialBean> merge = new ArrayList<>();
            if (video.size() > index) {
                merge.add(video.get(index));
            }
            index++;
            if (photo.size() > indeximg) {
                int last = (indeximg + 9) > photo.size() ? photo.size() : (indeximg + 9);
                if (last < photo.size() && index > video.size()) {
                    last++;
                }
                List<MaterialBean> result = photo.subList(indeximg, last);
                merge.addAll(result);
                indeximg = last;
            }
            if (merge != null && merge.size() < 10 && isAddImg) {
                MaterialBean materialBean = new MaterialBean(null, null, 1, null, null, 4, 0, 0, 0, null,0.0,0.0);
                merge.add(materialBean);
            }
            AlbumSplitData data = new AlbumSplitData(count, merge, 0);
            list.add(data);
            count++;

        }
        return list;
    }

    private static ArrayList<AlbumSplitData> getTimeSplitdata(List<AlbumData> albumData, boolean isAddImg) {
        ArrayList<AlbumSplitData> list = new ArrayList<>();
        for (int i = 0; i < albumData.size(); i++) {
            List data = getVandP(albumData.get(i).getList());
            List<MaterialBean> video = (List<MaterialBean>) data.get(0);
            List<MaterialBean> photo = (List<MaterialBean>) data.get(1);
            int index = 0;
            int indeximg = 0;
            int count = 0;
            while (video.size() > index || photo.size() > indeximg) {
                ArrayList<MaterialBean> merge = new ArrayList<>();
                if (video.size() > index) {
                    merge.add(video.get(index));
                }
                index++;
                if (photo.size() > indeximg) {
                    int last = (indeximg + 9) > photo.size() ? photo.size() : (indeximg + 9);
                    if (last < photo.size() && index > video.size()) {
                        last++;
                    }
                    List<MaterialBean> result = photo.subList(indeximg, last);
                    merge.addAll(result);
                    indeximg = last;
                }
                if (merge != null && merge.size() < 10 && isAddImg) {
                    MaterialBean materialBean = new MaterialBean(null, null, 1, null, null, 4, 0, 0, 0, null,0.0,0.0);
                    merge.add(materialBean);
                }
                AlbumSplitData albumSplitData = new AlbumSplitData(count, merge, merge.get(0).getDate());
                list.add(albumSplitData);
                count++;

            }
        }
        return list;
    }
}