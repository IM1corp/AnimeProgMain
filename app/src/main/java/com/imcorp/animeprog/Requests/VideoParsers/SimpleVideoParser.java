package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class SimpleVideoParser {
    protected String frameUrl;
    protected String urlFrom;
    protected Request request;
    protected SimpleVideoParser(String frame_url,Request request,String url_from){
        this.frameUrl =frame_url;
        this.request=request;
        this.urlFrom = url_from;
    }
    public abstract void loadToOneVideoEpisode(final OneVideo episode) throws IOException, JSONException;
    String getGoodSrc(String src,String parent) throws MalformedURLException {
        if(src.startsWith("//")){
            src = "https:"+src;
        }
        else if(src.startsWith("/")){
            src = "https://"+new URL(parent).getHost() + src;
        }
        else if(!src.startsWith("http")){
            URL url = new URL(parent);
            String final_src = "https://"+url.getHost()+url.getPath();
            if(!final_src.endsWith("/")){
                final_src = final_src.substring(0,final_src.lastIndexOf("/")+1);
            }
            src=final_src+src;
        }
        return src;
    }
}
