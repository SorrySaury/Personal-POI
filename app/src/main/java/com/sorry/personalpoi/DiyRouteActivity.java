package com.sorry.personalpoi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.gyf.immersionbar.ImmersionBar;
import com.sorry.personalpoi.util.MySQLiteHelper;
import com.sorry.personalpoi.view.ClearEditText;

import java.util.ArrayList;
import java.util.List;

public class DiyRouteActivity extends AppCompatActivity implements View.OnClickListener{


    //参数
    public String city,from,to,nfrom,nto;
    public double latitude,longitude;

    //百度控件
    public BaiduMap baiduMap;
    public MapView mapView; //地图控件
    public UiSettings uiSettings;

    //自定义控件
    public ClearEditText efrom,eto;
    public Button search;

    //数据库
    public MySQLiteHelper mySQLiteHelper;
    public SQLiteDatabase db;

    //路径规划相关
    public String path=null;
    public ArrayList<LatLng> nodelatlng = new ArrayList<LatLng>();
    public ArrayList<int[][]> areagraph = new ArrayList<int[][]>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_route);

        //沉浸式状态栏
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init();
        parseIntent();
        //初始化地图
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        initView();
        mySQLiteHelper = new MySQLiteHelper(this,"map.db",null,1);
        db = mySQLiteHelper.getWritableDatabase();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }



    public void parseIntent(){
        Intent frontintent = getIntent();
        city = frontintent.getStringExtra("city");
        latitude = frontintent.getDoubleExtra("latitude",0.0);
        longitude = frontintent.getDoubleExtra("longitude",0.0);
    }

    public void initView(){
        baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        LatLng latLng = new LatLng(latitude,longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        MapStatusUpdate msuzoom = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMapStatus(msuzoom);

        //初始化控件
        efrom = (ClearEditText) findViewById(R.id.from);
        eto = (ClearEditText) findViewById(R.id.to);
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(this);



    }

    public void getPath(){
        nodelatlng.clear();
        baiduMap.clear();
        Cursor cr = db.rawQuery("select * from Node where name=?",new String[]{from});
        while(cr.moveToNext()){
            nfrom = cr.getString(0);
            Log.i("naddress",nfrom);
        }

        cr.close();
        Cursor cr1 = db.rawQuery("select * from Node where name=?",new String[]{to});
        while(cr1.moveToNext()){
            nto = cr1.getString(0);
            Log.i("naddress",nto);
        }

        cr1.close();

        Cursor cr2 = db.rawQuery("select * from Paths where start_id=? and end_id=?",new String[]{nfrom,nto});
        while(cr2.moveToNext()){
            path = cr2.getString(3);
        }
        cr2.close();
        String[] sresult = path.split("\\,");
        for(String s:sresult){
            Cursor cursor = db.rawQuery("select * from Node where node_id=?",new String[]{s});
            while(cursor.moveToNext()){
                LatLng temp = new LatLng(Double.parseDouble(cursor.getString(3)),Double.parseDouble(cursor.getString(2)));
                nodelatlng.add(temp);
            }
        }
        for(LatLng l:nodelatlng){
            Log.i("latlng",l.toString());
        }

        List<OverlayOptions> options = new ArrayList<OverlayOptions>();

        //自定义起止点icon
        BitmapDescriptor startico = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_st);
        BitmapDescriptor endico = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_en);
        //自定义纹理
        BitmapDescriptor mBlueTexture = BitmapDescriptorFactory.fromResource(R.drawable.road);

        //起点
        OverlayOptions soption = new MarkerOptions()
                .position(nodelatlng.get(0))
                .icon(startico);
        baiduMap.addOverlay(soption);

        //终点
        OverlayOptions eoption = new MarkerOptions()
                .position(nodelatlng.get(nodelatlng.size()-1))
                .icon(endico);
        baiduMap.addOverlay(eoption);

        //折线
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(30)
                .customTexture(mBlueTexture)
                .points(nodelatlng);
        options.add(soption);
        options.add(eoption);
        options.add(mOverlayOptions);

        baiduMap.addOverlays(options);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(nodelatlng.get(0).latitude,nodelatlng.get(0).longitude));
        MapStatusUpdate msuzoom = MapStatusUpdateFactory.zoomTo(18.0f);
        baiduMap.setMapStatus(msu);
        baiduMap.setMapStatus(msuzoom);



    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        from = efrom.getText().toString();
        to = eto.getText().toString();
        Log.i("addressinfo",from+"---->"+to);
        if(i == R.id.search){
            getPath();
        }
    }


}
