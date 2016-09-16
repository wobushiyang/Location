package com.app.trackline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by ASUS-PC on 2016/8/20.
 */
public class DatabaseAdapter {

    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        dbHelper=new DatabaseHelper(context);
    }

    //添加线路跟踪
    public int addTrack(Track track){

        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(DatabaseHelper.TRACK_NAME,track.getTrack_name());
        contentValues.put(DatabaseHelper.CREATE_DATE,track.getCreate_date());
        contentValues.put(DatabaseHelper.START_LOC,track.getStart_loc());
        contentValues.put(DatabaseHelper.END_LOC,track.getEnd_loc());
        long id=db.insertOrThrow(DatabaseHelper.TABLE_TRACK,null,contentValues);
        db.close();
        return (int) id;

    }

    //查询所有线路
    public ArrayList<Track> getTracks(){
        String sql="select _id,track_name,create_date,start_loc,end_loc from track";
        ArrayList<Track> tracks=new ArrayList<>();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor c=db.rawQuery(sql,null);
        Track t=null;
        if(null!=c){
            while (c.moveToNext()){
                t=new Track(c.getInt(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4));
                tracks.add(t);
            }
            c.close();
        }
        db.close();
        return tracks;
    }


    //更新终点位置，刚开始的时候走的时候，终点位置就是起点位置
    public void updateEndLoc(String endLoc,int id){
        String sql="update track set end_loc=? where _id=?";
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        db.execSQL(sql,new Object[]{endLoc,id});
        db.close();
    }

    //添路线明细
    public void addTrackDetail(int tid,double lat,double lng){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql="insert into track_detail(tid,lat,lng) values(?,?,?)";
        db.execSQL(sql,new Object[]{tid,lat,lng});
        db.close();
    }

    //根据线路ID查询线路跟踪点
    public ArrayList<TrackDetail> getTrackDetails(int id){
        String sql="select _id,lat,lng from track_detail where tid=? order by _id desc";
        ArrayList<TrackDetail> list=new ArrayList<>();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor c=db.rawQuery(sql,new String[]{String.valueOf(id)});
        if(null!=c){
            TrackDetail detail=null;
            while (c.moveToNext()){
                detail=new TrackDetail(c.getInt(0),c.getDouble(1),c.getDouble(2));
                list.add(detail);
            }
            c.close();
        }
        return list;
    }



    //删除所有的线路
    public void delTrack(int id){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        String sql1="delete from track where _id=?";
        String sql2="delete from track_detail where tid=?";

        db.beginTransaction();
        db.execSQL(sql1,new Object[]{id});
        db.execSQL(sql2,new Object[]{id});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }
}
