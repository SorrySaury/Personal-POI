package com.sorry.personalpoi.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;


/**
 * @anthor sorry
 * @time 2019/5/17
 * @class GridLayoutManager重写处理滑动冲突以及嵌套显示不完全问题
 */
public class AutoGridLayoutManager extends GridLayoutManager {
    private boolean isScrollEnabled = true;

    public AutoGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }

}