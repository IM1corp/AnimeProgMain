package com.imcorp.animeprog.Requests.JsonObj.Video;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.Default.SimpleService;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.VideoParsers.AJVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.AllVideoVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser.AllohaVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.AniBoomVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.KodikVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.MovieVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.SRVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.SibnetVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.SimpleVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.UStoreVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.VIDStreamingVideoParser;
import com.imcorp.animeprog.Requests.VideoParsers.ZombieVideoParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OneVideo implements Parcelable {
    public String voiceStudio,urlFrame,num;
    public OneVideoPlayer player=OneVideoPlayer.UNDEFINED;

    public ArrayList<OneVideoEpisodeQuality> videoQualities = new ArrayList<>();
    public ArrayList<Subtitle> videoSubtitles = new ArrayList<>();
    private boolean loaded=false;
    public boolean downloaded=false;
    public int id = 0;

    public OneVideo(String num){
        this.num = num;
    }
    public ArrayList<OneVideoEpisodeQuality> getVideoQualities(OneAnime anime, Context context, String anime_url, OneVideo video,boolean try_get_download)
            throws IOException, JSONException {
        DataBase dataBase;Request request;
        if (context instanceof SimpleService) {
            dataBase = ((SimpleService) context).dataBase;
            request = ((SimpleService) context).request;
        }
        else {
            dataBase = ((MyApp) context).dataBase;
            request = ((MyApp) context).request;
        }

        if(this.downloaded && try_get_download) {
            final File gg = dataBase.loader.getVideoURI(anime.getPath(),num);
            if(gg!=null){
                this.videoQualities.add(new OneVideoEpisodeQuality(gg.getAbsolutePath()));
                return this.videoQualities;
            }
        }

        if (urlFrame.startsWith("//")) urlFrame = "https:" + urlFrame;
        if (loaded) return this.videoQualities;
        SimpleVideoParser parser;
        switch (this.player) {
            case KODIK:
                parser = new KodikVideoParser(urlFrame, request, anime_url);
                break;
            case SIBNET:
                parser = new SibnetVideoParser(urlFrame, request, anime_url);
                break;
            case SR:
                parser = new SRVideoParser(urlFrame, request, anime_url);
                break;
            case ZOMBIE:
                parser = new ZombieVideoParser(urlFrame, request, anime_url, video);
                break;
            case ALLOHA:
                parser = new AllohaVideoParser(urlFrame, request, anime_url, video);
                break;
            case MOVIE:
                parser = new MovieVideoParser(urlFrame, request, anime_url);
                break;
            case ANIBOOM:
                parser = new AniBoomVideoParser(urlFrame, request, anime_url);
                break;
            case USTORE:
                parser = new UStoreVideoParser(urlFrame,request,anime_url);
                break;
            case ALLVIDEO:
                parser = new AllVideoVideoParser(urlFrame,request,anime_url);
                break;
            case ANIMEJOY:
                parser = new AJVideoParser(urlFrame, request, anime_url);
                break;
            case VIDSTREAMING:
                parser = new VIDStreamingVideoParser(urlFrame, request, anime_url);
                break;
            default:
                return null;
        }
        ArrayUtilsKt.sortBy(videoQualities,(element1, element2) -> element1.quality.ordinal() - element2.quality.ordinal());
        parser.loadToOneVideoEpisode(this);
        loaded = true;
        ArrayUtilsKt.sortBy(this.videoQualities,(element1, element2) ->element1.quality.ordinal()-element2.quality.ordinal());
        return this.videoQualities;
    }
    public CharSequence getPlayerStringFormatted(Resources resources){
        final String player = getPlayerString(resources);
        SpannableString string = new SpannableString(resources.getString(R.string.video_player)+' '+player) ;
        string.setSpan(new StyleSpan(Typeface.BOLD),string.length()-player.length(),string.length(),0);
        return string;
        /*SpannableStringBuilder builder = new SpannableStringBuilder(getPlayerString());
        builder.append(" (")
                .append(getEpisodeStringFormatted(resources,episodes.size()))
                .append(" )");
        return builder;*/
    }
    public static CharSequence getEpisodeStringFormatted(Resources resources,int episode){
        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString episodes_count = new SpannableString(String.valueOf(episode));
        episodes_count.setSpan(new StyleSpan(Typeface.BOLD),0,episodes_count.length(),0);

        builder.append(episodes_count)
                .append(" ")
                .append(resources.getString(R.string.episodes));
        return builder;
    }

    public String getPlayerString(Resources resources){
        switch (this.player){
            case KODIK:
                return "Codick";
            case ALLOHA:
                return "Alloha";
            case SIBNET:
                return "Sibnet";
            case ZOMBIE:
                return "Zombie";
            case SR:
                return "Sr";
            case ONVI:
                return "Onvi";
            case ANIBOOM:
                return "Aniboom";
            case MOVIE:
                return "Муви";
            case USTORE:
                return "UStore";
            case ALLVIDEO:
                return "AllVideo";
            case ANIMEJOY:
                return "AnimeJoy";
            case VIDSTREAMING:
                return "Vidstreaming";
            case GOGO_SERVER:
                return "Gogo server";
            case STREAMSB:
                return "Streamsb";
            case XSTREAMCDN:
                return "Xstreamcdn";
            case DOODSTREAM:
                return "Doodstream";
        }
        return resources.getString(R.string.undefined);
    }
    public void loadOneVideoPlayerFromUrl(){
        if(urlFrame==null)                             player = OneVideoPlayer.UNDEFINED;
        else if(urlFrame.contains("kodik")||
                urlFrame.contains("aniqit")||
                urlFrame.contains("anivod"))           player = OneVideoPlayer.KODIK;
        else if(urlFrame.contains("alloha"))           player = OneVideoPlayer.ALLOHA;
        else if(urlFrame.contains("sibnet"))           player = OneVideoPlayer.SIBNET;
        else if(urlFrame.contains("delivembed.cc")||
                urlFrame.contains("videostorage.xyz")||
                urlFrame.contains("collaps.org"))      player = OneVideoPlayer.ZOMBIE;
        else if (urlFrame.contains("onvi"))            player = OneVideoPlayer.ONVI;
        else if(urlFrame.contains("aniboom"))          player = OneVideoPlayer.ANIBOOM;
        else if(urlFrame.contains("myvi"))             player = OneVideoPlayer.MOVIE;
        else if(urlFrame.contains("sr")||
                urlFrame.contains("sovetromantica"))   player = OneVideoPlayer.SR;
        else if (urlFrame.contains("u-stream")||
                 urlFrame.contains("ustore")||
                 urlFrame.contains("u-play"))          player = OneVideoPlayer.USTORE;
        else if (urlFrame.contains("csst"))            player = OneVideoPlayer.ALLVIDEO;
        else if (urlFrame.contains("animejoy"))        player = OneVideoPlayer.ANIMEJOY;
        else if (urlFrame.contains("fembed9hd"))       player = OneVideoPlayer.XSTREAMCDN;
        else if (urlFrame.contains("streamsss"))       player = OneVideoPlayer.STREAMSB;
        else if (urlFrame.contains("dood."))           player = OneVideoPlayer.DOODSTREAM;
        else if (urlFrame.contains("gogohd.net/embed"))player = OneVideoPlayer.GOGO_SERVER;
        else if (urlFrame.contains("gogohd.net/strea"))player = OneVideoPlayer.VIDSTREAMING;
        else                                           player = OneVideoPlayer.UNDEFINED;
    }

    public String getVideoSubtitlesAsString() {
        if(videoSubtitles!=null && videoSubtitles.size()>0) {
            JSONArray ans = new JSONArray();
            for (int i = 0; i < videoSubtitles.size(); i++) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("title", videoSubtitles.get(i).title);
                    obj.put("name", videoSubtitles.get(i).getFileName());
                    ans.put(obj);
                } catch (JSONException e) { continue; }
            }
            return ans.toString();
        }
        return "";
    }
    public void subtitlesToArray(String data, File path){
        try {
            JSONArray ans_arr = new JSONArray(data);
            for(int i=0;i<ans_arr.length();i++){
                JSONObject j = ans_arr.getJSONObject(i);
                File url = new File(path,j.getString("name"));
                videoSubtitles.add(new Subtitle(url.toString(),j.getString("title")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public enum OneVideoPlayer{
        UNDEFINED,
        KODIK,
        SIBNET,
        ALLOHA,
        ZOMBIE,
        ONVI,
        SR,
        ANIBOOM,
        MOVIE,
        USTORE,
        ALLVIDEO,
        ANIMEJOY,
        XSTREAMCDN,
        DOODSTREAM,
        STREAMSB,
        GOGO_SERVER,
        VIDSTREAMING
    }
    public enum VideoType {
        V240, V360, V480, V720, V1080, AUTO, UNDEFINED, DOWNLOADED;
        public static String[] STRING_VALUES = {"240p","360p","480p","720p","1080p"};
    }

    //region parcel
    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        //dest.writeStringArray(voiceAuthors);
        dest.writeString(num);
        dest.writeInt(player!=null?player.ordinal():-1);
        dest.writeString(voiceStudio);
        dest.writeString(urlFrame);
        Config.writeBoolean(dest,loaded);
        Config.writeBoolean(dest,downloaded);
        dest.writeTypedList(videoQualities);
        dest.writeTypedList(videoSubtitles);
    }
    public static final Creator<OneVideo> CREATOR = new Creator<OneVideo>() {
        @Override
        public OneVideo createFromParcel(Parcel in) {
            OneVideo video =  new OneVideo(in.readString());
            //video.voiceAuthors = in.createStringArray();
            final int q = in.readInt();
            if(q!=-1) video.player = OneVideoPlayer.values()[q];
            video.voiceStudio = in.readString();
            video.urlFrame = in.readString();
            video.loaded = Config.readBoolean(in);
            video.downloaded = Config.readBoolean(in);
            in.readTypedList(video.videoQualities,OneVideoEpisodeQuality.CREATOR);
            in.readTypedList(video.videoSubtitles,Subtitle.CREATOR);
            return video;
        }

        @Override
        public OneVideo[] newArray(int size) {
            return new OneVideo[size];
        }
    };
    //endregion
}
