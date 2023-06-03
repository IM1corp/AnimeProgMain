package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SibnetVideoParser extends SimpleVideoParser {
    private final Pattern SRC_PATTERN = Pattern.compile("['|\"]?src['|\"]?: ?['|\"](.+?)['|\"]");
    public SibnetVideoParser(String frame_url, Request request,String url_from){
        super(frame_url,request,url_from);
    }
    @Override public void loadToOneVideoEpisode(OneVideo episode) throws IOException {
        String mp4_url = this.parseSrcFromUrl();
        OneVideoEpisodeQuality q = new OneVideoEpisodeQuality();
        q.quality = OneVideo.VideoType.UNDEFINED;
        q.mp4Url = mp4_url;
        q.ref = frameUrl;
        episode.videoQualities.add(q);
    }
    private String  parseSrcFromUrl() throws IOException {
        String frame_response =  request.loadFrameFromUrl(frameUrl,urlFrom);
        Matcher m = SRC_PATTERN.matcher(frame_response);
        String mp4_url;
        if(!m.find()||(mp4_url=m.group(1))==null)throw new InvalidHtmlFormatException("HTML is invalid - src not found");
        return getGoodSrc(mp4_url, frameUrl);
    }
}
