package com.imcorp.animeprog.Requests.Http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.Default.SimpleService;
import com.imcorp.animeprog.Requests.JsonObj.AnimeGo.AGAnimeLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeGo.AGMainPageLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeGo.AGSearchLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeGo.AGVideoLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeJoy.AJAnimeLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeJoy.AJMainPageLoader;
import com.imcorp.animeprog.Requests.JsonObj.AnimeJoy.AJSearchLoader;
import com.imcorp.animeprog.Requests.JsonObj.GogoAnime.GAMainPageLoader;
import com.imcorp.animeprog.Requests.JsonObj.GogoAnime.GASearchLoader;
import com.imcorp.animeprog.Requests.JsonObj.GogoAnime.GAVideoLoader;
import com.imcorp.animeprog.Requests.JsonObj.GogoAnime.GAnimeLoader;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YAnimeLoader;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YSearchLoader;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YummyRestApi;
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.profile.YummyUser;
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.NotImplementedError;

public class Request {
    private final Context mContext;
    private final YummyRestApi mYummyRestApi;
    public Request(Context context){
        this.mContext = context;
        if(context instanceof MyApp)
            mYummyRestApi = new YummyRestApi(this, ((MyApp) context).dataBase);
        else if(context instanceof SimpleService)
            mYummyRestApi = new YummyRestApi(this, ((SimpleService) context).dataBase);
        else
            mYummyRestApi = new YummyRestApi(this, null);
    }
    public MainPage getMainPage(byte host) throws IOException {
        switch (host){
            case Config.HOST_ANIMEGO_ORG:
                return new AGMainPageLoader(
                        new ReqBuild(mContext,Config.ANIMEGO_URL,false)
                                .SendRequest()
                                .toHtml()
                ).load();
            case Config.HOST_YUMMY_ANIME:
//                return new YMainPageLoader(
//                        new ReqBuild(mContext, Config.YUMMY_ANIME_URL,false)
//                                .SendRequest()
//                                .toHtml()
//                ).load();
                return mYummyRestApi.getFeed()
                        .getMainPage()
                        .toMainPage(this.mContext.getResources());
            case Config.HOST_ANIME_JOY:
                return AJMainPageLoader.load(
                        new ReqBuild(mContext, Config.ANIMEJOY_URL,false)
                        .SendRequest()
                        .toHtml()
                );
            case Config.HOST_GOGO_ANIME:
                return GAMainPageLoader.load(
                        new ReqBuild(mContext, Config.GOGOANIME_URL,false)
                                .SendRequest()
                                .toHtml(), mContext

                );

        }
        throw new IOException("Invalid host");
    }
    public ArrayList<OneAnime.OneAnimeWithId> searchJson(String word,byte host) throws IOException, JSONException {
        switch (host){
            case Config.HOST_ANIMEGO_ORG:
                return AGSearchLoader.fromJsonAnimego(
                        new ReqBuild(mContext,Config.ANIMEGO_URL +Config.ANIMEGO_SEARCH_PATH+"all",false)
                                .add("q",word)
                                .add("type","small")
                                .add("_",System.currentTimeMillis())
                                .addXRequestsWithHeader()
                                .SendRequest()
                                .toJson()
                );
            case Config.HOST_YUMMY_ANIME:
                return YSearchLoader.fromJson(
                        new ReqBuild(mContext,Config.YUMMY_ANIME_URL+"/get-search-list/",false)
                                .add("word",word)
                                .addXRequestsWithHeader()
                                .SendRequest()
                                .toJson()
                );
            case Config.HOST_ANIME_JOY:
                return AJSearchLoader.INSTANCE.loadDataFromHtml(
                        new ReqBuild(mContext,Config.ANIMEJOY_URL+"/index.php",true)
                        .add("do","search")
                        .addPost("do","search")
                        .addPost("subaction","search")
                        .add("search_start",0)
                        .add("full_search",0)
                        .add("result_from",1)
                        .add("story",word)
                        .SendRequest().toHtml()
                );
            case Config.HOST_GOGO_ANIME:
                return GASearchLoader.fromJson(
                        new ReqBuild(mContext, "https://ajax.gogo-load.com/site/loadAjaxSearch", false)
                                .add("keyword", word)
                                .add("id",-1)
                                .add("link_web", Config.GOGOANIME_URL+"/")
                                .addHeader("origin", Config.GOGOANIME_URL)
                                .addHeader("referer", Config.GOGOANIME_URL+"/")
                                .SendRequest().toJson()
                );
        }
        return null;
    }
    public ArrayList<OneAnime.OneAnimeWithId> searchBig(String word,int page,byte host) throws IOException, JSONException {
        if(page<1)throw new IllegalArgumentException("page should be more than 1");
        switch (host) {
            case Config.HOST_ANIMEGO_ORG:
                return AGSearchLoader.fromJsonAnimego(
                    new ReqBuild(mContext, Config.ANIMEGO_URL + Config.ANIMEGO_SEARCH_PATH + "anime", false)
                            .add("q", word)
                            .add("type", "list")
                            .add("page", page)
                            .add("_", System.currentTimeMillis())
                            .addXRequestsWithHeader()
                            .SendRequest()
                            .toJson()
                );
            case Config.HOST_YUMMY_ANIME:
                return YSearchLoader.fromJson(
                        new ReqBuild(mContext,Config.YUMMY_ANIME_URL+"/get-search-list/",false)
                                .add("word",word)
                                .add("page",page)
                                .addXRequestsWithHeader()
                                .SendRequest()
                                .toJson()
                );
            case Config.HOST_GOGO_ANIME:
                return GASearchLoader.fromHtml(
                        new ReqBuild(mContext,Config.GOGOANIME_URL+"/search.html", false)
                        .add("keyword", word)
                        .add("page", page)
                        .SendRequest()
                        .toHtml()
                );
            case Config.HOST_ANIME_JOY:
                return this.searchJson(word, host);
        }
        return null;
    }
    public OneAnime loadAnimeFromPath(String path,final int HOST) throws IOException {
        if(path.startsWith("//"))path = path.substring(2);
        if(path.startsWith("/"))path = path.substring(1);
        if(!path.startsWith("https://")&&!path.startsWith("http://")){
            path = Config.getUrlByHost(HOST) + '/' + path;
        }
        if(path.startsWith(Config.GOGOANIME_URL) && !path.contains("/category/")){
            path = GAMainPageLoader.INSTANCE.getRealUrl(path);
        }
        if(HOST == Config.HOST_YUMMY_ANIME){
            return mYummyRestApi.getAnimes()
                    .getByUrl(path, true)
                    .toOneAnime(this.mContext.getResources());
        }
        OneAnime answer=null;
        ReqResponse resp = new ReqBuild(mContext,path,false).SendRequest();

        switch (HOST){
            case Config.HOST_ANIMEGO_ORG:
                if(!path.startsWith(Config.ANIMEGO_URL+"/anime/"))
                    throw new InvalidHtmlFormatException.InvalidUrl(path,"Anime url at '"+path+"' is invalid");
                answer = new AGAnimeLoader(resp.toString(),path.substring("https://".length()+Config.ANIMEGO_HOST.length()),mContext).load();
                break;
            case Config.HOST_YUMMY_ANIME:
                if(!path.startsWith(Config.YUMMY_ANIME_URL+"/catalog/item") && !path.startsWith(Config.YUMMY_ANIME_URL_2+"/catalog/item"))
                    throw new InvalidHtmlFormatException.InvalidUrl(path,"Anime url at '"+path+"' is invalid");
                Document doc = resp.toHtml();
                if(doc != null)
                    answer = YAnimeLoader.INSTANCE.fromHtml(resp.toHtml(), path, mContext);
                else throw new IOException("Invalid response - try using a proxy server");
                break;
            case Config.HOST_ANIME_JOY:
                if(!path.startsWith(Config.ANIMEJOY_URL))
                    throw new InvalidHtmlFormatException.InvalidUrl(path,"Anime url at `"+path+"` is invalid");
                answer = AJAnimeLoader.INSTANCE.fromHtml(resp.toHtml(), path, mContext);
                break;
            case Config.HOST_GOGO_ANIME:
                if(!path.startsWith(Config.GOGOANIME_URL))
                    throw new InvalidHtmlFormatException.InvalidUrl(path,"Anime url at `"+path+"` is invalid");
                answer = GAnimeLoader.INSTANCE.fromHtml(resp.toHtml(), path, mContext);
                break;
        }
        return answer;
    }
    @Nullable
    public Bitmap loadImageFromUrl(String image) throws IOException {
//        if(image.startsWith(Config.YUMMY_ANIME_URL)){
//            final JummyAnimeAdapter.BlobWebView v = new JummyAnimeAdapter.BlobWebView(mContext, image,Config.YUMMY_ANIME_URL);
//            ((MyApp) mContext).threadCallback.post(v::initilize);
//            try {
//                v.lock();
//                if(v.isError()) throw new IOException(new String(v.answer));
//                //final String pureBase64Encoded = ((String)v.answer).substring(((String)v.answer).indexOf(",") + 1);
//                //final byte[] data = Base64.decode((String)v.answer, Base64.DEFAULT);
//                return BitmapFactory.decodeByteArray(v.answer,0,v.answer.length);
//            }catch (InterruptedException e){
//                throw new InterruptedIOException(e.getMessage());
//            }finally {
//                v.stop();
//            }
//
//        }
//        else {
//            URL url = new URL(image);
//            HttpURLConnection conn = url.openConnection();
            HttpURLConnection conn = new ReqBuild(this.mContext, image, false).initRequest();
            conn.connect();
            return BitmapFactory.decodeStream(conn.getInputStream());
//        }
    }
    public String loadFrameFromUrl(String url, @Nullable String ref) throws IOException {
        ReqBuild r = new ReqBuild(mContext,url,false)
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("dnt", "1")
                .addHeader("sec-fetch-dest","iframe")
                .addHeader("sec-fetch-mode", "navigate")
                .addHeader("sec-fetch-site", "cross-site")
                .addHeader("sec-fetch-user", "?1");
        if(ref!=null){
            r.addHeader("referer", ref);
        }
        return r.SendRequest().toString();

    }
    public ReqResponse loadTextFromUrl(String url,boolean post) throws IOException {
        return loadTextFromUrl(url,post, new HashMap<>(0));
    }
    public ReqResponse loadTextFromUrl(String url,boolean post,@NotNull HashMap<String,String> headers) throws IOException {
        final ReqBuild reqBuild = new ReqBuild(mContext,url,post);
        for (HashMap.Entry<String, String> i:headers.entrySet())
            reqBuild.addHeader(i.getKey(),i.getValue());
        return reqBuild.SendRequest();
    }
    public ReqResponse loadTextFromUrl(String url, boolean post, String referer) throws IOException {
        return loadTextFromUrl(url,post,referer,true);
    }
    public ReqResponse loadTextFromUrl(String url, boolean post, String referer,boolean needXRequestedWith) throws IOException{
        final ReqBuild reqBuild = new ReqBuild(mContext,url,post);
        reqBuild.addHeader("Referer", referer);
        if(needXRequestedWith)reqBuild.addXRequestsWithHeader();
        return reqBuild.SendRequest();
    }
    public ReqResponse loadTextFromUrl(String url, HashMap<String, String> params, HashMap<String, String> headers) throws IOException{
        final ReqBuild reqBuild = new ReqBuild(mContext, url,false);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            reqBuild.add(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            reqBuild.addHeader(entry.getKey(), entry.getValue());
        }

        return reqBuild.SendRequest();
    }

    public ReqResponse post(String url, HashMap<String,String> data) throws IOException {
        ReqBuild r = new ReqBuild(mContext,url,true);
        for (Map.Entry<String, String > entry : data.entrySet()) {
            r.addPost(entry.getKey(),entry.getValue());
        }
        return r.SendRequest();
    }
    public Bitmap loadBigImage(OneAnime anime) throws IOException, JSONException {
        String loadCover=null;
        if(anime.getBigCoverString()==null) {
            JSONObject response = new ReqBuild(mContext, anime.getAnimeURI(), false)
                    .addXRequestsWithHeader()
                    .add("type", "posters")
                    .SendRequest()
                    .toJson();
            if (response.has("content")) {
                String html = response.getString("content");
                Document document = Jsoup.parse(html);
                Element el = document.selectFirst("img");
                if (el != null) {
                    final String cover = el.attr("src");
                    if (cover != null) {
                        anime.setBigCover(cover);
                        loadCover = anime.getBigCoverString();
                    }
                }
            }
        }
        else loadCover = anime.getBigCoverString();
        if(loadCover==null) loadCover = anime.getCover();
        return loadImageFromUrl(loadCover);
    }
    public ArrayList<OneVideo> loadVideosFromEpisodeAG(OneVideoEP episode) throws IOException, JSONException {
        ReqBuild r = new ReqBuild(mContext,Config.URL_GET_MORE_EP_AG,false)
                .addXRequestsWithHeader()
                .add(episode.keys);
        JSONObject j = r.SendRequest().toJson();
        return new AGVideoLoader(episode,j).load(mContext);
    }
    public ArrayList<OneVideo> loadVideosFromEpisodeGA(OneVideoEP episode) throws IOException{
        Document r = new ReqBuild(mContext, episode.keys.get("href"), false)
                .SendRequest()
                .toHtml();
        return GAVideoLoader.loadVideosInto(episode, r, episode.keys.get("dub"), mContext);
    }
    public HttpURLConnection downloadData(final String url,final HashMap<String,String> headers) throws IOException {
        ReqBuild reqBuild = new ReqBuild(mContext, url, false);
        for (Map.Entry<String, String> aa : headers.entrySet())
            reqBuild.addHeader(aa.getKey(), aa.getValue());
        HttpURLConnection c = reqBuild.initRequest();
        c.setReadTimeout(10000000);
        return c;
    }

    @NotNull
    public ReqResponse method(@NotNull String method, @NotNull String url, @NotNull JSONObject params, @NotNull HashMap<String, String> headers) throws InvalidParameterSpecException, IOException {
        ReqBuild ans = new ReqBuild(this.mContext, url, method)
                .addPostJson(params);
        for (Map.Entry<String, String> item: headers.entrySet())
            ans.addHeader(item.getKey(), item.getValue());
        return ans.SendRequest();
    }

    @NotNull
    public YummyUser getProfile(byte mainHost){
        if(mainHost == Config.HOST_YUMMY_ANIME)
            return mYummyRestApi.getUsers().profile();
        throw new NotImplementedError("Method getProfile is not implemented for other hosts!");
    }
    @NotNull
    public String auth(byte host, @NotNull String login, @NotNull String password, @Nullable String recaptchaKey){
        if(host == Config.HOST_YUMMY_ANIME)
            return mYummyRestApi.getUsers().login(login, password, recaptchaKey)
                    .getToken();
        throw new NotImplementedError("Method auth is not implemented for other hosts!");

    }
}
