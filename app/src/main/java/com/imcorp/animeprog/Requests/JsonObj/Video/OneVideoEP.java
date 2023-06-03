package com.imcorp.animeprog.Requests.JsonObj.Video;

import android.content.res.Resources;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OneVideoEP implements Parcelable {
    public boolean selectAble=true;
    public String num;
    public HashMap<String,String> keys = new HashMap<>();
    private ArrayList<OneVideo> videos=null;
    public OneVideoEP(){}
    public OneVideoEP(boolean selectAble){this.selectAble=selectAble;}
    @Nullable
    public ArrayList<OneVideo> getVideos(final OneAnime anime, Request request) throws IOException, JSONException {
        if(this.videos!=null&&this.videos.size()!=0){
            return videos;
        }
        else if(anime.HOST == Config.HOST_ANIMEGO_ORG){
            return request.loadVideosFromEpisodeAG(this);
        }
        else if(anime.HOST == Config.HOST_GOGO_ANIME){
            return request.loadVideosFromEpisodeGA(this);
        }
        return null;
    }
    public ArrayList<OneVideo> initVideos(int size){
        if(this.videos==null) return this.videos = new ArrayList<>(size);
        return this.videos;
    }
    public boolean videosLoaded(){
        return videos!=null;
    }
    public boolean isDownloaded(){
        if(!videosLoaded())return false;
        return ArrayUtilsKt.any(videos, objects -> objects.downloaded)!=null;
    }
    public static final Creator<OneVideoEP> CREATOR = new Creator<OneVideoEP>() {
        @Override
        public OneVideoEP createFromParcel(Parcel in) {
            return new OneVideoEP(in);
        }

        @Override
        public OneVideoEP[] newArray(int size) {
            return new OneVideoEP[size];
        }
    };
    @SuppressWarnings("unchecked")
    private OneVideoEP(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) selectAble = in.readBoolean();
        else selectAble = in.readByte()==1;
        num = in.readString();
        videos = in.createTypedArrayList(OneVideo.CREATOR);

        /*
        String[] data = in.createStringArray();
        if(data!=null) for(int i=0;i<data.length;i++){
            keys.put(data[i*2],data[i*2+1]);
        }*/
        keys = in.readHashMap(String.class.getClassLoader());
    }
    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(selectAble);
        else dest.writeByte((byte)(selectAble?1:0));
        dest.writeString(num);
        dest.writeTypedList(videos);

        dest.writeMap(keys);
        /*int i=0;
        for(Map.Entry<String, String> entry:keys.entrySet()){
            k[i*2] = entry.getKey();
            k[i*2+i] = entry.getValue();
            i++;
        }
        dest.writeStringArray(k);*/

    }

    public CharSequence getTitle(Resources resources) {
        if(selectAble)
            return this.num + " "+ resources.getString(R.string.episodes);
        return this.num;
    }
}
