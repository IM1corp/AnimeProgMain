package com.imcorp.animeprog.Requests.JsonObj.mainPage;

import android.os.Parcel;
import android.os.Parcelable;

import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import java.util.ArrayList;

/**
 * Container contains list of @RowList
 * and its title
 */
public class Container implements Parcelable {
    public OneAnime.Link title;
    public ArrayList<RowList> list;
    public Container(final OneAnime.Link title, final ArrayList<RowList> list ){
        this.title=title;
        this.list=list;
    }
    public Container(){ }


    public static final Creator<Container> CREATOR = new Creator<Container>() {
        @Override
        @SuppressWarnings("unchecked")
        public Container createFromParcel(Parcel in) {
            return new Container(in.readParcelable(OneAnime.Link.class.getClassLoader()),in.readArrayList(RowList.class.getClassLoader()));
        }

        @Override
        public Container[] newArray(int size) {
            return new Container[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(title,0);
        dest.writeList(list);
    }
}
