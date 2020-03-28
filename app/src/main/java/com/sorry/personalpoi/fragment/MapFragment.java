package com.sorry.personalpoi.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;

import com.sorry.personalpoi.DiyRouteActivity;
import com.sorry.personalpoi.PoiSearchActivity;
import com.sorry.personalpoi.R;
import com.sorry.personalpoi.RouteActivity;
import com.sorry.personalpoi.util.MyOrientationListener;



/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment   {
    //控件
    public MapView mapView; //地图控件
    public Button search,locate,route,myroute;

    //百度api实例
    public BaiduMap baiduMap;
    public UiSettings uiSettings;
    public LocationClient locationClient;
    public MyLocationListener myListener = new MyLocationListener();

    //参数
    public float currentX;
    public double latitude;
    public double longitude;
    public String city;
    public double poilat,poilng;

    //自定义方向传感器实例
    public MyOrientationListener myOrientationListener;

    //定位图层显示方式
    public MyLocationConfiguration.LocationMode locationMode;



    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map,container,false);
        mapView = (MapView) v.findViewById(R.id.bmapView);


        initLocation();//初始化定位
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        search = (Button) getActivity().findViewById(R.id.search);
        locate = (Button) getActivity().findViewById(R.id.getMyLocation);
        route = (Button) getActivity().findViewById(R.id.route);
        myroute = (Button) getActivity().findViewById(R.id.diyroute);

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "重新定位成功", Toast.LENGTH_LONG).show();
                getMyLocation();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PoiSearchActivity.class);
                intent.putExtra("city",city);
                startActivityForResult(intent,1);//1
            }

        });

        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RouteActivity.class);
                intent.putExtra("city",city);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                startActivityForResult(intent,2);//2
            }

        });

        myroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DiyRouteActivity.class);
                intent.putExtra("city",city);
                intent.putExtra("latitude",latitude);
                intent.putExtra("longitude",longitude);
                startActivityForResult(intent,3);//3
            }

        });

    }


    @Override
    public void onStart(){
        super.onStart();
        //开启定位
        baiduMap.setMyLocationEnabled(true);
        if(!locationClient.isStarted())
        {
            locationClient.start();
        }
        myOrientationListener.start();

    }

    @Override
    public void onStop() {
        super.onStop();
        //停止定位
        baiduMap.setMyLocationEnabled(false);
        locationClient.stop();
        myOrientationListener.stop();
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


    //再次使用定位
    public void getMyLocation(){
        baiduMap.clear();
        LatLng latLng = new LatLng(latitude,longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        MapStatusUpdate msuzoom = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMapStatus(msuzoom);
    }

    //定位初始化
    public void initLocation(){
        baiduMap = mapView.getMap();
        //baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setTrafficEnabled(true);//实时路况
        uiSettings = baiduMap.getUiSettings();
        uiSettings.setCompassEnabled(false);//禁用指南针
        uiSettings.setOverlookingGesturesEnabled(false);//禁用俯视
        uiSettings.setRotateGesturesEnabled(false);//禁用旋转

        // 隐藏百度的LOGO
        View child = this.mapView.getChildAt(1);
        //原理也就是在view里面找到一个iamgeview（也就是logo），然后隐藏
        if (child != null && (child instanceof ImageView)) {
            child.setVisibility(View.INVISIBLE);
        }

        locationMode= MyLocationConfiguration.LocationMode.NORMAL;
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        locationClient =new LocationClient(getActivity());
        //注册监听器
        locationClient.registerLocationListener(myListener);
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        LocationClientOption mOption=new LocationClientOption();
        //设置坐标类型
        mOption.setCoorType("bd09ll");
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true);
        //设置是否打开gps进行定位
        mOption.setOpenGps(true);
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        mOption.setScanSpan(1000);
        //设置 LocationClientOption
        locationClient.setLocOption(mOption);

        myOrientationListener=new MyOrientationListener(getActivity());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                currentX =x;
            }
        });

    }

    //获取POI经纬度
    public  void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == 233){
            poilat = data.getDoubleExtra("lat",0.0);
            poilng = data.getDoubleExtra("lng",0.0);
            addPOI();
        }
    }

    //标注POI
    public void addPOI(){
        baiduMap.clear();
        //自定义Markerdian
        LatLng point = new LatLng(poilat,poilng);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.poi);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(poilat,poilng));
        MapStatusUpdate msuzoom = MapStatusUpdateFactory.zoomTo(18.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMapStatus(msuzoom);
    }


    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener extends BDAbstractLocationListener {
        public boolean isFirstLocate = true;
        @Override
        public void onReceiveLocation(BDLocation location) {
            //BDLocation 回调的百度坐标类，内部封装了如经纬度、半径等属性信息
            //MyLocationData 定位数据,定位数据建造器
            /*
             * 可以通过BDLocation配置如下参数
             * 1.accuracy 定位精度
             * 2.latitude 百度纬度坐标
             * 3.longitude 百度经度坐标
             * 4.satellitesNum GPS定位时卫星数目 getSatelliteNumber() gps定位结果时，获取gps锁定用的卫星数
             * 5.speed GPS定位时速度 getSpeed()获取速度，仅gps定位结果时有速度信息，单位公里/小时，默认值0.0f
             * 6.direction GPS定位时方向角度
             *
             *
             * */


            if(location.getLocType() == BDLocation.TypeGpsLocation ||
                    location.getLocType() == BDLocation.TypeNetWorkLocation){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                city = location.getCity();

                //Log.i("site",String.valueOf(mLatitude)+","+String.valueOf(mLongitude));

                MyLocationData data = new MyLocationData.Builder()
                        .direction(currentX)//设定图标方向
                        .accuracy(0)//getRadius 获取定位精度,默认值0.0f
                        .latitude(latitude)//百度纬度坐标
                        .longitude(longitude)//百度经度坐标
                        .build();
                Log.i("currentlocation","方向:"+currentX+" 经纬度:"+longitude+","+latitude);
                //设置定位数据，只有先允许定位图层后设置数据才会生效
                baiduMap.setMyLocationData(data);

                MyLocationConfiguration configuration =
                        new MyLocationConfiguration(locationMode,true,null);
                baiduMap.setMyLocationConfiguration(configuration);
                //判断是否为第一次定位
                if(isFirstLocate){
                    //地理坐标基本数据结构
                    LatLng Latlng = new LatLng(location.getLatitude(),location.getLongitude());
                    //描述地图状态将要发生的变化，通过当前经纬度来使地图显示到该位置
                    MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(Latlng);

                    //改变地图状态
                    baiduMap.setMapStatus(msu);
                    isFirstLocate = false;
                }
            }
        }
    }



}
