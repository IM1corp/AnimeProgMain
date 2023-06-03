package com.imcorp.animeprog.Requests.JsonObj.AnimeGo;

import android.content.Context;

import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class AGVideoLoader {
    private final OneVideoEP where;
    private final ArrayList<OneVideo> list;
    private final Element document;
    private final int count;
    private Element players;
    public AGVideoLoader(final OneVideoEP where, final JSONObject data) throws JSONException {
        this.where=where;
        this.list=where.initVideos(count =data.has("numVideos")?data.getInt("numVideos"):0);
        this.document = Jsoup.parse(data.getString("content"));
    }
    public AGVideoLoader(final OneVideoEP where,final Element parent,final int count){
        this.list = where.initVideos(this.count =count);
        this.document = parent;
        this.where = where;
    }

    public ArrayList<OneVideo> load(Context context) throws InvalidHtmlFormatException {
        final Elements dubbings = document.select("#video-dubbing .video-player-toggle-item");
        if(dubbings==null||dubbings.size()==0){
            if(this.count==0)throw new InvalidHtmlFormatException.NoVideosFoundException(context.getString(R.string.no_videos_found));
            throw new InvalidHtmlFormatException("Html is invalid - no dubbing element found");
        }
        players = document.selectFirst("#video-players");
        for(Element el:dubbings){
            this.loadOneDubbing(el);
        }
        return this.list;
    }
    private void loadOneDubbing(final Element dubbing_el){
        final String dubbing_index = dubbing_el.attr("data-dubbing"),
                title = dubbing_el.text();
        final Elements players_el = players.select("span[data-provide-dubbing='"+dubbing_index+"']");

        for (Element el:players_el) {
            OneVideo video = new OneVideo(where.num);
            video.voiceStudio = title;
            video.urlFrame = el.attr("data-player");
            video.loadOneVideoPlayerFromUrl();
            this.list.add(video);
        }
    }
}
