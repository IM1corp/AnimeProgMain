package com.imcorp.animeprog.Default;

import android.app.Service;
import android.os.Handler;

import androidx.annotation.CallSuper;

import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.Requests.Http.Request;

public abstract class SimpleService extends Service {
    public DataBase dataBase;
    public Handler threadCallback;
    public Request request = new Request(this);
    public SimpleService() {

    }

    @CallSuper
    @Override
    public void onCreate() {
        this.dataBase = new DataBase(this);
        super.onCreate();
        this.threadCallback = new Handler();
    }
}