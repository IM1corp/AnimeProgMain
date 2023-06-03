package com.imcorp.animeprog.Requests.JsonObj.AnimeGo;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class AGSearchLoader {
    public static ArrayList<OneAnime.OneAnimeWithId> fromJsonAnimego(final JSONObject response) throws JSONException {
        String content = response.getString("content");
        return fromHtml(content);
    }
    public static ArrayList<OneAnime.OneAnimeWithId> fromHtml(final String html){
        Document d = Jsoup.parse(html);
        Elements els;
        ArrayList<OneAnime.OneAnimeWithId> answer;
        if((els=d.select(".animes-grid-item")).size()!=0){
            answer = new ArrayList<>(els.size());
            for(Element el:els) {
                Element href =    el.selectFirst(".animes-grid-item-body a"),
                        year_el = el.selectFirst(".anime-year"),
                        img_el =  el.selectFirst(".anime-grid-lazy");
                String url = href.attr("href"),
                        name = href.ownText(),
                        year_text =year_el.text(),
                        img_url = img_el.dataset().get("original"),
                        description = el.select(".text-truncate").eq(1).text();
                int year;
                try{year=Integer.parseInt(year_text);}catch (NumberFormatException ignored){year = 0;}
                answer.add(OneAnime.fromSearch(Config.HOST_ANIMEGO_ORG,year,url,name,img_url,description));
            }
        }else{
            els = d.select(".result-search-anime .media");
            answer = new ArrayList<>(els.size());
            for(Element el:els){
                Element href = el.selectFirst("a"),
                        year_el = el.selectFirst(".anime-year"),
                        img_el = el.selectFirst("img");
                final String url = href!=null?href.attr("href"):"",
                        name = href!=null?href.ownText():"",
                        year_text = year_el!=null?year_el.text():"",
                        img_url = img_el!=null?img_el.attr("src"):"",
                        description = el.getElementsByClass("text-truncate").eq(1).text();
                int year=0;
                try{year=Integer.parseInt(year_text);}catch (NumberFormatException ignored){}
                answer.add(OneAnime.fromSearch(Config.HOST_ANIMEGO_ORG,year,url,name,img_url,description));
            }
        }

        return answer;
    }

}