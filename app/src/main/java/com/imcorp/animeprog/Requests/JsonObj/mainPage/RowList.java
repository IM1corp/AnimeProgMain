package com.imcorp.animeprog.Requests.JsonObj.mainPage;

import android.os.Parcel;
import android.os.Parcelable;

import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import java.util.ArrayList;

/**
 * {@link RowList} contains List of {@link com.imcorp.animeprog.Requests.JsonObj.OneAnime}
 * and {@link String} title
 **/
public class RowList implements Parcelable {
    public final ArrayList<OneAnime> list;
    public final OneAnime.Link title;
    public RowList(final OneAnime.Link title, final ArrayList<OneAnime> list) {
        this.title = title;
        this.list = list;
    }
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(title,0);
        dest.writeList(list);
    }
    public static final Creator<RowList> CREATOR = new Creator<RowList>() {
        @Override
        @SuppressWarnings("unchecked")
        public RowList createFromParcel(Parcel in) {
            return new RowList(in.readParcelable(OneAnime.Link.class.getClassLoader()),in.readArrayList(OneAnime.class.getClassLoader()));
        }
        @Override
        public RowList[] newArray(int size) {
            return new RowList[size];
        }
    };

}
