package com.imcorp.animeprog.Requests.VideoParsers;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieVideoParser extends SimpleVideoParser {
    private static final Pattern p = Pattern.compile("CreatePlayer\\( ?[\"|']([^'\"]+?)[\"|'] ");
    public MovieVideoParser(String frame_url, Request request, String url_from) {
        super(frame_url, request, url_from);
    }
    @Override
    public void loadToOneVideoEpisode(OneVideo episode) throws IOException, JSONException {
        final String url = getUrlFromData(getDataFromUrl());
        episode.videoQualities.add(new OneVideoEpisodeQuality(null,url, OneVideo.VideoType.UNDEFINED,frameUrl));
    }

    private String getDataFromUrl() throws IOException {
        String html = this.request.loadFrameFromUrl(frameUrl,urlFrom);
        Matcher m = p.matcher(html);
        if(!m.find()||m.groupCount()!=1)throw new InvalidHtmlFormatException("Html is invalid - url not found");
        return m.group(1);
        //'v=https%3a%2f%2fmyvi.top%2fstream%2fHLLIyZdcuUevwMzcwA7t1g2%2f2.mp4%3fs%3d0GqhXadXruqHb7hRaNJOwrC_ygXJwCwFwnUb5SK-U8aMw2OxYDYFni4JJ0T4_dXvY9tFWAdP9BTvf2wFO_5dBA2%26r%3djbxN9FCU6cNF2ASY5WfMLBt_7KhAc6JyANTeD5gowfB3p1SFqqpqJVkbYFvsVAuq0%26d%3d1500249%26ri%3dqy_iBGDzJ0GpcYylOEt5sg2\u0026tp=video%2fmp4\u0026i=d13ct1czm1hwxm6y3uqcydzp4a\u0026ea=true\u0026ai=14-3-3\u0026ac=12\u0026au=https%3a%2f%2fwww.myvi.top%2fembed%2fd13ct1czm1hwxm6y3uqcydzp4a\u0026pb=%2cpl291%2c114786%2cpl292%2c114786\u0026hb=always\u0026sn=\u0026sp=\u0026e=https%3a%2f%2fwww.myvi.top%2fembed%2fd13ct1czm1hwxm6y3uqcydzp4a\u0026t=Kaguya-sama+wa+Kokurasetai+Tensai-tachi+no+Ren%60ai+Zunousen+01+(AniLibria)\u0026p=https%3a%2f%2ffs129.myvi.tv%3a8092%2fVB%2fkC%2fAB%2fMA%2fAA%2fAB%2f0%2ftm1.jpg%3fr%3du0mMe9xdRIpa8P7vEX88lksATC-WNZUcU6_b4C0SM9lD9zCwMwKH54J1BIK2KrH90\u0026d=1500.249\u0026u=https%3a%2f%2fwww.myvi.top%2fidnc%3fv%3dd13ct1czm1hwxm6y3uqcydzp4a\u0026o=https%3a%2f%2fapi.myvi.tv\u0026a=%2fapi%2f1.0%2fauth\u0026tl=https%3a%2f%2fmyvi.top%2ftrack%2fload%3fv%3dHLLIyZdcuUevwMzcwA7t1g2\u0026tv=https%3a%2f%2fmyvi.top%2ftrack%2fplay%3fv%3dHLLIyZdcuUevwMzcwA7t1g2\u0026tn=Player\u0026ti=UA-75173616-2\u0026ec=Video\u0026el=anime\u0026ci=17199490\u0026cg=anime\u0026pl=", "//myvi.top/assets/libs/player/", "https://animego.org/anime/gospozha-kaguya-v-lyubvi-kak-na-voyne-r821'.split('&').map(function(i){let a = {};a[i.split('=')[0]]=decodeURIComponent(i.split('=')[1]);return a})

    }
    private String getUrlFromData(final String data) throws InvalidHtmlFormatException, UnsupportedEncodingException {
        String[] splited = data.split("&");
        for (final String item:splited) {
            final String[] key_value = item.split("=");
            if(key_value.length<2)throw new InvalidHtmlFormatException("Html is invalid - can not parse url from html");
            if(key_value[0].equals("v")){
                return URLDecoder.decode(key_value[1], Config.coding);
            }
        }
        throw new InvalidHtmlFormatException("Html is invalid - can not parse url from html");
    }
    //МУВИ

}
