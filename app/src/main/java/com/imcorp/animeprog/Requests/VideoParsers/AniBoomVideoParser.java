package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class AniBoomVideoParser extends SimpleVideoParser {
    private Document document;
    public AniBoomVideoParser(String frame_url, Request request, String url_from) {
        super(frame_url, request, url_from);
    }
    @Override
    public void loadToOneVideoEpisode(OneVideo episode) throws IOException, JSONException {
        this.loadDocument();
        final String url = getM3U8Url();
        episode.videoQualities.add(new OneVideoEpisodeQuality(url,null, OneVideo.VideoType.UNDEFINED,this.urlFrom));
    }
    private String getM3U8Url() throws IOException, JSONException {
        Element id = document.selectFirst("#video");
        final String json;
        if(id==null||(json = id.dataset().get("parameters"))==null)throw new InvalidHtmlFormatException("Html is invalid - video element not found");
        JSONObject j = new JSONObject(json);
        JSONObject data = new JSONObject(j.getString("hls"));
        final String ans =  data.getString("src");
        return ans;//.replace("master.m3u8","media_0.m3u8");
    }
    private void loadDocument() throws IOException {
        String html = this.request.loadFrameFromUrl(frameUrl,urlFrom);
        this.urlFrom = frameUrl;
        document = Jsoup.parse(html);
    }
}
