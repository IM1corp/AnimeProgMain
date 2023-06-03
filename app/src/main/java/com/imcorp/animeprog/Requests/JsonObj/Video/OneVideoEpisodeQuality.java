package com.imcorp.animeprog.Requests.JsonObj.Video;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.imcorp.animeprog.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneVideoEpisodeQuality implements Parcelable {
    public String mp4Url = null, m3u8Url = null, ref, downloadUrl, qualityTitle;
    public OneVideo.VideoType quality;

    public OneVideoEpisodeQuality(final String download_url){
        this.downloadUrl=download_url;
        this.quality = OneVideo.VideoType.DOWNLOADED;
    }
    public OneVideoEpisodeQuality(){}
    public OneVideoEpisodeQuality(String m3u8_url, String mp4_url, OneVideo.VideoType quality){
        this.m3u8Url = m3u8_url;
        this.mp4Url = mp4_url;
        this.quality = quality;
    }
    public OneVideoEpisodeQuality(String m3u8_url, String mp4_url, OneVideo.VideoType quality,String ref){
        this.m3u8Url = m3u8_url;
        this.mp4Url = mp4_url;
        this.quality = quality;
        this.ref=ref;
    }
    public OneVideoEpisodeQuality(String m3u8_url, String mp4_url, String quality){
        this.m3u8Url = m3u8_url;
        this.mp4Url = mp4_url;
        loadVideoTypeFromString(quality);
    }
    public OneVideoEpisodeQuality(String m3u8_url, String mp4_url, String quality,String ref){
        this.m3u8Url = m3u8_url;
        this.mp4Url = mp4_url;
        this.ref=ref;
        loadVideoTypeFromString(quality);
    }
    public void loadVideoTypeFromString (String str){
        if(str==null)this.quality= OneVideo.VideoType.UNDEFINED;
        else if(str.contains("240")) this.quality= OneVideo.VideoType.V240;
        else if(str.contains("360")) this.quality= OneVideo.VideoType.V360;
        else if(str.contains("480")) this.quality= OneVideo.VideoType.V480;
        else if(str.contains("720")) this.quality= OneVideo.VideoType.V720;
        else if(str.contains("1080")) this.quality= OneVideo.VideoType.V1080;
        else this.quality= OneVideo.VideoType.UNDEFINED;
    }
    public boolean loadVideoTypeFromM3U8Comment(String comment_line){
        final Pattern SEARCH_RESOLUTION_IN_M3U8 = Pattern.compile("RESOLUTION=\\d+x(\\d+)");
        Matcher m = SEARCH_RESOLUTION_IN_M3U8.matcher(comment_line);
        if(!m.find()){
            this.quality= OneVideo.VideoType.UNDEFINED;
        }
        this.loadVideoTypeFromString(m.group(1));
        return this.quality != OneVideo.VideoType.UNDEFINED;
    }
    public static String getQualityString(OneVideo.VideoType type,Context context){
        switch (type){
            case AUTO:
                return context.getString(R.string.auto_quality);
            case DOWNLOADED:
                return context.getString(R.string.downloaded);
            case V240:
                return "240p";
            case V360:
                return "360p";
            case V480:
                return "480p";
            case V720:
                return "720p";
            case V1080:
                return "1080p";
        }
        return context.getString(R.string.undefined_quality);
    }

    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mp4Url);
        dest.writeString(m3u8Url);
        dest.writeString(ref);
        dest.writeInt(quality!=null?quality.ordinal():-1);
    }
    public static final Creator<OneVideoEpisodeQuality> CREATOR = new Creator<OneVideoEpisodeQuality>() {
        @Override
        public OneVideoEpisodeQuality createFromParcel(Parcel in) {
            OneVideoEpisodeQuality quality =  new OneVideoEpisodeQuality();
            quality.mp4Url = in.readString();
            quality.m3u8Url = in.readString();
            quality.ref = in.readString();
            int q = in.readInt();
            if(q!=-1)quality.quality = OneVideo.VideoType.values()[q];
            return quality;

        }

        @Override
        public OneVideoEpisodeQuality[] newArray(int size) {
            return new OneVideoEpisodeQuality[size];
        }
    };
}