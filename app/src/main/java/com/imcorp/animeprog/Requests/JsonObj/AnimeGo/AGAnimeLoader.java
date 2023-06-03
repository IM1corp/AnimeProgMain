package com.imcorp.animeprog.Requests.JsonObj.AnimeGo;

import android.content.Context;
import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.Default.HtmlUtils;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.Http.ReqBuild;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.imcorp.animeprog.Config.loadIntegerFromText;
import static com.imcorp.animeprog.Requests.JsonObj.OneAnime.VideoResponseType.UNDEFINED;
import static com.imcorp.animeprog.Requests.JsonObj.OneAnime.VideoResponseType.VIDEO_NOT_ABLE_IN_YOUR_COUNTRY;
public class AGAnimeLoader {
    private final OneAnime anime;
    private final Document document;
    private final Element animeInfo;
    private final Context mContext;
    private final OneAnime.AnimeAttrs attrs;
    public AGAnimeLoader(String html, String path, Context context){
        this.anime = new OneAnime(Config.HOST_ANIMEGO_ORG);
        this.attrs = this.anime.getAttrs();
        this.anime.setPath( path );
        this.document = Jsoup.parse(html);
        this.animeInfo = document.selectFirst(".anime-info");
        this.mContext = context;
    }
    public OneAnime load() throws IOException {
        if(this.animeInfo==null)throw new InvalidHtmlFormatException("No anime-info class found");
        this.loadTitle();
        this.loadDescription();
        this.loadYear();
        this.loadCover();
        this.loadViewingOrder();
        this.loadVideos();
        this.loadData();
        this.loadSynonyms();
        this.loadCommentsUrl();
        this.anime.setFullyLoaded(true);

        return anime;
    }
    private void loadSynonyms() {
        Elements strings = document.select(".synonyms li");
        if(strings.size()!=0)
            attrs.synonyms = ArrayUtilsKt.selectByFunc(strings, Element::text);
    }
    public static void loadGenresToAnime(@Nullable Element info_genres, OneAnime.AnimeAttrs attrs){
        Elements genres = info_genres!=null?info_genres.select("a"):null;
        if(genres!=null) {
            attrs.genres = new ArrayList<>(genres.size());
            for (Element el : genres)
                attrs.genres.add(new OneAnime.Link(el));
        }
    }
    private void loadData() {
        final Element info_rating = getInfoFromKeys("Возвраст","ограничения"),
                info_genres = getInfoFromKeys("Жанр"),
                studio = getInfoFromKeys("Студия"),
                issue_date = getInfoFromKeys("Выпуск"),
                episodes = getInfoFromKeys("Эпизод"),
                original_source = getInfoFromKeys("Первоисточник"),
                status= getInfoFromKeys("Статус");
        attrs.rating = info_rating!=null?info_rating.text():"";
        loadGenresToAnime(info_genres,attrs);
        if(studio!=null){
            final Element studioA=studio.selectFirst("a");
            if(studioA!=null)
                attrs.setStudios(new ArrayList<OneAnime.Link>(){{this.add(new OneAnime.Link(studioA)); }});
            else attrs.setStudios(new ArrayList<OneAnime.Link>(){{this.add(OneAnime.Link.noHref(studio.text())); }});
        }
        if(issue_date!=null){
            attrs.issueDate = issue_date.text();
        }
        if(episodes!=null){
            attrs.epCount = loadIntegerFromText(episodes.text(),0);
        }
        if(original_source!=null){
            attrs.originalSource = original_source.text();
        }
        if(status!=null){
            anime.status = status.text();
        }
    }
    private void loadVideos() throws IOException {
        OneAnime.VideoResponseType responseType = OneAnime.VideoResponseType.UNDEFINED;
        try{
            Element dropdown = document.selectFirst(".dropdown");
            if(dropdown==null){
                responseType = OneAnime.VideoResponseType.VIDEO_NOT_ABLE_IN_YOUR_COUNTRY;
                throw new InvalidHtmlFormatException("Invalid html format - dropdown not found");
            }
            String url = dropdown.attr("data-ajax-url"),
                    params = dropdown.attr("data-ajax-appear-params");
            if(url.startsWith("//"))url = "https:"+url;
            else if(url.startsWith("/"))url = Config.ANIMEGO_URL+url;
            HashMap<String,String> get_params= new HashMap<>();
            if(params!=null&&!params.isEmpty()){
                JSONObject p = new JSONObject(params);
                for (Iterator<String> it = p.keys(); it.hasNext(); ) {
                    final String key = it.next();
                    get_params.put(key,p.get(key).toString());
                }
            }
            ReqBuild request = new ReqBuild(mContext,url,false)
                    .addXRequestsWithHeader()
                    .add(get_params);
            String json = request.SendRequest().toString();
            responseType = this.loadVideosDataFromJson(json);
        }catch (InvalidHtmlFormatException | JSONException ignored){
            if(Config.NEED_LOG) Log.e(Config.LOG_HTML_PARSE,"Can not find videos in json");
        }
        anime.responseType = responseType;
    }
    private OneAnime.VideoResponseType loadVideosDataFromJson(final String json) throws JSONException {
        final JSONObject json_obj = new JSONObject(json);
        final String html = json_obj.getString("content");
        Document doc = Jsoup.parse(html);
        Elements episodes = doc.getElementsByClass("video-player-bar-series-item"),
            player_items = doc.getElementsByClass("video-player-toggle-item");
        if(episodes.size()==0){
            if(player_items.size()==0){
                return parseErrorFromContentEl(doc);
            }
            Element el = new Element("span");
            el.addClass("video-player__active");
            episodes = new Elements();
            episodes.add(el);
        }
        for(Element el:episodes){
            final String data_id=el.attr("data-id"),episode = el.attr("data-episode"),
                    num = episode != null && !episode.isEmpty() ? episode : "1";
            OneVideoEP ep = new OneVideoEP(true);
            ep.num = num;
            ep.selectAble=true;
            if(el.hasClass("video-player__active"))
                try {
                    new AGVideoLoader(ep,doc,player_items.size()).load(mContext);
                }
                catch (InvalidHtmlFormatException ignored) {
                    if(Config.NEED_LOG) Log.e(Config.LOG_TAG,"can not parse video from html");
                }
            ep.keys.put("id",data_id!=null&&!data_id.isEmpty()?data_id:"1");
            ep.keys.put("episode",num);
            ep.keys.put("dubbing",player_items.size()>0?player_items.first().attr("data-provide-dubbing"):"1");
            ep.keys.put("provider",player_items.size()>0?player_items.first().attr("data-provider"):"1");
            anime.videos.add(ep);
        }

        return OneAnime.VideoResponseType.SUCCESS;
    }
    private void loadViewingOrder() {
        Elements watches = this.document.select(".seasons-container .seasons-item");
        if(watches==null||watches.size()==0)return;
        anime.viewingOrder =new ArrayList<>(watches.size());
        for(Element el:watches){
            OneAnime viewingOrderAnime = new OneAnime(Config.HOST_ANIMEGO_ORG);
            Element title_el = el.selectFirst(".seasons-item-name a"),
                    data_year = el.selectFirst(".seasons-item-info");
            if (title_el == null || data_year == null) continue;
                viewingOrderAnime.title = title_el.text();
                viewingOrderAnime.setPath(title_el.attr("href"));
                viewingOrderAnime.year = loadIntegerFromText(data_year.text(), 4);
                viewingOrderAnime.description = el.select(".seasons-item-info").text();

                anime.viewingOrder.add(viewingOrderAnime);
            }
        }
    private void loadTitle(){
        this.anime.title = this.document.selectFirst(".anime-title h1").text();
    }
    private void loadDescription(){
        Element el = document.selectFirst(".description");
        anime.description = el==null?mContext.getString(R.string.not_found):HtmlUtils.INSTANCE.getTextWithNewLines(el);
    }
    private void loadYear(){
        Element el = getInfoFromKeys("Сезон","Выпуск");
        if(el!=null) {
            String text = el.text();
            anime.year = loadIntegerFromText(text,4);
        }else this.anime.year=0;
    }
    private void loadCover(){
        Element image = this.document.selectFirst(".anime-poster img");
        if(image!=null){
            this.anime.setCover(image.attr("src"));
        }
    }
    private void loadCommentsUrl(){
        Element commentsUrl = this.document.selectFirst("#begin-comments");
        if(commentsUrl!=null){
            String url = commentsUrl.attr("data-ajax-url");
            if(url == null)
                url = Config.ANIMEGO_URL+"/comment/"+commentsUrl.attr("data-thread-init")+"/1/show";
            anime.comments.setDefaultUrl(url);
        }
    }
    @Nullable
    private Element getInfoFromKeys(final String ...keys){
        Element el=null;
        for (String key:keys) {
            el = animeInfo.selectFirst("dt:containsOwn("+key+") + dd");
            if(el!=null)break;
        }
        return el;
    }
    private static boolean containsError(final String text,final String ...titles){
        for(String i:titles)
            if(text.contains(i)) return true;
        return false;
    }
    private static OneAnime.VideoResponseType parseErrorFromContentEl(Element doc){
        final Element block_el =  doc.selectFirst(".player-blocked");
        if(block_el!=null) {
            final String text = block_el.text().toLowerCase();
            if (containsError(text, "недоступно", "российской федерации", "россии", "на территории", "запрещено"))
                return VIDEO_NOT_ABLE_IN_YOUR_COUNTRY;
        }
        return UNDEFINED;
    }
}
