package com.imcorp.animeprog.DB.PathLoader;

import static com.imcorp.animeprog.DB.DataBase.CACHE_DATE;
import static com.imcorp.animeprog.DB.DataBase.CACHE_FILE_NAME;
import static com.imcorp.animeprog.DB.DataBase.CACHE_TABLE_NAME;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.SimpleDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Locale;

public class Cache implements SimpleDB {
    private final static String LOG_TAG = "ImageCache";
    private final DataBase dataBase;
    public Cache(DataBase dataBase){
        this.dataBase = dataBase;
    }
    private File getCachePath() {
        return dataBase.context.getCacheDir();
    }
    public void deleteCache(){
        deleteCache(false);
    }
    public void deleteCache(final boolean forceDelete) {
        try {
            final File cache_dir = getCachePath();
            final LinkedList<CacheRow> history = getCacheHistory();
            CacheRow c=null;
            while (history.size() > (forceDelete?0:dataBase.settings.getMaxCacheFilesCount())) {
                if((c = history.pollFirst())==null)break;
                final File f = new File(cache_dir,c.md5);
                if(f.isFile() && !f.delete())
                    if(Config.NEED_LOG) Log.e(LOG_TAG,"Can not delete file" +f.getAbsolutePath());
            }
            if(c!=null)
            this.deleteHistory(c.date);
        } catch (Exception e) {
            if(Config.NEED_LOG) Log.e(Config.LOG_TAG, e.getMessage(), e);
        }
    }
    public void saveImgToCache(String url, Bitmap bitmap){
        final String md5 = Config.md5(url);
        File f = new File(getCachePath(),md5);
        try {
            saveBitmapToFile(bitmap,f);
        } catch (IOException ignored) {}
        this.insertRow(md5);
    }

    private void insertRow(String md5) {
        try {
            dataBase.getMyDB().beginTransaction();
            ContentValues row1 = new ContentValues(2);

            row1.put(CACHE_FILE_NAME,md5);
            row1.put(CACHE_DATE,System.currentTimeMillis());

            final long res = dataBase.getMyDB().insertWithOnConflict(CACHE_TABLE_NAME, CACHE_FILE_NAME, row1, SQLiteDatabase.CONFLICT_REPLACE);
            dataBase.getMyDB().setTransactionSuccessful();
        }
        finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    private LinkedList<CacheRow> getCacheHistory(){
        dataBase.getMyDB().beginTransaction();
        final LinkedList<CacheRow> answer = new LinkedList<CacheRow>();
        try(final Cursor cursor = dataBase.getMyDB().query(CACHE_TABLE_NAME,
                null,null,null,null,null, CACHE_DATE+" ASC")) {
            while(cursor.moveToNext()){
                answer.add(new CacheRow(
                            cursor.getString(cursor.getColumnIndexOrThrow(CACHE_FILE_NAME)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(CACHE_DATE))
                        ));
            }
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
        return answer;
    }
    private void deleteHistory(long maxDate){
        try{
            dataBase.getMyDB().beginTransaction();
            dataBase.getMyDB().delete(CACHE_TABLE_NAME,CACHE_DATE+"<="+maxDate,null);
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }

    public String getCacheSize(){
        long length=0;
        File[] files = getCachePath().listFiles();
        if (files != null) for (File file : files) {
            if (file.isFile()) length += file.length();
        }
        final float kb = (length/1024f);
        final boolean showKB =  kb < 1024;
        return String.format(Locale.getDefault(),"%.2f"+(showKB?"KB":"MB"), showKB? kb : kb / 1024f);
    }
    public int getCacheFilesCount(){
        File[] files = getCachePath().listFiles();
        return files!=null?files.length:0;
    }
    @Nullable public String tryGetImgFromCache(String url){
        if(url==null)return null;
        final String md5 = Config.md5(url);
        File img_f = new File(getCachePath(),md5);
        if(img_f.exists()){
            if(Config.NEED_LOG) Log.i(LOG_TAG,"Getting cached image at "+url);
            this.insertRow(md5);
            return img_f.getPath();
        }
        return null;
    }
    void saveBitmapToFile(Bitmap bitmap,File file) throws IOException {
        FileOutputStream f = null;
        try {
            if(!file.createNewFile())return;
            f = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, f);
        }finally {
            if(f!=null) try {f.close();} catch (IOException ignored) { }
        }
    }

    @Nullable @Override public String[] getInitQuery() {
        return new String[]{"CREATE TABLE IF NOT EXISTS "+
                CACHE_TABLE_NAME+
                " ("+ CACHE_DATE+ " INTEGER NOT NULL, "+
                CACHE_FILE_NAME+ " TEXT PRIMARY KEY NOT NULL);"};
    }
    private static class CacheRow{
        public final long date;
        public final String md5;
        CacheRow(String md5, long date){
            this.md5 = md5;
            this.date = date;
        }
    }
}
