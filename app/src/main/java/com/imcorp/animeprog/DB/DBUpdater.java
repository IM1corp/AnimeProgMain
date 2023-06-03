 package com.imcorp.animeprog.DB;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.imcorp.animeprog.Config;

import org.intellij.lang.annotations.Language;

import java.util.Arrays;
import java.util.LinkedList;

public class DBUpdater {
    public final static int DB_VERSION = 3;
    private final SQLiteDatabase myDb;
    private final LinkedList<String> queries =new LinkedList<>();
    public DBUpdater(SQLiteDatabase myDB) {
        this.myDb = myDB;
    }
    public void updateTo(int version_now) {
        switch (version_now){
            case DB_VERSION:return;
            case -1:
            case 0:
            case 1:
                add(getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_STUDIO,"BLOB"),
                    getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_GENRES,"BLOB"),
                    getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_ISSUE_DATE,"TEXT"),
                    getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_ORIGINAL_SOURCE,"TEXT"),
                    getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_STATUS,"TEXT"),
                    getColumnQ(DataBase.DOWNLOADS_TABLE_NAME,DataBase.DOWNLOADS_ANIME_SYNONYMS,"TEXT")
                        );
            case 2:
                add(getColumnQ(DataBase.DOWNLOADS_EPISODES_TABLE, DataBase.DOWNLOADS_ANIME_SUBTITLES, "TEXT DEFAULT ''"));
        }
        for(String i: queries) {
            try {
                myDb.execSQL(i);
            }catch (Exception e){
                if(Config.NEED_LOG) Log.e(Config.DATABASE_ER_LOG,e.getMessage(),e);
            }

        }
    }
    @Language("RoomSql")
    private String getColumnQ(@Language("RoomSql") final String table, @Language("RoomSql") final String columnName, @Language("RoomSql") final String columnType){
        return "ALTER TABLE "+table+" ADD "+columnName +" "+columnType+";";
    }
    private void add(@Language("RoomSql") String ...q){
        this.queries.addAll(Arrays.asList(q));
    }
}
