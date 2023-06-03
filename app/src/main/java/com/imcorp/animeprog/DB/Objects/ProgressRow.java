package com.imcorp.animeprog.DB.Objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.local.Progress;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;


public final class ProgressRow implements Parcelable {
    public String path;
    public Progress.WatchState watchState;
    private HashSet<String> numWatched;
    public float progress;
    public Integer id;
    public ProgressRow(final String path, final Progress.WatchState watchState, @NotNull final String numWatched, final float progress, final int id){
        this.path=path;
        this.watchState=watchState;
        final String[] g = numWatched.split(",");
        this.numWatched = new HashSet<>(g.length);
        Collections.addAll(this.numWatched,g);
        this.numWatched.remove("");

        this.progress=progress;
        this.id=id;
    }
    public ProgressRow(final String path, final Progress.WatchState watchState, @Nullable final HashSet<String> numWatched, final float progress, @Nullable final Integer id){
        this.path=path;
        this.watchState=watchState;
        this.numWatched = numWatched!=null?numWatched:new HashSet<String>();
        this.progress=progress;
        this.id=id;
    }

    public static ProgressRow getDefault(final String path){
        return new ProgressRow(path, Progress.WatchState.UNDEFINED,null,0,null);
    }
    public boolean existsNum(String num){
        return numWatched.contains(num);
    }
    public ProgressRow addNum(String num){
        num = num.replace(",","");
        numWatched.add(num);
        return this;
    }
    public int numSize(){
        return numWatched.size();
    }
    public String getNumWatchedFormated(){
        return TextUtils.join(",",this.numWatched);
    }
    public void update(DataBase dataBase){
        dataBase.progress.updateRow(this);
    }

    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeInt(watchState.ordinal());
        final String[] g = new String[numWatched.size()];
        numWatched.toArray(g);
        dest.writeStringArray(g);
        dest.writeFloat(progress);
        Config.writeBoolean(dest,id!=null);
        if(id!=null)
            dest.writeInt(id);
    }
    private ProgressRow(Parcel in) {
        path = in.readString();
        watchState = Progress.WatchState.values()[in.readInt()];
        final String[] arr = in.createStringArray();
        numWatched = new HashSet<>(arr.length);
        Collections.addAll(numWatched,arr);
        progress = in.readFloat();
        if (Config.readBoolean(in)) id=in.readInt();
    }
    public static final Creator<ProgressRow> CREATOR = new Creator<ProgressRow>() {
        @Override
        public ProgressRow createFromParcel(Parcel in) {
            return new ProgressRow(in);
        }

        @Override
        public ProgressRow[] newArray(int size) {
            return new ProgressRow[size];
        }
    };
}
