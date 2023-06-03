package com.imcorp.animeprog.Requests.JsonObj.Video;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.exoplayer2.util.MimeTypes;
import com.imcorp.animeprog.Config;

public class Subtitle implements Parcelable {
    public String url, title;
    public Subtitle() {}
    public Subtitle(final String url, final String title) {
        this.url = url;
        this.title = title;
    }

    public String getLanguage(){

        return title;
//        if(title != null) {
//            if (title.contains("ru") || title.contains("Ru")) return "ru";
//            else if (title.contains("ja") || title.contains("Ja")) return "ja";
//        }
//        return "en";
    }

    public static final Creator<Subtitle> CREATOR = new Creator<Subtitle>() {
        @Override
        public Subtitle createFromParcel(Parcel in) {
            return new Subtitle(in.readString(),in.readString());
        }

        @Override
        public Subtitle[] newArray(int size) {
            return new Subtitle[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(title);
    }

    public String getMime() {
        if(url.contains(".vtt")) return MimeTypes.TEXT_VTT;
        else return MimeTypes.APPLICATION_SUBRIP;
    }

    public String getFileName() {
        return Config.md5(this.url)+".vvt";
    }
}
