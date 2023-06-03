package com.imcorp.animeprog.DB.PathLoader;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.Default.LoadImageEvents;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import java.io.File;
import java.io.IOException;

public class AnimeLoader {
    private final DataBase dataBase;
    public AnimeLoader(DataBase dataBase){
        this.dataBase = dataBase;
    }
    public void saveCover(final OneAnime anime) {
        final File cover_path = getCoverFilePath(anime.getPath());
        anime.loadCover(dataBase.context, new Request(null), new LoadImageEvents() {
            @Override public void onSuccess(Bitmap bitmap) {
                try {
                    dataBase.cache.saveBitmapToFile(bitmap,cover_path);
                } catch (IOException e) {
                    onFail(e);
                }
            }

            @Override public boolean onFail(Exception exception) {
                if(Config.NEED_LOG) Log.e(Config.LOG_TAG,exception.toString());
                return true;
            }
        });
    }
    public String loadCover(OneAnime anime){
        File file = getCoverFilePath(anime.getPath());
        return (file.isFile())?file.getPath():null;
    }
    public File getAnimeLocalPath(String path_){
        final File p = new File(getPath(),path_);
        if(!p.exists()&&!p.mkdirs())return null;
        return p;
    }

    private File getPath() {
        return new File(dataBase.context.getFilesDir(),Environment.DIRECTORY_MOVIES);//Config.DATA_PATH);
    }
    private File getCoverFilePath(String anime_path){
        return new File(getAnimeLocalPath(anime_path),Config.COVER_DATA_PATH);
    }
    public File getVideoURI(String anime,String num){
        File anime_path = getAnimeLocalPath(anime);
        File video_path_mp4 = new File(anime_path,num+".mp4");
        File video_path_m3u8 = new File(new File(anime_path,num),Config.M3U8_DOWNLOAD_FNAME);
        if (video_path_mp4.exists()) {
            return video_path_mp4;
        }
        if (video_path_m3u8.exists()) {
            return video_path_m3u8;
        }
        return null;
    }

    public void removeCover(String path) {
        File f = new File(getAnimeLocalPath(path), Config.COVER_DATA_PATH);
        if(f.exists()) f.delete();
        f = f.getParentFile();
        if(f.listFiles()==null||f.listFiles().length==0) f.delete();
    }
}
