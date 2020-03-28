package com.sorry.personalpoi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.gyf.immersionbar.ImmersionBar;
import com.sorry.personalpoi.view.ClearEditText;

import java.util.ArrayList;
import java.util.List;

public class PoiSearchActivity extends AppCompatActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener,View.OnClickListener {
    public String city;

    //结果集
    public ArrayList<String> addrs = new ArrayList<String>();
    public ArrayList<String> names = new ArrayList<String>();
    public ArrayList<LatLng> latlngs = new ArrayList<LatLng>();

    public ClearEditText keyword;
    public ListView poilist;
    public ArrayAdapter<String> sugAdapter;

    public PoiSearch poiSearch;
    public SuggestionSearch suggestionSearch;

    public Button Back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisearch);
        //沉浸式状态栏
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init();

        parseIntent();


        initSearch();

    }


    //解析intent
    public void parseIntent(){
        Intent intent = getIntent();
        city = intent.getStringExtra("city");
    }
    public void initSearch(){

        //返回键监听
        Back = (Button) findViewById (R.id.back);
        Back.setOnClickListener(this);

        // 初始化搜索模块，注册搜索事件监听
        poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        suggestionSearch = SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(this);

        keyword = (ClearEditText) findViewById(R.id.keyword);
        poilist = (ListView) findViewById(R.id.results);

        //搜索实例adapter
        sugAdapter = new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line);
        keyword.setAdapter(sugAdapter);
        keyword.setThreshold(1);

        //当输入关键字变化时，动态更新建议列表
        keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() <= 0){
                    return;
                }
                suggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(s.toString())
                        .city(city));

                citySearch(0,keyword.getText().toString(),40);
            }
        });
    }

    public void citySearch(int page,String keyword,int pageCapacity){
        if(city!=null){
            //设置检索参数
            PoiCitySearchOption citySearchOption = new PoiCitySearchOption();
            citySearchOption.city(city);
            citySearchOption.keyword(keyword);
            citySearchOption.pageCapacity(pageCapacity);
            citySearchOption.pageNum(page);
            //发送检索请求
            poiSearch.searchInCity(citySearchOption);
        }
    }



    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if(poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
            //Toast.makeText(PoiSearchActivity.this,"未找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        if(poiResult.error == SearchResult.ERRORNO.NO_ERROR){
            if(addrs!=null){addrs.clear();}
            if(latlngs!=null){latlngs.clear();}
            if(names!=null){names.clear();}
            if(poiResult.getAllPoi()!=null&&poiResult.getAllPoi().size()>0){
                List<PoiInfo> poiinfo = poiResult.getAllPoi();
                for(PoiInfo p:poiinfo){
                    names.add(p.name);
                    addrs.add(p.address);
                    latlngs.add(p.location);
                    Log.i("mlocation",p.name+"----------"+p.location+"");
                    SearchListAdapter searchListAdapter = new SearchListAdapter();
                    poilist.setAdapter(searchListAdapter);

                }
            }


        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.back){
            Intent intent = new Intent();
            PoiSearchActivity.this.setResult(1,intent);
            PoiSearchActivity.this.finish();
        }
    }

    public class SearchListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return names.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView ==null){
                convertView = LayoutInflater.from(
                        getApplicationContext()).inflate(R.layout.item_poi,parent,false);
                holder = new ViewHolder();
                holder.poiname = (TextView)convertView.findViewById(R.id.name);
                holder.poiaddr = (TextView)convertView.findViewById(R.id.address);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.poiname.setText(names.get(position));
            holder.poiaddr.setText(addrs.get(position));


            holder.poiname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    Intent intent = new Intent();
                    intent.putExtra("placename",names.get(position));
                    intent.putExtra("lat",latlngs.get(position).latitude);
                    intent.putExtra("lng",latlngs.get(position).longitude);

                    PoiSearchActivity.this.setResult(233,intent);
                    PoiSearchActivity.this.finish();
                }

            });

            holder.poiaddr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    Intent intent = new Intent();
                    intent.putExtra("placename",names.get(position));
                    intent.putExtra("lat",latlngs.get(position).latitude);
                    intent.putExtra("lng",latlngs.get(position).longitude);
                    PoiSearchActivity.this.setResult(233,intent);
                    PoiSearchActivity.this.finish();
                }

            });


            return convertView;
        }
        class ViewHolder{
            TextView poiname;
            TextView poiaddr;
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {

    }
}
