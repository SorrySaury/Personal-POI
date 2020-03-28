package com.sorry.personalpoi;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.gyf.immersionbar.ImmersionBar;
import com.next.easynavigation.view.EasyNavigationBar;
import com.sorry.personalpoi.fragment.HomeFragment;
import com.sorry.personalpoi.fragment.MapAlbumFragment;
import com.sorry.personalpoi.fragment.MapFragment;
import com.sorry.personalpoi.fragment.UserFragment;
import com.zinc.libpermission.annotation.Permission;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {
    private EasyNavigationBar navigationBar;
    private String[] tabText = {"焦点", "地图相册","", "地球", "云聊"};
    //未选中icon
    private int[] normalIcon = {R.mipmap.news_default, R.mipmap.map_default, R.mipmap.album_selected,R.mipmap.earth_default, R.mipmap.chat_default};
    //选中时icon
    private int[] selectIcon = {R.mipmap.news_selected, R.mipmap.map_selected, R.mipmap.album_selected,R.mipmap.earth_selected, R.mipmap.chat_selected};

    private List<Fragment> fragments = new ArrayList<>();

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //沉浸式状态栏，深色

        ImmersionBar.with(this)
                    .transparentStatusBar()
                    .statusBarDarkFont(true)
                    .init();
        //导航栏
        getNavigation();

    }


    //导航栏,并动态申请权限
    @Permission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE//读写SD卡
            ,Manifest.permission.ACCESS_FINE_LOCATION}  //GPS定位}//访问网络，POI检索使用
            ,requestCode = 100)
    public void getNavigation(){
        navigationBar = findViewById(R.id.navigationBar);
        fragments.add(new HomeFragment());
        fragments.add(new MapAlbumFragment());
        fragments.add(new MapFragment());
        fragments.add(new UserFragment());

        View view = LayoutInflater.from(this).inflate(R.layout.activity_main, null);

        navigationBar.titleItems(tabText)
                .normalIconItems(normalIcon) //必传  Tab未选中图标集合
                .selectIconItems(selectIcon) //必传  Tab选中图标集合
                .fragmentList(fragments) //必传  fragment集合
                .mode(EasyNavigationBar.MODE_ADD)//默认MODE_NORMAL 普通模式  //MODE_ADD 带加号模式
                .fragmentManager(getSupportFragmentManager())////必传
            //  .canScroll(true)//Viewpager能否左右滑动
                .addAsFragment(false)
                .addIconSize(50) //中间add图片大小
                .onTabClickListener(new EasyNavigationBar.OnTabClickListener() {
                    @Override
                    public boolean onTabClickEvent(View view, int position) {
                        Log.e("Tap->Position", position + "");
                        if (position == 2) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //跳转至AlbumActivity
                                        Intent intent = new Intent(MainActivity.this,AlbumActivity.class);
                                        startActivityForResult(intent,1);

                                }
                            });
                        }
                        return false;
                    }
                })
                .build();

    }


    public EasyNavigationBar getNavigationBar() {
        return navigationBar;
    }

}
