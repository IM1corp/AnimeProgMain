package com.imcorp.animeprog.DB.local;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.Objects.ProgressRow;
import com.imcorp.animeprog.DB.SimpleDB;

import static com.imcorp.animeprog.DB.DataBase.PROGRESS_ANIME_ID;
import static com.imcorp.animeprog.DB.DataBase.PROGRESS_ANIME_PATH;
import static com.imcorp.animeprog.DB.DataBase.PROGRESS_NUM_WATCHED;
import static com.imcorp.animeprog.DB.DataBase.PROGRESS_PROGRESS;
import static com.imcorp.animeprog.DB.DataBase.PROGRESS_STATE;
import static com.imcorp.animeprog.DB.DataBase.PROGRESS_TABLE;

public class Progress implements SimpleDB {
    private final DataBase dataBase;
    public Progress(DataBase dataBase){
        this.dataBase = dataBase;
    }
    @Nullable
    @Override
    public String[] getInitQuery() {
        return new String[]{"CREATE TABLE IF NOT EXISTS "+PROGRESS_TABLE+" (" +
                PROGRESS_ANIME_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROGRESS_ANIME_PATH+" TEXT NOT NULL, " +
                PROGRESS_STATE + " INTEGER NOT NULL, " +
                PROGRESS_NUM_WATCHED+" TEXT NOT NULL, "+
                PROGRESS_PROGRESS+" FLOAT NOT NULL" +
                ");"};
    }
    public ProgressRow getProgress(final String path) {
        Cursor c = null;
        try {
            final ProgressRow progressRow;
            c = dataBase.getMyDB().query(PROGRESS_TABLE, null, PROGRESS_ANIME_PATH + "=?", new String[]{path}, null, null, null);
            if (!c.moveToNext()) {
                progressRow = ProgressRow.getDefault(path);
                final int id = this.updateRow(progressRow);
                if (id != -1) progressRow.id = id;
            } else {
                progressRow = new ProgressRow(
                        path,
                        WatchState.values()[c.getInt(c.getColumnIndexOrThrow(PROGRESS_STATE))],
                        c.getString(c.getColumnIndexOrThrow(PROGRESS_NUM_WATCHED)),
                        c.getFloat(c.getColumnIndexOrThrow(PROGRESS_PROGRESS)),
                        c.getInt(c.getColumnIndexOrThrow(PROGRESS_ANIME_ID))
                );
            }
            return progressRow;
        } finally {
            if (c != null) c.close();
        }
    }
    public int updateRow(ProgressRow progress_row) {
        ContentValues row = new ContentValues();
        row.put(PROGRESS_PROGRESS,progress_row.progress);
        row.put(PROGRESS_NUM_WATCHED, progress_row.getNumWatchedFormated());
        row.put(PROGRESS_STATE, progress_row.watchState.ordinal());
        row.put(PROGRESS_ANIME_PATH, progress_row.path);
        if(progress_row.id!=null){
            dataBase.getMyDB().update(PROGRESS_TABLE,row,PROGRESS_ANIME_ID+"="+progress_row.id,null);
        } else {
            return (int)dataBase.getMyDB().insert(PROGRESS_TABLE,null,row);
        }
        return -1;
    }

    public static enum WatchState {
        WATCHING,
        ENDED,
        UNDEFINED
    }
}
