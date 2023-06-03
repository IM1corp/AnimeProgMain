package com.imcorp.animeprog.Requests.JsonObj.mainPage;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;

public class MainPage implements Parcelable {
    public ArrayList<Container> containers;
    public ArrayList<RowList> rows;
    public RowList bigRow=null;
    public MainPage(){
        this.containers = new ArrayList<>();
        this.rows = new ArrayList<>();
    }
    public MainPage(ArrayList<Container> containers,ArrayList<RowList> rows,RowList bigRow){
        this.containers=containers;
        this.rows=rows;
        this.bigRow=bigRow;
    }

    public static final Creator<MainPage> CREATOR = new Creator<MainPage>() {
        @Override
        public MainPage createFromParcel(Parcel in) {
            return new MainPage(in.readArrayList(Container.class.getClassLoader()),in.readArrayList(RowList.class.getClassLoader()),in.readParcelable(RowList.class.getClassLoader()));
        }

        @Override
        public MainPage[] newArray(int size) {
            return new MainPage[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(containers);
        dest.writeList(rows);
        dest.writeParcelable(bigRow,0);
    }
    @NonNull
    public LinkedList<Parcelable> getData(){
        LinkedList<Parcelable> answer = new LinkedList<>();
        for(int i=0;i<Math.max(containers.size(),rows.size());i++){
            if(rows.size()>i)answer.add(rows.get(i));
            if(containers.size()>i)answer.add(containers.get(i));
        }
        return answer;
    }
}
