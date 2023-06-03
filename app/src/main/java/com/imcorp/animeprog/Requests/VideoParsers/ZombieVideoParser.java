package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static com.imcorp.animeprog.Requests.JsonObj.System.JsonParser.loadJsonFromJs;

public class ZombieVideoParser extends SimpleVideoParser {
    private ArrayList<OneVideo> episodes;
    public ZombieVideoParser(String frame_url, Request request, String url_from, OneVideo video){
        super(frame_url,request,url_from);
        this.episodes =null;//TODO: video.;
    }
    @Override
    public void loadToOneVideoEpisode(OneVideo episode) throws IOException, JSONException {
        String frame_html = request.loadFrameFromUrl(frameUrl,urlFrom);
        Document doc = Jsoup.parse(frame_html);
        for(Element script:doc.body().select("script")){
            String text = script.data().trim();
            if(text.isEmpty())continue;
            if(text.contains("hlsList")&&text.contains("playlist")){
                loadOneVideoFromJs(text,episode);
                break;
            }
        }
    }
    private void loadOneVideoFromJs(String text,OneVideo episode) throws JSONException, InvalidHtmlFormatException {
        JSONObject json_episode = (JSONObject) loadJsonFromJs(text,"hlsList");
        if(json_episode.length()==0)throw new InvalidHtmlFormatException("HTML is invalid - no json in script");
        loadOneVideoEpisodeFromJson(json_episode,episode);
        JSONArray jsonSeasons = (JSONArray)loadJsonFromJs(text,"playlist","seasons");
        JSONArray episodes_=null;
        boolean b=false;
        for (int i=0;i<jsonSeasons.length();i++) {
            episodes_ = jsonSeasons.getJSONObject(i).getJSONArray("episodes");
            for(int j=0;j<episodes_.length();j++){
                JSONObject one_episode = episodes_.getJSONObject(j);
                if(checkEpisode(one_episode.getJSONObject("hlsList"),json_episode)){
                    b=true;
                    break;
                }
            }
            if(b)break;
        }
        if(b){
            for(int j=0;j<episodes_.length();j++){
                JSONObject one_episode = episodes_.getJSONObject(j);
                loadOneVideoEpisodeFromJson(one_episode, episodes.get(j));
            }
        }
    }
    private void loadOneVideoEpisodeFromJson(JSONObject jsonObject,OneVideo episode) throws JSONException {
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String key = it.next();
            episode.videoQualities.add(new OneVideoEpisodeQuality(jsonObject.getString(key),null,key,this.frameUrl));
        }
    }
    private boolean checkEpisode(JSONObject one_episode,JSONObject is) throws JSONException {
        for (Iterator<String> it = one_episode.keys(); it.hasNext(); ) {
            String key = it.next();
            if(is.has(key)&&is.getString(key).equals(one_episode.getString(key)))
                return true;
        }
        return false;
    }
}
