package com.imcorp.animeprog.Requests.JsonObj.System;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    private final static char[] DEFAULT_END = {'}',']'};
    private final static char[] DEFAULT_START = {'{','['};
    private final static char[] DEFAULT_ARGS_START= {'('};
    private final static char[] DEFAULT_ARGS_END= {')'};

    public static Object loadJsonFromJs(final String js,final String ...splited) throws JSONException {
        return loadJsonFromJs(js,DEFAULT_START,DEFAULT_END,splited,true);
    }
    public static String findArgs(final String js,final boolean needArgs) throws JSONException {
        /*if(backwards){
            return (String)loadJsonFromJs(js,DEFAULT_ARGS_END,DEFAULT_ARGS_START,null,false,false);
        } else {*/
        final String data_with_args = (String)loadJsonFromJs(js,DEFAULT_ARGS_START,DEFAULT_ARGS_END,null,true,false);
        if(!needArgs || data_with_args.isEmpty())return data_with_args;
        else {
            final String only_args = (String) loadJsonFromJs(data_with_args.substring(0, data_with_args.length() - 1), DEFAULT_ARGS_END, DEFAULT_ARGS_START, null, false, false);
            return only_args;
        }
        //}

    }
    public static Object loadJsonFromJs(String js, final char[] find_start, final char[] find_end, @Nullable final String[] splited, boolean forward) throws JSONException {
        return loadJsonFromJs(js,find_start,find_end,splited,forward,true);
    }
    public static Object loadJsonFromJs(String js, final char[] find_start, final char[] find_end, @Nullable final String[] splited, boolean forward,
                                        final boolean needJson) throws JSONException {
        int index = 0;
        boolean in_s = false;
        int js_index = forward?0:js.length()-1;
        if(splited!=null)
            for (String i:splited) {
                if(!i.isEmpty())
                    js_index = forward?js.indexOf(i,js_index):js.lastIndexOf(i,js_index);
            }
        if(js_index==-1) js_index = 0;
        for (int i = js_index;i < js.length()&&i>=0;i+=(forward?1:-1))  {
            char ch = js.charAt(i);
            boolean breaks = false;
            for(char start:find_start){
                if (start==ch) {
                    js = forward?js.substring(i):js.substring(0,i+1);
                    breaks=true;
                    break;
                }
            }
            if(breaks)break;

        }
        char item, last_item = '_';
        for (int i = forward?0:js.length()-1;i < js.length()&&i>=0;i+=(forward?1:-1)) {
            if(!forward)last_item= js.charAt(i-1);
            item = js.charAt(i);
            if (item == '"' ||item=='\'' && last_item != '\\') in_s = !in_s;
            else if(!in_s){
                boolean trys = true;
                for (char ch:find_start) {
                    if(ch==item){
                        index+=1;
                        trys=false;
                    }
                }
                if (trys)
                    for (char ch:find_end)
                        if(ch==item) {
                            index-=1;
                            break;
                        }
            }
            if(forward) last_item = item;
            if (index == 0) {
                String json = forward?js.substring(0, i + 1):js.substring(i);
                return needJson?(json.startsWith("{")?new JSONObject(json):(json.startsWith("[")?new JSONArray(json):json)):json;
            }
        }
        return needJson?new JSONObject():"";
    }
}
