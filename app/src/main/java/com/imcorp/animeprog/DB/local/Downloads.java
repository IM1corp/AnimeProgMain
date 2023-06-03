package com.imcorp.animeprog.DB.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.SimpleDB;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static com.imcorp.animeprog.DB.DataBase.*;
public class Downloads implements SimpleDB {
    private final DataBase dataBase;
    public Downloads(DataBase dataBase) {
        this.dataBase = dataBase;
    }
    public String[] getInitQuery(){
        return new String[]{"CREATE TABLE IF NOT EXISTS "+DOWNLOADS_TABLE_NAME+ " (" +
                DOWNLOADS_ANIME_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                DOWNLOADS_ANIME_HOST+" INEGER NOT NULL, "+
                DOWNLOADS_ANIME_PATH+" TEXT , " +
                //DOWNLOADS_ANIME_COVER+" TEXT, " +
                DOWNLOADS_ANIME_TITLE+" TEXT NOT NULL, "+
                DOWNLOADS_ANIME_DESCRIPTION+" TEXT NOT NULL, "+
                DOWNLOADS_ANIME_YEAR+" INTEGER NOT NULL ,"+
                DOWNLOADS_ANIME_RAITING +" TEXT " +
                ");", "CREATE TABLE IF NOT EXISTS "+ DOWNLOADS_EPISODES_TABLE +" ("+
                DOWNLOADS_EPISODE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                DOWNLOADS_ANIME_ID+" INTEGER NOT NULL,"+
                DOWNLOADS_EPISODE_DUBBING + " STRING NOT NULL,"+
                DOWNLOADS_EPISODE_PLAYER + " INTEGER NOT NULL," +
                DOWNLOADS_EPISODE_NUM + " TEXT, " +
                DOWNLOADS_EPISODE_DOWNLOAD+" INTEGER NOT NULL,"+
                DOWNLOADS_EPISODE_FRAME_URL+" TEXT NOT NULL"+
                ");"
        };
    }
    public long addToDownloads(OneAnime anime){
        try {
            dataBase.getMyDB().beginTransaction();
            ContentValues row1 = new ContentValues(12);
            //change this                           _---_
            row1.put(DOWNLOADS_ANIME_HOST, anime.HOST);
            row1.put(DOWNLOADS_ANIME_PATH, anime.getPath());
            row1.put(DOWNLOADS_ANIME_TITLE, anime.title);
            row1.put(DOWNLOADS_ANIME_DESCRIPTION, anime.description);
            row1.put(DOWNLOADS_ANIME_YEAR, anime.year);
            row1.put(DOWNLOADS_ANIME_RAITING, anime.getAttrs().rating);
            row1.put(DOWNLOADS_ANIME_GENRES, OneAnime.Link.arrayToBlob(anime.getAttrs().genres));
            row1.put(DOWNLOADS_ANIME_STUDIO, anime.getAttrs().getStudio() != null ? anime.getAttrs().getStudio().toBlob() : null);
            row1.put(DOWNLOADS_ANIME_ISSUE_DATE, anime.getAttrs().issueDate);
            row1.put(DOWNLOADS_ANIME_ORIGINAL_SOURCE, anime.getAttrs().originalSource);
            row1.put(DOWNLOADS_ANIME_STATUS, anime.status);
            row1.put(DOWNLOADS_ANIME_SYNONYMS, ArrayUtilsKt.stringJoiner(anime.getAttrs().synonyms, "\n"));
            //row1.put(FAVORITES_ANIME_ID,getMaxId());
            final long id = this.animeExists(anime), res;
            if (id != -1) {
                res = id;
                dataBase.getMyDB().update(
                        DOWNLOADS_TABLE_NAME, row1, DOWNLOADS_ANIME_ID + "=?", new String[]{String.valueOf(id)});
            }
            else res = dataBase.getMyDB().insertWithOnConflict(
                    DOWNLOADS_TABLE_NAME, DOWNLOADS_ANIME_PATH, row1, SQLiteDatabase.CONFLICT_REPLACE
            );
            this.dataBase.loader.saveCover(anime);
            dataBase.getMyDB().setTransactionSuccessful();
            return res;
        }
        finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    private int animeExists(OneAnime anime){
        try (Cursor c = dataBase.getMyDB()
                .query(DOWNLOADS_TABLE_NAME, null,
                        DOWNLOADS_ANIME_PATH + "=?", new String[]{anime.getPath()},
                        null, null, null, "1")) {
            if (!c.moveToNext()) return -1;
            return c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_ID));
        }
    }
    public long addVideoToDownloads(final long anime_id, final OneVideo video,final boolean is_downloading){
        try {
            dataBase.getMyDB().beginTransaction();
            final String selection = DOWNLOADS_ANIME_ID + "=? and " + DOWNLOADS_EPISODE_NUM + "=? and " + DOWNLOADS_EPISODE_PLAYER + "=? and " + DOWNLOADS_EPISODE_DUBBING + "=? and " + DOWNLOADS_EPISODE_DOWNLOAD + "=?";

            try (Cursor c = dataBase.getMyDB().query(DOWNLOADS_EPISODES_TABLE, null, selection,
                    new String[]{
                            String.valueOf(anime_id),
                            video.num,
                            String.valueOf(video.player.ordinal()),
                            video.voiceStudio,
                            is_downloading ? "0" : "-1"
                    }, null, null, null)) {
                if (c.moveToNext())
                    return -1;
            }

            ContentValues row1 = new ContentValues(7);
            row1.put(DOWNLOADS_ANIME_ID, anime_id);
            row1.put(DOWNLOADS_EPISODE_DUBBING, video.voiceStudio);
            row1.put(DOWNLOADS_EPISODE_PLAYER, video.player.ordinal());
            row1.put(DOWNLOADS_EPISODE_NUM, video.num);
            row1.put(DOWNLOADS_EPISODE_DOWNLOAD, is_downloading ? 0 : -1);
            row1.put(DOWNLOADS_EPISODE_FRAME_URL, video.urlFrame);
            row1.put(DOWNLOADS_ANIME_SUBTITLES, video.getVideoSubtitlesAsString());
            final long res = dataBase.getMyDB().insert(DOWNLOADS_EPISODES_TABLE, null, row1);

            dataBase.getMyDB().setTransactionSuccessful();
            return res;
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public ArrayList<OneAnime.OneAnimeWithId> getDownloads(int count,final int offset){
        Cursor c=null;
        final ArrayList<OneAnime.OneAnimeWithId> answer;
        try {
            answer=new ArrayList<>();
            c = this.dataBase.getMyDB().query(DOWNLOADS_TABLE_NAME, null, null, null, null, null,
                    DOWNLOADS_ANIME_ID + " DESC");
            if (offset > 0) c.moveToPosition(offset - 1);
            while (c.moveToNext() && count-- > 0) {
                answer.add(parseOneDownloadsFromCursor(c));
            }
        }finally {
            if(c!=null)c.close();
        }
        return answer;
    }
    @Nullable public OneAnime.OneAnimeWithId getFromDownloads(long id){
        return getDownloadFromSelectQ(DOWNLOADS_ANIME_ID+" = ?",new String[]{String.valueOf(id)});
    }
    @Nullable public OneAnime.OneAnimeWithId getFromDownloads(final String path){
        return getDownloadFromSelectQ(DOWNLOADS_ANIME_PATH+" = ?",new String[]{path});
    }
    public ArrayList<OneAnime.OneAnimeWithId> search(String query,int count){
        try(final Cursor c = this.dataBase.getMyDB().query(DOWNLOADS_TABLE_NAME,null,
                            DOWNLOADS_ANIME_TITLE+" LIKE ?",new String[]{"%"+query.trim()+"%"},
                            null,null,null,String.valueOf(count))) {
            final ArrayList<OneAnime.OneAnimeWithId> ans = new ArrayList<>(Math.max(count, c.getCount()));
            while (c.moveToNext()) ans.add(this.parseOneDownloadsFromCursor(c));
            return ans;
        }
    }

    @Nullable private OneAnime.OneAnimeWithId getDownloadFromSelectQ(@Language("RoomSql") final String selectQ,@Nullable final String[] args){
        try (Cursor c = this.dataBase.getMyDB().query(DOWNLOADS_TABLE_NAME, null, selectQ, args, null, null, null, "1")) {
            if (!c.moveToNext()) return null;
            return parseOneDownloadsFromCursor(c);
        }

    }
    private OneAnime.OneAnimeWithId parseOneDownloadsFromCursor(Cursor c){
        final int id = c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_ID));
        OneAnime anime = new OneAnime((byte) c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_HOST)));
        anime.setPath(c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_PATH)));
        dataBase.loader.loadCover(anime);
        anime.description = c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_DESCRIPTION));
        anime.title = c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_TITLE));
        anime.year = c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_YEAR));
        anime.status=c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_STATUS));

        anime.getAttrs().rating=c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_RAITING));
        anime.getAttrs().genres=(ArrayList<OneAnime.Link>) OneAnime.Link.arrayFromBlob(c.getBlob(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_GENRES)));
        anime.getAttrs().setStudios(new ArrayList<OneAnime.Link>(){{
            add(OneAnime.Link.fromBlob(c.getBlob(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_STUDIO))));
                                    }});
        anime.getAttrs().issueDate=c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_ISSUE_DATE));
        anime.getAttrs().originalSource=c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_ORIGINAL_SOURCE));
        final String synonyns = c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_SYNONYMS));
        anime.getAttrs().synonyms = synonyns!=null? new ArrayList<>(Arrays.asList(synonyns.split("\\n"))):null;

        anime.setFullyLoaded(true);
        final int [] counts = getDownloadsC(anime);
        final OneAnime.OneAnimeWithId animeWithId = new OneAnime.OneAnimeWithId(id, anime,counts[0],counts[1]);
        return animeWithId;
    }

    public int[] getDownloadsC(final OneAnime anime) {
        final int[] def_ans = {0,0};
        final int index = this.animeExists(anime);
        if(index==-1)return def_ans;
        Cursor cursor=null;
        int[] answer = {0,0};
        HashMap<String,Boolean> datas = new HashMap<>();
        try{
            cursor = this.dataBase.getMyDB().query(DOWNLOADS_EPISODES_TABLE,
                    new String[]{DOWNLOADS_EPISODE_DOWNLOAD,DOWNLOADS_EPISODE_NUM},
                    DOWNLOADS_ANIME_ID+"="+index,
                    null,
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()){
                final String num = cursor.getString(1);
                final boolean value = cursor.getInt(0)!=-1;
                if(!datas.containsKey(num)){
                    datas.put(num,value);
                    if(value)answer[0]++;
                    answer[1]++;
                }
                else if(value&&!datas.get(num)){
                    answer[0]++;
                }
            }
        }finally {
            if(cursor!=null)cursor.close();
        }
        return answer;
    }
    public void loadVideosToAnime(OneAnime.OneAnimeWithId anime) {
        anime.anime.videos.clear();

        final String selection = DOWNLOADS_ANIME_ID+"="+anime.id;
        @Language("RoomSql")
        final String order_by = "cast("+DOWNLOADS_EPISODE_NUM+" as unsigned)";
        try (Cursor c = dataBase.getMyDB().query(DOWNLOADS_EPISODES_TABLE, null, selection, null, null, null, order_by)) {
            final ArrayList<OneVideo> list = new ArrayList<>(c.getColumnCount());
            while (c.moveToNext()) {
                final int ids = c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_ID));
                OneVideo video = new OneVideo(c.getString(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_NUM)));
                video.voiceStudio = c.getString(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_DUBBING));
                video.urlFrame = c.getString(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_FRAME_URL));
                video.player = OneVideo.OneVideoPlayer.values()[c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_PLAYER))];
                video.downloaded = c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_DOWNLOAD)) != -1;
                video.subtitlesToArray(c.getString(c.getColumnIndexOrThrow(DOWNLOADS_ANIME_SUBTITLES)),dataBase.loader.getAnimeLocalPath(anime.anime.getPath()));
                list.add(video);
            }
            final HashMap<String, ArrayList<OneVideo>> map = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                final OneVideo item = list.get(i);
                final ArrayList<OneVideo> ep_arr;
                if (!map.containsKey(item.num)) {
                    final OneVideoEP ep = new OneVideoEP(true);
                    ep.num = item.num;
                    ep_arr = ep.initVideos(1);
                    map.put(item.num, ep_arr);
                    anime.anime.videos.add(ep);
                }
                else ep_arr = map.get(item.num);
                Objects.requireNonNull(ep_arr).add(item);
            }
        }
    }

    public void moveDownloadItem(final int from_id, int to_id) {
        if (from_id == to_id) return;
        try {
            dataBase.getMyDB().beginTransaction();
            for (final String table : new String[]{DOWNLOADS_TABLE_NAME, DOWNLOADS_EPISODES_TABLE}) {
                final String query1, query2 = "UPDATE " + table + " SET " + DOWNLOADS_ANIME_ID + " = -" + DOWNLOADS_ANIME_ID + " WHERE " + DOWNLOADS_ANIME_ID + " < 0";
                if (from_id > to_id) {
                    query1 = "UPDATE " + table + " SET " +
                            DOWNLOADS_ANIME_ID + "= (case when " + DOWNLOADS_ANIME_ID + " != " + from_id + " then -(" + DOWNLOADS_ANIME_ID + " + 1) else -" + to_id + " end) " +
                            "WHERE " + DOWNLOADS_ANIME_ID + " >= " + to_id + " AND " + DOWNLOADS_ANIME_ID + " <= " + from_id;
                }
                else {
                    query1 = "UPDATE " + table + " SET " +
                            DOWNLOADS_ANIME_ID + "= (case when " + DOWNLOADS_ANIME_ID + " != " + from_id + " then -(" + DOWNLOADS_ANIME_ID + " - 1) else -" + to_id + " end) " +
                            "WHERE " + DOWNLOADS_ANIME_ID + " >= " + from_id + " AND " + DOWNLOADS_ANIME_ID + " <= " + to_id;
                }
                dataBase.getMyDB().execSQL(query1);
                dataBase.getMyDB().execSQL(query2);
            }
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public void deleteAnimeFromDownloads(final int id,final OneAnime anime) {
        try {
            dataBase.getMyDB().beginTransaction();
            final String query = DOWNLOADS_ANIME_ID + "=" + id;
            dataBase.getMyDB().delete(DOWNLOADS_TABLE_NAME, query, null);
            dataBase.getMyDB().delete(DOWNLOADS_EPISODES_TABLE, query, null);
            this.deleteFiles(anime);
            dataBase.getMyDB().setTransactionSuccessful();
        }finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
        }
    }
    public void deleteVideoFromDownloads(OneAnime anime, OneVideo deletingVideo) throws InvalidHtmlFormatException {
        Cursor c=null;
        try {
            dataBase.getMyDB().beginTransaction();
            final int id = this.animeExists(anime);
            if (id == -1)
                throw new InvalidHtmlFormatException("Database is invalid - anime not found");
            c = dataBase.getMyDB().query(DOWNLOADS_EPISODES_TABLE,null,
                    DOWNLOADS_EPISODE_NUM+"=? and "+DOWNLOADS_EPISODE_DUBBING+"=? and "+DOWNLOADS_EPISODE_PLAYER+"=?",
                    new String[]{deletingVideo.num,deletingVideo.voiceStudio,String.valueOf(deletingVideo.player.ordinal())},
                    null,null,null);
            if(!c.moveToNext()) throw new InvalidHtmlFormatException("Database is invalid - video not found");
            final int episode_id = c.getInt(c.getColumnIndexOrThrow(DOWNLOADS_EPISODE_ID));

            c.close();
            c=null;

            ContentValues update_values = new ContentValues(1);
            update_values.put(DOWNLOADS_EPISODE_DOWNLOAD,-1);
            dataBase.getMyDB().update(DOWNLOADS_EPISODES_TABLE,update_values,
                    DOWNLOADS_EPISODE_ID+"="+episode_id,null);

            final File path_to_anime = dataBase.loader.getAnimeLocalPath(anime.getPath());
            for(File f:new File[]{new File(path_to_anime,deletingVideo.num+".mp4"),new File(path_to_anime,deletingVideo.num+".m3u8")})
                if(f.exists()){
                    if(!f.delete())
                        f.deleteOnExit();
                }
            dataBase.getMyDB().setTransactionSuccessful();
        } finally {
            if(dataBase.getMyDB().isOpen() && dataBase.getMyDB().inTransaction())
                dataBase.getMyDB().endTransaction();
            if(c!=null)c.close();
        }
    }
    private void deleteFiles(@Nullable final File[] files,@NonNull final OneAnime anime){
        if(files==null)return;
        for (final File fileEntry : files) {
            if(fileEntry.getName().equals(Config.COVER_DATA_PATH) && dataBase.favorites.isFavorite(anime.getPath(),anime.HOST)!=null)continue;
            if(fileEntry.isDirectory())deleteFiles(fileEntry.listFiles(), anime);
            if(!fileEntry.delete()) fileEntry.deleteOnExit();

        }
    }
    private void deleteFiles(final OneAnime anime){
        final File path = dataBase.loader.getAnimeLocalPath(anime.getPath());
        File[] files;
        if(path!=null)
            deleteFiles(path.listFiles(),anime);
        files = path.listFiles();
        if(files==null||files.length==0)
            if(!path.delete())path.deleteOnExit();
    }

}
