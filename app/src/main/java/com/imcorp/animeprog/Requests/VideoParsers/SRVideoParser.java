package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SRVideoParser extends SimpleVideoParser {
    /* Парсер плеера sr - SovetRomantica
        У него прицип такой:
         1) находим m3u8 файл из javascript -                                   parseM3U8FromFrame
         2) Впиринципе, этот файл играбельный - ставим ему VideoType auto
         3) Парсим из этого m3u8
     */
    private final Pattern SEARCH_M3U8_URL= Pattern.compile("['|\"]?file['|\"]? ?: ?['|\"](.+?[^\\\\])['|\"]");
    public SRVideoParser(String frame_url, Request request,String url_from){
        super(frame_url,request,url_from);
    }
    @Override
    public void loadToOneVideoEpisode(OneVideo episode) throws IOException, JSONException {
        String m3u8_url = parseM3U8FromFrame();
        episode.videoQualities.add(new OneVideoEpisodeQuality(m3u8_url,null,OneVideo.VideoType.AUTO,this.frameUrl));
        loadDataToEpisodeFromM3U8(m3u8_url,episode);
    }
    private String parseM3U8FromFrame() throws IOException {
        String response = this.request.loadFrameFromUrl(this.frameUrl,urlFrom);
        Document doc = Jsoup.parse(response,urlFrom);
        String url_to_return=null;
        for(Element el:doc.select("script")){
            String text = el.data().trim();
            if(text.isEmpty())continue;
            Matcher m = SEARCH_M3U8_URL.matcher(text);
            if(!m.find())continue;
            url_to_return = m.group(1);
            break;
        }
        if(url_to_return==null)throw new InvalidHtmlFormatException("HTML is invalid - m3u8 url not found");
        return url_to_return;
    }
    private void loadDataToEpisodeFromM3U8(String m3u8_url,OneVideo episode) throws IOException {
        String[] m3u8_lines = request.loadTextFromUrl(m3u8_url,false).toString().split("\\r?\\n");
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i=0;i<m3u8_lines.length;i++) if(!m3u8_lines[i].startsWith("#"))indexes.add(i);
        for (int i:indexes) {
            OneVideoEpisodeQuality q = new OneVideoEpisodeQuality();
            q.m3u8Url = getGoodSrc(m3u8_lines[i],m3u8_url);
            q.ref = this.frameUrl;
            if(!q.loadVideoTypeFromM3U8Comment(m3u8_lines[i-1]))continue;
            episode.videoQualities.add(q);
        }
    }

}
