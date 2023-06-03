package com.imcorp.animeprog.Requests.JsonObj;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DB.DataBase;
import com.imcorp.animeprog.DB.Objects.ProgressRow;
import com.imcorp.animeprog.DB.local.Favorites;
import com.imcorp.animeprog.Default.LoadImageEvents;
import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.Request;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;
import com.imcorp.animeprog.Requests.JsonObj.comments.AnimeComments;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneAnime implements Parcelable {
    private final static Pattern ANIMEGO_COVER_REPLACE = Pattern.compile("thumbs_\\d+x\\d+");
    public String title,description,animeType,status;
    private String cover,big_cover,path;
    public long watches;
    public int year;
    public byte HOST;
    private AnimeAttrs attrs;
    private ProgressRow progress=null;
    private DataPosterText dataPosterText;

    public ArrayList<OneAnime> viewingOrder =new ArrayList<>();
    public ArrayList<OneVideoEP> videos=new ArrayList<>();
    public VideoResponseType responseType = VideoResponseType.SUCCESS;

    private boolean fullyLoaded=false;
    private Thread loadingThread;
    private final LinkedList<LoadImageEvents> loadingImageSet = new LinkedList<>();
    public AnimeComments comments = new AnimeComments();
    private int animeId;

    public OneAnime(byte HOST){
        this.HOST=HOST;
    }
    public void removeLoadImageCallbacks(){
        loadingImageSet.clear();
    }
    public void loadCover(@Nullable final Context context, final Request request, final LoadImageEvents events){
        String cover_cache = null;
        final DataBase dataBase = (context instanceof MyApp)?((MyApp)context).dataBase:((DownloadService)context).dataBase;
        if(dataBase!=null) {
            cover_cache = dataBase.cache.tryGetImgFromCache(cover);
            if(cover_cache==null)cover_cache = dataBase.loader.loadCover(this);
        }
        Bitmap bitmapDataObject = null;
        if(cover_cache != null && new File(cover_cache).exists()) {
            bitmapDataObject = BitmapFactory.decodeFile(cover_cache);
            if (bitmapDataObject != null)
                events.onSuccess(bitmapDataObject);
        }
        if(bitmapDataObject == null)
            synchronized (OneAnime.this) {
            if(!loadingImageSet.contains(events))
                loadingImageSet.add(events);
            if (this.loadingThread == null) {
                if(Config.NEED_LOG) Log.i(Config.REQUEST_LOG_TAG,"Downloading image from "+cover );
                this.loadingThread = new Thread(() -> {
                    Runnable r;
                    try {
                        Bitmap bitmap = request.loadImageFromUrl(cover);
                        if (dataBase != null) dataBase.cache.saveImgToCache(cover, bitmap);
                        if(bitmap == null) throw new NullPointerException("Image is null");
                        r = () -> {
                            synchronized (OneAnime.this) {
                                for (LoadImageEvents event : loadingImageSet)
                                    event.onSuccess(bitmap);
                                loadingImageSet.clear();
                            }
                        };
                    } catch (IOException|NullPointerException e) {
                        r = () -> {
                            synchronized (OneAnime.this) {
                                for (Iterator<LoadImageEvents> iterator = loadingImageSet.iterator(); iterator.hasNext(); ) {
                                    final LoadImageEvents event = iterator.next();
                                    if (event.onFail(e)) iterator.remove();
                                    event.onFail(e);
                                }
                            }
                        };
                    } finally {
                        synchronized (OneAnime.this) {
                            loadingThread = null;
                        }
                    }
                    if (context instanceof MyApp) ((MyApp)context).threadCallback.post(r);
                    else r.run();
                });
                loadingThread.start();
            }
        }
    }
    //region parceable
    @Override public int describeContents() {
        return 0;
    }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(HOST);

        dest.writeStringArray(new String[]{title,description,path,cover, animeType,status,big_cover});
        dest.writeLong(watches);
        dest.writeInt(year);
        dest.writeList(viewingOrder);
        dest.writeTypedList(videos);

        Config.writeBoolean(dest,progress!=null);
        if(progress!=null)dest.writeParcelable(progress,0);
        Config.writeBoolean(dest,isFullyLoaded());
        dest.writeParcelable(attrs,0);
        dest.writeParcelable(dataPosterText,0);
        dest.writeInt(responseType!=null?responseType.ordinal():-1);
        dest.writeParcelable(comments, 0);
    }
    public static final Parcelable.Creator<OneAnime> CREATOR = new Parcelable.Creator<OneAnime>() {
        @Override public OneAnime createFromParcel(Parcel in) {
            OneAnime anime = new OneAnime(in.readByte());
            String[] data=new String[7];
            in.readStringArray(data);
            anime.title = data[0];
            anime.description = data[1];
            anime.path = data[2];
            anime.cover = data[3];
            anime.animeType = data[4];
            anime.status = data[5];
            anime.big_cover = data[6];

            anime.watches = in.readLong();
            anime.year = in.readInt();
            in.readList(anime.viewingOrder,OneAnime.class.getClassLoader());
            anime.videos = in.createTypedArrayList(OneVideoEP.CREATOR);
            //anime.bitmapDataObject = Bitmap.CREATOR.createFromParcel(in);
            if(Config.readBoolean(in))
                anime.progress = in.readParcelable(ProgressRow.class.getClassLoader());
            anime.setFullyLoaded(Config.readBoolean(in));
            anime.attrs = in.readParcelable(AnimeAttrs.class.getClassLoader());
            anime.dataPosterText = in.readParcelable(DataPosterText.class.getClassLoader());
            final int o = in.readInt();
            if(o!=-1) anime.responseType = VideoResponseType.values()[o];

            anime.comments = in.readParcelable(AnimeComments.class.getClassLoader());
            return anime;

        }

        @Override
        public OneAnime[] newArray(int size) {
            return new OneAnime[size];
        }
    };
    //endregion
    public String getPath() {
        return this.path!=null?this.path:"";
    }
    public void setCover(String value){
        if(value!=null&&!value.isEmpty()) {
            if (value.startsWith("/")) value = Config.getUrlByHost(HOST)+value;
            if (HOST == Config.HOST_ANIMEGO_ORG) {
                if (!value.contains("thumbs_250x350")) {
                    Matcher m = ANIMEGO_COVER_REPLACE.matcher(value);
                    if (m.find())
                        value = value.replace(Objects.requireNonNull(m.group(0)), "thumbs_250x350");
                }
            }
        }
        cover = value;
    }
    public String getCover(){
        return cover;
    }
    public String getBigCoverString(){
        return big_cover==null?null:
                !big_cover.startsWith("/")?big_cover:getHostString()+big_cover;
    }
    public Bitmap getBigCover(MyApp context) throws IOException, JSONException {
        return context.request.loadBigImage(this);
    }
    public void setBigCover(String value){
        if(value.startsWith("/"))value = getHostString()+value;
        big_cover=value;
    }
    public String getHostString(){
        switch (HOST){
            case Config.HOST_ANIMEGO_ORG:
                return  "https://"+Config.ANIMEGO_HOST;
            case Config.HOST_YUMMY_ANIME:
                return "https://"+Config.YUMMY_ANIME_HOST;
        }
        return "";
    }
    public String getAnimeURI() {
        switch (this.HOST){
            case Config.HOST_ANIMEGO_ORG:
                return Config.ANIMEGO_URL+path;
            case Config.HOST_YUMMY_ANIME:
                return Config.YUMMY_ANIME_URL+path;
        }
        return "";
    }
    @NotNull public ProgressRow getProgress(@NotNull DataBase dataBase){
        if(progress==null){
            this.progress = dataBase.progress.getProgress(getPath());
        }
        return progress;
    }
    public void setProgress(@Nullable ProgressRow progress){
        this.progress = progress;
    }
    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
    public void setFullyLoaded(boolean fullyLoaded){this.fullyLoaded = fullyLoaded;}
    public AnimeAttrs getAttrs(){
        if(attrs==null)attrs=new AnimeAttrs();
        return attrs;
    }
    public boolean posterExists(){
        return dataPosterText!=null;
    }
    public DataPosterText getDataPosterText(){
        if(dataPosterText==null)dataPosterText=new DataPosterText();
        return dataPosterText;
    }
    public int getErrorFromResponseType() {
        switch (responseType){
            case VIDEO_DELETED:
                return R.string.video_deleted_error;
            case VIDEO_NOT_ABLE_IN_YOUR_COUNTRY:
                return R.string.video_not_enabled_error;
            default:
                return R.string.undefined_error;
        }
    }
    @StringRes public int getActionButtonFromResponseType() {
        switch (responseType){
            case VIDEO_DELETED:
                return R.string.retry;
            case VIDEO_NOT_ABLE_IN_YOUR_COUNTRY:
                return R.string.go_link;
            default:
                return R.string.undefined_error;
        }
    }

    public void setPath(String href) {
        if(href.startsWith("https://") || href.startsWith("http://")){
            for(int i=0,c=0;i<href.length();i++){
                if(href.charAt(i)=='/'||href.charAt(i)=='\\')c++;
                if(c == 3){
                    href = href.substring(i);
                    break;
                }
            }
        }
        if(!href.startsWith("/")) href='/'+href;
        path = href;
    }

    public int getAnimeId() {
        return animeId;
    }

    public void setAnimeId(int animeId) {
        this.animeId = animeId;
    }

    public enum VideoResponseType {
        SUCCESS,
        VIDEO_DELETED,
        VIDEO_NOT_ABLE_IN_YOUR_COUNTRY,
        UNDEFINED
    }
    public static class OneAnimeWithId implements Parcelable {
        public int id, epCount, epDownloaded;
        public OneAnime anime;
        public Favorites.FavoritesType type=null;
        public OneAnimeWithId(final int id,final OneAnime anime,final int seriasDownloaded,final int seriasCount){
            this.id=id;
            this.anime=anime;
            this.epCount=seriasCount;
            this.epDownloaded =seriasDownloaded;
        }
        public OneAnimeWithId(final int id,final OneAnime anime){
            this.id=id;
            this.anime=anime;
            this.epCount=0;
            this.epDownloaded=0;
        }
        public static final Creator<OneAnimeWithId> CREATOR = new Creator<OneAnimeWithId>() {
            @Override public OneAnimeWithId createFromParcel(Parcel in) {
                return new OneAnimeWithId(in.readInt(), in.readParcelable(OneAnime.class.getClassLoader()),in.readInt(),in.readInt());
            }
            @Override public OneAnimeWithId[] newArray(int size) {
                return new OneAnimeWithId[size];
            }
        };
        @Override public int describeContents() {
            return 0;
        }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeParcelable(anime,0);
            dest.writeInt(epDownloaded);
            dest.writeInt(epCount);
        }
    }
    public static OneAnime.OneAnimeWithId fromSearch(byte host, int year, String url, String name, String img_url, String description){
        OneAnime anime = new OneAnime(host);
        anime.year=year;
        anime.setPath(url);
        anime.title = name;
        anime.setCover(img_url);
        anime.description = description;
        return new OneAnimeWithId(0,anime);
    }
    public static class Link implements Parcelable{
        public String title,link;
        public Link(String title,String link){
            this.title=title;
            this.link=link;
        }
        public Link(@NotNull Element el){
            this.title = el.text();
            this.link = el.attr("href");
        }
        private Link(Parcel p){
            this.title = p.readString();
            this.link = p.readString();
        }
        public static final Creator<Link> CREATOR = new Creator<Link>() {
            @Override public Link createFromParcel(Parcel in) {
                return new Link(in);
            }
            @Override public Link[] newArray(int size) {
                return new Link[size];
            }
        };
        @Override public int describeContents() { return 0; }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(link);
        }

        public byte[] toBlob() {
            Parcel parcel=null;
            try {
                parcel = Parcel.obtain();
                this.writeToParcel(parcel, 0);
                return parcel.marshall();
            }catch (Exception e){
                if(Config.NEED_LOG) Log.e(Config.LOG_TAG,e.getMessage(),e);
                return null;
            }finally {
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        }
        public static Link fromBlob(byte[] data){
            Parcel parcel = null;
            try {
                parcel = Parcel.obtain();
                parcel.unmarshall(data, 0, data.length);
                return new Link(parcel);
            }catch (Exception e){
                return new Link("","");
            }finally {
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        }
        public static byte[] arrayToBlob(List<Link> list){
            Parcel parcel=null;
            try {
                parcel = Parcel.obtain();
                parcel.writeList(list);
                return parcel.marshall();
            }catch (Exception e){
                if(Config.NEED_LOG) Log.e(Config.LOG_TAG,e.getMessage(),e);
                return null;
            }finally {
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        }
        @SuppressWarnings("unchecked") public static List<Link> arrayFromBlob(byte[] dataBlob){
            if(dataBlob==null||dataBlob.length==0)return null;
            Parcel parcel=null;
            try {
                parcel = Parcel.obtain();
                parcel.unmarshall(dataBlob,0,dataBlob.length);
                return parcel.readArrayList(Link.class.getClassLoader());
            }catch (Exception e){
                if(Config.NEED_LOG) Log.e(Config.LOG_TAG,e.getMessage(),e);
                return null;
            }finally {
                if (parcel != null) {
                    parcel.recycle();
                }
            }
        }
        public static Link noHref(String title){return new Link(title,"");}
    }
    public static class AnimeAttrs implements Parcelable{
        private ArrayList<Link> studios = new ArrayList<>(), producers=new ArrayList<>();
        public String issueDate, rating, originalSource,serialType;
        public ArrayList<OneAnime.Link> genres;
        public ArrayList<String> synonyms;
        public int epCount=0;

        public ArrayList<Link> getStudios(){
            return this.studios;
        }
        public ArrayList<OneAnime.Link> getProducers(){return this.producers;}
        public void setStudios(ArrayList<Link> val){
            this.studios = val;
        }
        public void setProducers(ArrayList<Link> val){
            this.producers = val;
        }

        public OneAnime.Link getProducer(){
            return this.producers.size() > 0 ? this.producers.get(0) : null;
        }
        public OneAnime.Link getStudio(){
            return this.studios.size() > 0 ? this.studios.get(0) : null;
        }

        public AnimeAttrs(){}
        public SpannableString getIssueDateFormatted(){
            final SpannableString s = new SpannableString(issueDate);
            for(int i=0,start=-1;i<s.length();i++){
                if(Character.isDigit(issueDate.charAt(i))) {
                    if(start==-1)start = i;
                    else if(i==s.length()-1)
                        s.setSpan(new StyleSpan(Typeface.BOLD), start, i+1, 0);
                }else if(start!=-1) {
                    s.setSpan(new StyleSpan(Typeface.BOLD), start, i, 0);
                    start=-1;
                }
            }
            return s;
        }
        public static final Creator<AnimeAttrs> CREATOR = new Creator<AnimeAttrs>() {
            @Override public AnimeAttrs createFromParcel(Parcel in) {
                return new AnimeAttrs(in);
            }
            @Override public AnimeAttrs[] newArray(int size) {
                return new AnimeAttrs[size];
            }
        };
        @Override public int describeContents() {
            return 0;
        }
        @Override public void writeToParcel(Parcel dest, int flags) {

            dest.writeList(studios);
            dest.writeList(producers);
            dest.writeString(rating);
            dest.writeString(issueDate);
            dest.writeString(originalSource);
            dest.writeString(serialType);
            dest.writeStringList(synonyms);
            dest.writeList(genres);
            dest.writeInt(epCount);
        }
        @SuppressWarnings("unchecked") private AnimeAttrs(Parcel in) {
            studios = in.readArrayList(Link.class.getClassLoader());
            producers = in.readArrayList(Link.class.getClassLoader());
            rating = in.readString();
            issueDate = in.readString();
            originalSource = in.readString();
            serialType = in.readString();
            synonyms = in.createStringArrayList();
            genres = in.readArrayList(Link.class.getClassLoader());
            epCount = in.readInt();
        }
    }
    public static class DataPosterText implements Parcelable {
        public String mainTitle,subTitle;
        public DataPosterText(String mainTitle,String subTitle){this.mainTitle=mainTitle;this.subTitle=subTitle;}
        public DataPosterText(){}
        public static final Creator<DataPosterText> CREATOR = new Creator<DataPosterText>() {
            @Override
            public DataPosterText createFromParcel(Parcel in) {
                return new DataPosterText(in.readString(),in.readString());
            }

            @Override
            public DataPosterText[] newArray(int size) {
                return new DataPosterText[size];
            }
        };

        @Override public int describeContents() {
            return 0;
        }

        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mainTitle);
            dest.writeString(subTitle);
        }
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof OneAnime){
            return HOST == ((OneAnime) obj).HOST &&
                    getPath().equals(
                            ((OneAnime) obj).getPath()
                    );
        }
        return super.equals(obj);
    }
}
