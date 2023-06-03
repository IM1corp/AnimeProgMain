package com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser;

import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;
import com.imcorp.animeprog.Requests.JsonObj.Video.Subtitle;
import com.imcorp.animeprog.Requests.VideoParsers.SimpleVideoParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllohaVideoParser extends SimpleVideoParser {
    final static String FILE_SEPARATOR = "##";
    private final static Pattern FIND_FILE = Pattern.compile("new Playerjs ?\\([\"|'](.+?)[\"|']\\)");
    private final static Pattern MORE_EPISODES_JSON = Pattern.compile("serial ?= ?['|\"]");
    private final static Pattern FIND_URL = Pattern.compile("(https://.+?);");
    private String data, token=null,file=null;

    public AllohaVideoParser(String frame_url, Request request, String url_from, OneVideo video) {
        super(frame_url, request, url_from);
    }
    @Override public void loadToOneVideoEpisode(final OneVideo episode) throws IOException, JSONException {
        final Document doc = Jsoup.parse(this.request.loadFrameFromUrl(frameUrl,urlFrom));
        final EvalDecoder decoder = new EvalDecoder();
        for (Element el:doc.getElementsByTag("script")) {
            final String data = el.data().trim();
            if(data.isEmpty())continue;
            decoder.decodeFile(data);
        }
        this.data = decoder.toString();
        this.loadOneEpisodeFromJs(episode);
    }
    private void loadOneEpisodeFromJs(final OneVideo episode) throws InvalidHtmlFormatException {
        Matcher matcher = FIND_FILE.matcher(data);
        if(!matcher.find()|| matcher.groupCount()!=1)throw new InvalidHtmlFormatException("Invalid js - file not found");

        ParsedData data = parseData(file=matcher.group(1));
        loadDataToOneEpisode(episode,data);
        tryLoadMoreEpisodesFromData();
    }
    //region LoadMoreEpisodes
    private void tryLoadMoreEpisodesFromData()  {
//
//        Matcher m = MORE_EPISODES_JSON.matcher(data);
//        if(m.find()){
//            final int m_from = m.start();
//            try {
//                RESP resp = loadSeasonFromData((JSONObject) JsonParser.loadJsonFromJs(data.substring(m_from)));
//                if(resp==null)throw new JSONException("Season is undefined");
//
//                for (Iterator<String> it = resp.season.keys(); it.hasNext(); ) {
//                    String episode_key = it.next();
//                    int ep_num = Integer.parseInt(episode_key);
//                    JSONObject episode = resp.season.getJSONObject(episode_key);
//                    JSONObject one_video_ep = null;
//                    for(Iterator<String> its = episode.keys(); its.hasNext(); ){
//                        JSONObject v = episode.getJSONObject(its.next());
//                        if(v.getString("translation").equals(resp.voice_studio)){
//                            one_video_ep = v;
//                            break;
//                        }
//                    }
//                    if(one_video_ep==null)continue;
//                    while (episodes.size()<ep_num){
//                        OneVideo ep = new OneVideo(null,episodes.size()+1, OneVideo.OneVideoPlayer.ALLOHA);
//                        episodes.add(ep);
//                    }
//                    OneVideoEpisode ep = episodes.get(ep_num-1);
//                    if(ep.url_frame==null)
//                        ep.url_frame = frameUrl.replaceAll("([&|?]episode)=(\\d+)","$1="+episode_key);
//                    if(!one_video_ep.getString("player").equals(file))
//                        loadDataToOneEpisode(ep,parseUrlFromData(one_video_ep.getString("player")));
//
//                }
//
//            } catch (ClassCastException|JSONException|InvalidHtmlFormatException|NumberFormatException ignored){
//                Log.w("Warning","Can not find json in js");
//            }
//        }
        //TODO: edit
    }
    private RESP loadSeasonFromData(final JSONObject jsonObject) throws JSONException {
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            JSONObject season = jsonObject.getJSONObject(it.next());

            for (Iterator<String> iter = season.keys(); iter.hasNext(); ) {
                String episodeStr = iter.next();
                JSONObject episode = season.getJSONObject(episodeStr);
                for (Iterator<String> iterator = episode.keys(); iterator.hasNext(); ) {
                    JSONObject voice_studio_obj = episode.getJSONObject(iterator.next());
                    if(voice_studio_obj.getString("player").equals(file)){
                        RESP resp = new RESP();
                        resp.season = season;
                        resp.voice_studio = voice_studio_obj.getString("translation");
                        resp.episode_num=Integer.parseInt(episodeStr);
                        return resp;
                    }
                }
            }

        }
        return null;
    }
    private class RESP{
        JSONObject season;
        String voice_studio;
        int episode_num;
    }
    //endregion
    private Set<String> getToken() throws InvalidHtmlFormatException {
        // String eval_thing = JsonParser.findArgs(data.substring(data.indexOf("eval")),false);
        // final String code = new EvalDecoder("eval"+eval_thing).decode();
        final Matcher matcher = Pattern.compile("['|\"]([a-z0-9]{30})['|\"]").matcher(this.data);
        if (!matcher.find() || matcher.groupCount() != 1) throw new InvalidHtmlFormatException("Password not found in html");
        final Set<String> ans = new HashSet<>();
        while (matcher.find())
            ans.add(matcher.group(1));
        return ans;
    }
    private String decryptData(final String datas) throws UnsupportedEncodingException, GeneralSecurityException, InvalidHtmlFormatException, JSONException {
        SimpleDataDecrypter decrypter = null;
        if(new AESDecrypt().checkIf(datas)){
            decrypter = new AESDecrypt(getToken());
        }
        else if(new BASE64Decrypt().checkIf(datas)){
            decrypter = new BASE64Decrypt();
        }
        if(decrypter==null)throw new InvalidHtmlFormatException("Undefined decryption method");
        return decrypter.decrypt(datas);
    }
    private ParsedData parseData(final String data) throws InvalidHtmlFormatException {
        try {
            ParsedData ans = new ParsedData();
            final JSONObject data_json = new JSONObject(decryptData(data));
            final String fileS = data_json.getString("file"),
                    subtitleS = data_json.has("subtitle")?data_json.getString("subtitle"):null;
            Matcher m = FIND_URL.matcher(fileS);
            String file = (m.find()? fileS: decryptData(fileS)).replaceAll("[\"|\\\\]","");
            for (String i : file.split(";")) {
                int index0 = i.indexOf('{'),
                        index1 = i.indexOf('}');
                if(index0 !=-1 && index1 !=-1) {
                    final String dataName = i.substring(index0+1,index1),
                            dataUrl = i.substring(i.indexOf("http"));
                    ans.urls.add(dataUrl);
                    ans.titles.add(dataName);
                }
                else{
                    ans.urls.add(i);
                    ans.titles.add("");
                }
            }
            if (subtitleS != null) {
                try{
                    m = FIND_URL.matcher(subtitleS);
                    final String subUrl = (m.find()?m.group(1): decryptData(subtitleS)).replaceAll("[\"|\\\\]",""),
                        subData = this.request.loadTextFromUrl(subUrl,false, frameUrl).toString();
                    final String[] splited = subData.split("\\r?\\n|,");
                    ans.subtitles = new ArrayList<>(splited.length);
                    for (String i : splited) {
                        final String dataName = i.substring(i.indexOf('[')+1,i.indexOf(']')),
                               dataUrl = i.substring(i.indexOf("http"));
                        ans.subtitles.add(new Subtitle(dataUrl, dataName));
                    }

                }catch (Exception e){
                    if(Config.NEED_LOG) Log.e(Config.ALLOHA_VIDEO_PARSER_LOG,e.getMessage(),e);
                }
            }
            return ans;
        }
        catch (GeneralSecurityException|UnsupportedEncodingException|AssertionError|JSONException e) {
            throw new InvalidHtmlFormatException("HTML is invalid - can not decrypt url");
        }
    }
    private void loadDataToOneEpisode(final OneVideo episode, final ParsedData data){
        for(int i=0;i<data.titles.size();i++){
            OneVideoEpisodeQuality ep = new OneVideoEpisodeQuality(data.urls.get(i),null, OneVideo.VideoType.UNDEFINED, this.frameUrl);
            ep.qualityTitle = data.titles.get(i);
            episode.videoQualities.add(ep);
        }
        episode.videoSubtitles = data.subtitles;
    }
    private static class ParsedData{
        public ArrayList<Subtitle> subtitles;
        public ArrayList<String> urls = new ArrayList<String>();
        public ArrayList<String> titles = new ArrayList<String>();
    }
}
