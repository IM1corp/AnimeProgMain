package com.imcorp.animeprog.DB;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.imcorp.animeprog.DB.PathLoader.AnimeLoader;
import com.imcorp.animeprog.DB.PathLoader.Cache;
import com.imcorp.animeprog.DB.local.Downloads;
import com.imcorp.animeprog.DB.local.Favorites;
import com.imcorp.animeprog.DB.local.History;
import com.imcorp.animeprog.DB.local.Progress;

import org.intellij.lang.annotations.Language;

import static android.content.Context.MODE_PRIVATE;

public class DataBase {
    public static final String MAIN_DB_NAME = "database.db";

    @Language("RoomSql") public static final String HISTORY_TABLE_NAME = "history";
    @Language("RoomSql") public static final String HISTORY_HOST = "host";
    @Language("RoomSql") public static final String HISTORY_PATH_NAME = "path";
    @Language("RoomSql") public static final String HISTORY_TITLE_NAME = "title";
    @Language("RoomSql") public static final String HISTORY_DATE_NAME = "date";
    @Language("RoomSql") public static final String DOWNLOADS_TABLE_NAME = "downloads";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_TITLE = "anime_title";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_ID = "anime_id";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_PATH = "path";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_COVER = "cover";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_RAITING = "raiting";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_STUDIO = "studio";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_ISSUE_DATE = "issueDate";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_ORIGINAL_SOURCE = "originalSource";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_STATUS = "status";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_GENRES = "genres";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_SYNONYMS = "synonyms";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_SUBTITLES = "subtitles";

    @Language("RoomSql") public static final String DOWNLOADS_ANIME_HOST = "host";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_DESCRIPTION = "description";
    @Language("RoomSql") public static final String DOWNLOADS_ANIME_YEAR = "year";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODES_TABLE = "ep_count";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_DUBBING = "dubbing";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_PLAYER = "player";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_NUM = "num";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_DOWNLOAD = "download";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_FRAME_URL = "frame";
    @Language("RoomSql") public static final String DOWNLOADS_EPISODE_ID = "episodes_id";
    @Language("RoomSql") public static final String FAVORITES_TABLE_NAME = "favorites";
    @Language("RoomSql") public static final String FAVORITES_ANIME_HOST = "host";
    @Language("RoomSql") public static final String FAVORITES_ANIME_PATH = "path";
    @Language("RoomSql") public static final String FAVORITES_ANIME_TYPE = "type";
    @Language("RoomSql") public static final String FAVORITES_ANIME_TITLE = "title";
    @Language("RoomSql") public static final String FAVORITES_ANIME_DESCRIPTION = "description";
    @Language("RoomSql") public static final String FAVORITES_ANIME_YEAR = "year";
    @Language("RoomSql") public static final String FAVORITES_ANIME_ID = "fav_id";

    @Language("RoomSql") public static final String CACHE_FILE_NAME = "file_name";
    @Language("RoomSql") public static final String CACHE_DATE = "date";
    @Language("RoomSql") public static final String CACHE_TABLE_NAME = "files_cache";

    @Language("RoomSql") public static final String PROGRESS_TABLE = "progress_table";
    @Language("RoomSql") public static final String PROGRESS_ANIME_ID = "progress_id";
    @Language("RoomSql") public static final String PROGRESS_ANIME_PATH = "progress_path";
    @Language("RoomSql") public static final String PROGRESS_STATE = "progress_state";
    @Language("RoomSql") public static final String PROGRESS_NUM_WATCHED = "num_watched";
    @Language("RoomSql") public static final String PROGRESS_PROGRESS = "progress_progress";



    public static final String SHARED_PREF_NAME = "settings.pref";
    public static final String PREF_SPEED_KEY = "speed";
    public static final String PREF_YUMMY_TOKEN = "yummy_token";
    public static final String PREF_SEARCH_HOST_KEY = "host";
    public static final String PREF_HOST_KEY = "main_host";
    public static final String PREF_MAX_CACHE_FILES_KEY = "cache_max_count";
    public static final String SHOW_VIDEO_HINT = "video_hint";
    public static final String PREF_COOKIES_KEY = "cookies";
    public static final String PREF_USER_PROGRESS_KEY = "prog";
    public static final String PREF_VOLUME_KEY = "volume";
    public static final String PREF_QUALITY_KEY = "video_q";
    public static final String PREF_DB_V = "db_version";


    public final Context context;
    private SharedPreferences preferences;
    private static SQLiteDatabase myDB;

    public final Settings settings = new Settings(this);
    public final History history = new History(this);
    public final Downloads downloads = new Downloads(this);
    public final Favorites favorites = new Favorites(this);
    public final Cache cache = new Cache(this);
    public final Progress progress = new Progress(this);
    public final AnimeLoader loader;

    private final SimpleDB[] initRows = {favorites,downloads,history,progress,cache};
    public DataBase(Context context){
        this.context = context;
        this.loader =  new AnimeLoader(this);
    }

    public SQLiteDatabase getMyDB() {
        if(myDB==null){
            myDB = context.openOrCreateDatabase(MAIN_DB_NAME, MODE_PRIVATE, null);
            this.initRows();
        }
        return myDB;
    }
    public SharedPreferences getPreferences() {
        if(preferences==null){
            preferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        }
        return preferences;
    }
    private void initRows() {
        try {
            myDB.beginTransaction();
            int version = settings.getDbVersion();

            for (SimpleDB i : initRows) {
                final String[] query = i.getInitQuery();
                if (query != null)
                    for (String q : query) myDB.execSQL(q);
            }
            if (version < DBUpdater.DB_VERSION) {
                new DBUpdater(myDB).updateTo(version);
            }
            settings.updateDbVersion();
            myDB.setTransactionSuccessful();
        }finally {
            if(getMyDB().isOpen() && getMyDB().inTransaction())
                myDB.endTransaction();
        }
    }

    public void close() {
        if(myDB!=null) {
            if(myDB.inTransaction())
                myDB.endTransaction();
            myDB.close();
        }
        myDB=null;
    }
}
