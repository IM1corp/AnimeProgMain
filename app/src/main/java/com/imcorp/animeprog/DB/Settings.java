package com.imcorp.animeprog.DB;

import static com.imcorp.animeprog.DB.DataBase.PREF_COOKIES_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_DB_V;
import static com.imcorp.animeprog.DB.DataBase.PREF_HOST_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_MAX_CACHE_FILES_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_QUALITY_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_SPEED_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_USER_PROGRESS_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_VOLUME_KEY;
import static com.imcorp.animeprog.DB.DataBase.PREF_YUMMY_TOKEN;
import static com.imcorp.animeprog.DB.DataBase.SHOW_VIDEO_HINT;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.VideoTest;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
public class Settings{
    private final DataBase db;

    public int getDbVersion(){
        return db.getPreferences().getInt(PREF_DB_V,-1);
    }
    public void updateDbVersion(){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putInt(PREF_DB_V,DBUpdater.DB_VERSION);
        p.apply();
    }

    Settings(DataBase db){
        this.db=db;
    }
    public float getSpeed(){
        return db.getPreferences().getFloat(PREF_SPEED_KEY,1f);
    }
    public void setSpeed(float value){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putFloat(PREF_SPEED_KEY,value);
        p.apply();
    }
    public void setYummyToken(@Nullable String token){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putString(PREF_YUMMY_TOKEN, token);
        p.apply();
    }
    @Nullable
    public String getYummyToken(){
        return db.getPreferences().getString(PREF_YUMMY_TOKEN, null);
    }
    public void setSearchHost(byte value){
        this.setMainHost(value);
//        SharedPreferences.Editor p = db.getPreferences().edit();
//        p.putInt(PREF_SEARCH_HOST_KEY,value);
//        p.apply();
    }
    public byte getSearchHost(){
        return this.getMainHost();
//        return (byte)db.getPreferences().getInt(PREF_SEARCH_HOST_KEY, Config.HOST_YUMMY_ANIME);
    }
    public void setMainHost(byte value){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putInt(PREF_HOST_KEY,value);
        p.apply();
    }
    public byte getMainHost(){
        return (byte)db.getPreferences().getInt(PREF_HOST_KEY, Config.HOST_YUMMY_ANIME);
    }
    public int getMaxCacheFilesCount(){
        return db.getPreferences().getInt(PREF_MAX_CACHE_FILES_KEY, 1000);
    }
    public void setMaxCacheFilesCount(int count){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putInt(PREF_MAX_CACHE_FILES_KEY, count);
        p.apply();
    }

    public boolean getSendBugs(){
        return db.getPreferences().getBoolean(db.context.getString(R.string.pref_enable_bugs),true);
    }
    public boolean getSendStatics(){
        return db.getPreferences().getBoolean(db.context.getString(R.string.pref_enable_log),true);
    }
    public int getTheme(){
        return db.getPreferences().getInt(db.context.getString(R.string.pref_theme_key), 2);
    }
    public void setTheme(final int theme){
        SharedPreferences.Editor p = db.getPreferences().edit();
        p.putInt(db.context.getString(R.string.pref_theme_key), theme);
        p.apply();
    }
    public boolean getBottomMenuAvailable(){
        return db.getPreferences().getBoolean(db.context.getString(R.string.pref_bottom_panel_key),true);
    }
    public boolean getTopLeftMenuAvailable(){
        return db.getPreferences().getBoolean(db.context.getString(R.string.pref_left_menu_key),true);
    }
    public boolean needToShowVideoHind(){
        SharedPreferences preferences = db.getPreferences();
        return preferences.getBoolean(SHOW_VIDEO_HINT,false);
    }
    public void setShowVideoHint(boolean value){
        SharedPreferences preferences = db.getPreferences();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SHOW_VIDEO_HINT,value);
        editor.apply();
    }
    public void setCookies(String[] string){
        SharedPreferences.Editor editor = db.getPreferences().edit();
        StringBuilder b = new StringBuilder();
        for(int i=0;i<string.length;i++){
            b.append(string[i]);
            if(i!=string.length-1)
                    b.append(';');
        }

        editor.putString(PREF_COOKIES_KEY, b.toString());
    }
    public String getCookies(){
        return db.getPreferences().getString(PREF_COOKIES_KEY,"");
    }

    public float getVolume(VideoTest manager) {
        return db.getPreferences().getFloat(PREF_VOLUME_KEY,manager.getSystemVolume());
    }
    public void setVolume(float value) {
        SharedPreferences.Editor editor = db.getPreferences().edit();
        editor.putFloat(PREF_VOLUME_KEY,value);
        editor.apply();
    }
    public void setQuality(OneVideo.VideoType quality){
        SharedPreferences.Editor editor = db.getPreferences().edit();
        editor.putInt(PREF_QUALITY_KEY,quality.ordinal());
        editor.apply();
    }
    public OneVideo.VideoType getQuality(){
        return OneVideo.VideoType.values()[db.getPreferences().getInt(PREF_QUALITY_KEY, OneVideo.VideoType.V360.ordinal())];
    }
    public int getUserProgress() {
        return db.getPreferences().getInt(PREF_USER_PROGRESS_KEY,0);
    }
    public void setUserProgress(int progress){
        SharedPreferences.Editor e = db.getPreferences().edit();
        e.putInt(PREF_USER_PROGRESS_KEY,progress);
        e.apply();
    }

    public static final int[] THEMES_STYLE = {R.style.AppTheme,R.style.AppDarkTheme};
    public static final int[] THEMES_STRING = {R.string.theme_0,R.string.theme_1, R.string.theme_auto};
    public static final int PROGRESS_VERIFY_POLICY = 1;

}
