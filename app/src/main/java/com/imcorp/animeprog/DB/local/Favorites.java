package com.imcorp.animeprog.DB.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.SimpleDB;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;

import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_DESCRIPTION;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_HOST;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_ID;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_PATH;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_TITLE;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_TYPE;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_ANIME_YEAR;
import static com.imcorp.animeprog.DB.DataBase.FAVORITES_TABLE_NAME;

public class Favorites implements SimpleDB {
    private final DataBase dataBase;
    public Favorites(DataBase dataBase) {
        this.dataBase = dataBase;
    }
    @SuppressWarnings({"unused"})
    @NotNull
    public ArrayList<OneAnime.OneAnimeWithId> getFavorites(int count, final int offset, FavoritesType type){
        ArrayList<OneAnime.OneAnimeWithId> answer;
        try (final Cursor c = this.dataBase.getMyDB().query(
                FAVORITES_TABLE_NAME,null,
                FAVORITES_ANIME_TYPE+"=?",new String[]{String.valueOf(type.ordinal())},null,null,
                FAVORITES_ANIME_ID+" DESC")){
            if(offset>0)c.moveToPosition(offset-1);
            answer = new ArrayList<>(c.getCount());
            while (c.moveToNext()&&count-->0)
                answer.add(getFromCursor(c));
        }
        return answer;
    }
    private OneAnime.OneAnimeWithId getFromCursor(Cursor c){
        final int id = c.getInt(c.getColumnIndexOrThrow(FAVORITES_ANIME_ID));
        OneAnime anime = new OneAnime((byte)c.getInt(c.getColumnIndexOrThrow(FAVORITES_ANIME_HOST)));
        anime.setPath( c.getString(c.getColumnIndexOrThrow(FAVORITES_ANIME_PATH)) );
        dataBase.loader.loadCover(anime);
        anime.description = c.getString(c.getColumnIndexOrThrow(FAVORITES_ANIME_DESCRIPTION));
        anime.title = c.getString(c.getColumnIndexOrThrow(FAVORITES_ANIME_TITLE));
        anime.year = c.getInt(c.getColumnIndexOrThrow(FAVORITES_ANIME_YEAR));
        final OneAnime.OneAnimeWithId animeWithId = new OneAnime.OneAnimeWithId(id,anime);
        animeWithId.type = FavoritesType.values()[c.getInt(c.getColumnIndexOrThrow(FAVORITES_ANIME_TYPE))];
        return animeWithId;
    }
    @SuppressWarnings("UnusedReturnValue")
    public long addToFavorites(OneAnime anime, @Nullable Integer id,FavoritesType type){
        try {
            dataBase.getMyDB().beginTransaction();
            ContentValues row1 = new ContentValues(8);
            row1.put(FAVORITES_ANIME_HOST, anime.HOST);
            row1.put(FAVORITES_ANIME_PATH, anime.getPath());
            row1.put(FAVORITES_ANIME_YEAR, anime.year);
            row1.put(FAVORITES_ANIME_TITLE, anime.title);
            row1.put(FAVORITES_ANIME_DESCRIPTION, anime.description);
            row1.put(FAVORITES_ANIME_TYPE, type.ordinal());
            if (id != null) {
                row1.put(FAVORITES_ANIME_ID, id);
            }
            final OneAnime.OneAnimeWithId animeWithId = this.isFavorite(anime.getPath(), anime.HOST);
            if (animeWithId != null) {
                row1.put(FAVORITES_ANIME_ID, animeWithId.id);
            }
            final long res = dataBase.getMyDB().replace(FAVORITES_TABLE_NAME, null, row1);

            this.dataBase.loader.saveCover(anime);
            dataBase.getMyDB().setTransactionSuccessful();
            return res;
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public long addToFavorites(OneAnime anime,FavoritesType type){
        return addToFavorites(anime,null,type);
    }
    public long addToFavorites(OneAnime.OneAnimeWithId item) {
        return addToFavorites(item.anime,item.id,item.type);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ArrayList<OneAnime.OneAnimeWithId> search(String query,int count){
        try(final Cursor c =
                    this.dataBase.getMyDB().query(FAVORITES_TABLE_NAME,null,
                            FAVORITES_ANIME_TITLE+" LIKE ?",new String[]{"%"+query.trim()+"%"},
                            null,null,null,String.valueOf(count))) {
            final ArrayList<OneAnime.OneAnimeWithId> ans = new ArrayList<>(Math.max(count, c.getCount()));
            while (c.moveToNext()) ans.add(this.getFromCursor(c));

            return ans;
        }
    }
    public void deleteFromFavorites(String path,int HOST){
        try {
            dataBase.getMyDB().beginTransaction();
            @Language("RoomSql") final String query = FAVORITES_ANIME_PATH + " = ? and " + FAVORITES_ANIME_HOST + " = ?";
            this.dataBase.getMyDB().delete(FAVORITES_TABLE_NAME, query, new String[]{path, String.valueOf(HOST)});
            if (this.dataBase.downloads.getFromDownloads(path) == null) {
                dataBase.loader.removeCover(path);
            }
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen()&&dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public void moveFavoritesItems(int from_id, int to_id){
        if(from_id==to_id)return;
        try {
            dataBase.getMyDB().beginTransaction();
            @Language("RoomSql") final String query1, query2 = "UPDATE " + FAVORITES_TABLE_NAME + " SET " + FAVORITES_ANIME_ID + " = -" + FAVORITES_ANIME_ID + " WHERE " + FAVORITES_ANIME_ID + " < 0";
            if (from_id > to_id) {
                query1 = "UPDATE " + FAVORITES_TABLE_NAME + " SET " +
                        FAVORITES_ANIME_ID + "= (case when " + FAVORITES_ANIME_ID + " != " + from_id + " then -(" + FAVORITES_ANIME_ID + " + 1) else -" + to_id + " end) " +
                        "WHERE " + FAVORITES_ANIME_ID + " >= " + to_id + " AND " + FAVORITES_ANIME_ID + " <= " + from_id;
            }
            else {
                query1 = "UPDATE " + FAVORITES_TABLE_NAME + " SET " +
                        FAVORITES_ANIME_ID + "= (case when " + FAVORITES_ANIME_ID + " != " + from_id + " then -(" + FAVORITES_ANIME_ID + " - 1) else -" + to_id + " end) " +
                        "WHERE " + FAVORITES_ANIME_ID + " >= " + from_id + " AND " + FAVORITES_ANIME_ID + " <= " + to_id;
            }
            dataBase.getMyDB().execSQL(query1);
            dataBase.getMyDB().execSQL(query2);
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public OneAnime.OneAnimeWithId isFavorite(String path,int HOST){
        final String[] args = new String[]{path,String.valueOf(HOST)};
        try (Cursor c = this.dataBase.getMyDB().rawQuery(
                "SELECT * FROM " + FAVORITES_TABLE_NAME + " WHERE " + FAVORITES_ANIME_PATH + " = ? and " + FAVORITES_ANIME_HOST + " = ?", args)) {
            if (c.moveToNext())
                return this.getFromCursor(c);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    public String[] getInitQuery() {
        return new String[]{"CREATE TABLE IF NOT EXISTS "+FAVORITES_TABLE_NAME+ " (" +
                FAVORITES_ANIME_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                FAVORITES_ANIME_HOST+" INEGER NOT NULL, "+
                FAVORITES_ANIME_PATH+" TEXT NOT NULL, " +
                FAVORITES_ANIME_YEAR+" INTEGER NOT NULL,"+
                FAVORITES_ANIME_TITLE+" TEXT NOT NULL, "+
                FAVORITES_ANIME_DESCRIPTION+" TEXT NOT NULL,"+
                FAVORITES_ANIME_TYPE+" INTEGER NOT NULL"+
                ");"};
    }

    @NotNull public int[] getCount(){
        int[] ans = new int[FavoritesType.orderToShow.length];
        try(Cursor c = dataBase.getMyDB().query(FAVORITES_TABLE_NAME,new String[]{FAVORITES_ANIME_TYPE},null,null,null,null,null)){
            while (c.moveToNext()){
                FavoritesType type = FavoritesType.values()[c.getInt(c.getColumnIndexOrThrow(FAVORITES_ANIME_TYPE))];
                for (int i = 0; i < FavoritesType.orderToShow.length; i++) {
                    final FavoritesType item = FavoritesType.orderToShow[i];
                    if(item == type) {
                        ans[i]++;
                        break;
                    }
                }
            }
        }
        catch (Exception e){
            if(Config.NEED_LOG) Log.e(Config.DATABASE_ER_LOG,e.getMessage(),e);
        }
        return ans;
    }

    public enum FavoritesType{
        //do not change order - it saves indexes in database
        FAVORITES,
        WATCH_LATER,
        WATCHING_NOW,
        WATCHED,
        BREAK_WATCHING;
        //order to show
        public static final FavoritesType[] orderToShow = {FAVORITES,WATCH_LATER,WATCHING_NOW,WATCHED,BREAK_WATCHING};
        public static final int[] idItems = {R.id.favoritesFavItem,R.id.favoritesWatchLaterItem,R.id.favoritesWatchNowItem,R.id.favoritesWatchedItem,R.id.favoritesStopWatchedItem};
        //titles for each favorites type
        public static final EnumMap<FavoritesType,Integer> valuesTitles = new EnumMap<FavoritesType,Integer>(FavoritesType.class){{
           put(FAVORITES, R.string.favorites_item_fav);
           put(WATCH_LATER, R.string.favorites_item_plan);
           put(WATCHING_NOW, R.string.favorites_item_watch_now);
           put(WATCHED, R.string.favorites_item_watched);
           put(BREAK_WATCHING, R.string.favorites_item_stop_watching);
        }};
    }
}
