package com.imcorp.animeprog.DB.local;

import android.content.ContentValues;
import android.database.Cursor;

import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.SimpleDB;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static com.imcorp.animeprog.DB.DataBase.HISTORY_DATE_NAME;
import static com.imcorp.animeprog.DB.DataBase.HISTORY_HOST;
import static com.imcorp.animeprog.DB.DataBase.HISTORY_PATH_NAME;
import static com.imcorp.animeprog.DB.DataBase.HISTORY_TABLE_NAME;
import static com.imcorp.animeprog.DB.DataBase.HISTORY_TITLE_NAME;

public class History implements SimpleDB {
    private final DataBase db;
    public History(DataBase db){
        this.db=db;
    }
    public ArrayList<Map.Entry<Date, OneAnime>> getHistory(final int count, final int offset){

        ArrayList<Map.Entry<Date, OneAnime>> answer = new ArrayList<>();
        try {
            db.getMyDB().beginTransaction();
            try (Cursor c = db.getMyDB().query(HISTORY_TABLE_NAME, null,
                    null, null, null, null, HISTORY_DATE_NAME + " DESC",
                    offset + "," + count)) {
                //"SELECT * FROM " + HISTORY_TABLE_NAME + " OFFSET " + offset + " LIMIT " + count, null);
                while (c.moveToNext()) {
                    long date = c.getLong(c.getColumnIndexOrThrow(HISTORY_DATE_NAME));

                    //Calendar calendar = Calendar.getInstance();
                    //calendar.setTimeInMillis(date);
                    Date date_date = new Date(date);
                    OneAnime anime = new OneAnime((byte) c.getInt(c.getColumnIndexOrThrow(HISTORY_HOST)));
                    anime.setPath(c.getString(c.getColumnIndexOrThrow(HISTORY_PATH_NAME)));
                    anime.title = c.getString(c.getColumnIndexOrThrow(HISTORY_TITLE_NAME));
                    answer.add(new AbstractMap.SimpleEntry<>(date_date, anime));
                }
            }
            db.getMyDB().setTransactionSuccessful();
        }finally {
            if(db.getMyDB().isOpen() && db.getMyDB().inTransaction())
                db.getMyDB().endTransaction();
        }

        return answer;
    }
    public int getCount(){
        try(Cursor c = db.getMyDB().query(HISTORY_TABLE_NAME,new String[]{"COUNT(*)"},null,null,null,null,null)){
            c.moveToNext();
            return c.getInt(0);
        }
    }
    public int deleteHistoryRow(long dateCreated){
        return db.getMyDB().delete(HISTORY_TABLE_NAME,HISTORY_DATE_NAME+"="+dateCreated,null);
    }
    public void deleteAllHistory(){
        db.getMyDB().delete(HISTORY_TABLE_NAME,null,null);
    }
    public void addToHistory(OneAnime object){
        ArrayList<Map.Entry<Date, OneAnime>> map = this.getHistory(1,0);
        final ContentValues row=new ContentValues();
        row.put(HISTORY_DATE_NAME, System.currentTimeMillis());
        if(map.size()==1&& map.get(0).getValue().equals(object))
            db.getMyDB().update(HISTORY_TABLE_NAME,row,HISTORY_PATH_NAME+"=?",new String[]{object.getPath()});
        else {
            row.put(HISTORY_TITLE_NAME, object.title);
            row.put(HISTORY_HOST, object.HOST);
            row.put(HISTORY_PATH_NAME, object.getPath());
            db.getMyDB().insert(HISTORY_TABLE_NAME, null, row);
        }
    }
    public String[] getInitQuery() {
        return new String[]{"CREATE TABLE IF NOT EXISTS "+
                HISTORY_TABLE_NAME+
                " ("+ HISTORY_DATE_NAME+ " INTEGER NOT NULL, "+
                HISTORY_HOST+" INTEGER NOT NULL, "+
                HISTORY_PATH_NAME+" TEXT NOT NULL, "+
                HISTORY_TITLE_NAME+" TEXT NOT NULL);"};
    }
}
