package com.imcorp.animeprog.Default;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.DownloadManager.RecycleViewAdapter;

import java.lang.reflect.ParameterizedType;

public class ParcelableSparseIntArray<T extends Parcelable> implements Parcelable {
    private final SparseArray<T> arr;
    private final Class<T> tClass;
    public ParcelableSparseIntArray(SparseArray<T> array,Class<T> tClass){
        this.arr = array;
        this.tClass = tClass;
    }

    private ParcelableSparseIntArray(Parcel in) {
        if(in.readByte()==1)tClass =(Class<T>) RecycleViewAdapter.EpisodeSelected.class;
        else tClass=null;
        if(tClass!=null) arr = in.readSparseArray(tClass.getClassLoader());
        else arr = null;
    }
    public static final Creator<ParcelableSparseIntArray> CREATOR = new Creator<ParcelableSparseIntArray>() {
        @Override
        public ParcelableSparseIntArray createFromParcel(Parcel in) {
            return new ParcelableSparseIntArray(in);
        }

        @Override
        public ParcelableSparseIntArray[] newArray(int size) {
            return new ParcelableSparseIntArray[size];
        }
    };
    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        if(tClass.equals(RecycleViewAdapter.EpisodeSelected.class)){
            dest.writeByte((byte)1);
        }else dest.writeByte((byte)0);
        dest.writeSparseArray(arr);
    }
    public SparseArray<T> getArr(){
        return arr;
    }
}
