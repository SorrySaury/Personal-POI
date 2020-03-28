package com.sorry.personalpoi.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class MySQLiteHelper extends SQLiteOpenHelper {

    //抽象类必须调用父类方法
    public MySQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //调用父类构造函数
        super(context, getMyDatabaseName(name), factory, version);
    }

    private static String getMyDatabaseName(String name){
        String databasename = name;
        boolean isSdcardEnable = false;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){//SDCard是否插入
            isSdcardEnable = true;
        }
        String dbPath = null;
        if(isSdcardEnable){
            dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sorry/database/";
        }else{//未插入SDCard，建在内存中

        }
        File dbp = new File(dbPath);
        if(!dbp.exists()){
            dbp.mkdirs();
        }
        databasename = dbPath + databasename;
        return databasename;
    }
    /**
     * 当数据库首次创建时执行该方法，一般将创建表等初始化操作放在该方法中执行.
     * 重写onCreate方法，调用execSQL方法创建表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("SWORD", "create a Database");
        //创建数据库sql语句
        //边表
        String sql1 = "create table Side" +
                "(prenode_id integer not null,nextnode_id integer not null,value integer not null)";
        //点表
        String sql2 = "create table Node" +
                "(node_id integer primary key ,name varchar ,longitude varchar not null,latitude varchar not null,area integer not null)";
        //最短路径集表
        String sql3 = "create table Paths" +
                "(path_id integer primary key ,start_id integer not null,end_id integer not null,path varchar not null,value integer not null,area integer not null)";
        //关键结点表
        String sql4 = "create table Linknode" +
                "(lnode_id integer primary key,prearea_id integer not null,nextarea_id integer not null,area integer not null)";
        //区域表
        String sql5 = "create table Area" +
                "(area_id integer primary key,name varchar not null)";

        //执行创建数据库操作
        db.execSQL(sql1);
        db.execSQL(sql2);
        db.execSQL(sql3);
        db.execSQL(sql4);
        db.execSQL(sql5);
    }

    @Override
    //当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
