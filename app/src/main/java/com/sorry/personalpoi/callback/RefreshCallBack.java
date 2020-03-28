package com.sorry.personalpoi.callback;

import com.sorry.personalpoi.bean.MaterialBean;

/**
 * @anthor sorry
 * @time 2019/5/17
 * @class 预览页图片，视频选择回调
 */
public interface RefreshCallBack {
    void checkNews(MaterialBean media, boolean type, String name);
}