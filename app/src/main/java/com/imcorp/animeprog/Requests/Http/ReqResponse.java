package com.imcorp.animeprog.Requests.Http;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ReqResponse{
    private final String text;
    public ReqResponse(@androidx.annotation.NonNull String text){
        this.text=text;
    }
    @Nullable
    public JSONObject toJson() throws JSONException {
        return new JSONObject(text);
    }
    @Nullable
    public Document toHtml(){
        return Jsoup.parse(this.text);
    }
    @androidx.annotation.NonNull
    @Override
    public String toString(){
        return text;
    }
}