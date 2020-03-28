package com.sorry.personalpoi.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.clusterutil.clustering.view.PersonRenderer;
import com.baidu.mapapi.clusterutil.projection.Point;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.sorry.personalpoi.R;
import com.sorry.personalpoi.bean.LocalPictrue;
import com.sorry.personalpoi.util.CoordinateTransformUtil;
import com.sorry.personalpoi.util.SdCardFileTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.baidu.mapapi.clusterutil.clustering.ClusterManager.MAX_DISTANCE_AT_ZOOM;
import static com.baidu.mapapi.clusterutil.clustering.algo.NonHierarchicalDistanceBasedAlgorithm.PROJECTION;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapAlbumFragment extends Fragment implements BaiduMap.OnMapLoadedCallback {


    public MapView mapView;
    volatile BaiduMap baiduMap;
    public MapStatus ms;
    private ClusterManager<LocalPictrue> clusterManager;

    public MapAlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_album, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLocation();
        initMap();


        // 定义点聚合管理类ClusterManager
        clusterManager = new ClusterManager<LocalPictrue>(getActivity(), baiduMap);
        clusterManager.setRenderer(new PersonRenderer(getActivity(),baiduMap,clusterManager));

        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        baiduMap.setOnMapStatusChangeListener(clusterManager);
        // 设置maker点击时的响应
        baiduMap.setOnMarkerClickListener(clusterManager);


        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<LocalPictrue>() {
            @Override
            public boolean onClusterClick(Cluster<LocalPictrue> cluster) {
                Toast.makeText(getActivity(),
                        "有" + cluster.getSize() + "个点", Toast.LENGTH_SHORT).show();

                return false;
            }
        });
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<LocalPictrue>() {
            @Override
            public boolean onClusterItemClick(LocalPictrue item) {
                Toast.makeText(getActivity(),
                        "点击单个Item", Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        //移动的回调
        clusterManager.setOnMapStatusChangeFinishListener(new ClusterManager.OnMapStatusChangeFinish() {
            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                markerGetAndSet(mapStatus.zoom,mapStatus.bound);
            }
        });


    }

    //获取屏幕上的点，并且开始计算以及显示
    public void markerGetAndSet(final float zoom, final LatLngBounds visibleBounds) {

        Observable.fromCallable(new Callable<ArrayList<LocalPictrue>>() {

            @Override
            public ArrayList<LocalPictrue> call() {

                final double zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / Math.pow(2, zoom) / 256;
                //加大搜索的范围 ，重新计算出新的边界 Bounds
                final double halfZoomSpecificSpan = zoomSpecificSpan * 2; //一倍边长的长度（屏幕上的）
                Point northeastP = PROJECTION.toPoint(visibleBounds.northeast); //右上角
                Point southwestP = PROJECTION.toPoint(visibleBounds.southwest); //左下角

                //莫斯托投影，y值越小，纬度越大，x值越大经度越小
                northeastP = new Point(northeastP.x + halfZoomSpecificSpan, northeastP.y - halfZoomSpecificSpan);
                southwestP = new Point(southwestP.x - halfZoomSpecificSpan, southwestP.y + halfZoomSpecificSpan);

                LatLng northeast = PROJECTION.toLatLng(northeastP);
                LatLng southwest = PROJECTION.toLatLng(southwestP);
                LatLngBounds expandVisibleBounds = new LatLngBounds.Builder()
                        .include(northeast).include(southwest).build();

                //右上角的经纬度 wgs 格式
                double [] wgs_northeast = CoordinateTransformUtil.bd09towgs84( expandVisibleBounds.northeast.longitude,
                        expandVisibleBounds.northeast.latitude);
                //左下角的经纬度 WGS 格式，
                double [] wgs_southwest = CoordinateTransformUtil.bd09towgs84( expandVisibleBounds.southwest.longitude,
                        expandVisibleBounds.southwest.latitude);


                //传下去JNI 获取 区域内的相片

                //获取当前屏幕的点，目前是获取sdcard相册的点
                return SdCardFileTool.getPhotoData(getActivity());
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<LocalPictrue>>() {

                    @Override
                    public void call(ArrayList<LocalPictrue> localPictrues) {
                        clusterManager.clearItems();
                        clusterManager.addItems(localPictrues);
                        //算法计算聚合，并显示
                        clusterManager.cluster(zoom,visibleBounds);
                    }
                });
    }

    /**
     * 初始化地图
     */
    public void initMap() {
        mapView = (MapView) getActivity().findViewById(R.id.bmapView);
        mapView.setLogoPosition(LogoPosition.logoPostionRightBottom);
        baiduMap = mapView.getMap();
        baiduMap.setMaxAndMinZoomLevel(21, 4);
        baiduMap.setOnMapLoadedCallback(this);

        baiduMap.getUiSettings().setOverlookingGesturesEnabled(false);
        baiduMap.getUiSettings().setRotateGesturesEnabled(false);
    }


    /**
     * 向地图添加Marker点
     */
    public void initLocation (){
        // 添加Marker点


        Observable.fromCallable(new Callable<List<LocalPictrue>>() {

            @Override
            public List<LocalPictrue> call() {

                return SdCardFileTool.getPhotoData(getActivity());

            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<List<LocalPictrue>>() {

                    @Override
                    public void call(List<LocalPictrue> localPictrues) {
//                        clusterManager.addItems(localPictrues);
                        ms = new MapStatus.Builder().target(localPictrues.get(0).getPosition()).zoom(8).build();
                        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));

                    }
                });


    }



    @Override
    public void onStart(){
        super.onStart();
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }

    @Override
    public void onMapLoaded() {

    }
}
