package com.imcorp.animeprog;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;

import com.imcorp.animeprog.Default.MyApp;
import com.imcorp.animeprog.Default.MyPopupMenu;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    public static String getUrlByHost(int host){
        switch(host){
            case HOST_ANIMEGO_ORG: return ANIMEGO_URL;
            case HOST_YUMMY_ANIME: return YUMMY_ANIME_URL;
            case HOST_ANIME_JOY: return ANIMEJOY_URL;
            case HOST_GOGO_ANIME: return GOGOANIME_URL;
        }
        return "";
    }
    public static byte getHostByUrl(String url){
        if (url!=null) {
            if (url.startsWith(ANIMEGO_URL)) return HOST_ANIMEGO_ORG;
            if (url.startsWith(YUMMY_ANIME_URL) || url.startsWith(YUMMY_ANIME_URL_2))
                return HOST_YUMMY_ANIME;
            if (url.startsWith(ANIMEJOY_URL)) return HOST_ANIME_JOY;
        }
        return 0;
    }

    public static final String YUMMY_ANIME_HOST = BuildConfig.DEBUG ? "yummyani.me": "yummy-anime.ru";
    public static final String YUMMY_ANIME_HOST_2 = "yummyani.me";
    public static final String YUMMY_ANIME_URL = BuildConfig.DEBUG?"http://"+YUMMY_ANIME_HOST :"https://"+YUMMY_ANIME_HOST;
    public static final String YUMMY_ANIME_URL_2 = "https://"+YUMMY_ANIME_HOST_2;
    public static final String REST_API_YUMMYANIME = BuildConfig.DEBUG ? "http://yummyani.me/api":"https://api.yani.tv";
//    public static final String REST_API_YUMMYANIME = "https://api.yani.tv";

    public static final String user_agent = "Mozilla/5.0 (Windows NT 6.1; rv:52.0) Gecko/20100101 Firefox/52.0";
    public static final String search_hint_path = "/search";
    public static final String one_anime_path = "/catalog/item";
    public static final String coding = "utf-8";
    public static final String vpn_string = "market://search?q=vpn%20free";
    public static final String URL_GET_MORE_EP_AG = "https://animego.org/anime/series";

    public static String API_key="0a6e6815-b7bb-4cee-b6b1-41935a7ec1cc";

    public static final String ANIMEGO_HOST = "animego.org";
    public static final String ANIMEGO_URL = "https://"+ANIMEGO_HOST;
    public static final String ANIMEGO_SEARCH_PATH = "/search/";

    public static final String ANIMEJOY_HOST = "animejoy.ru";
    public static final String ANIMEJOY_URL = "https://"+ANIMEJOY_HOST;

    public static final String GOGOANIME_HOST = "www1.gogoanime.day";
    public static final String GOGOANIME_URL = "https://"+GOGOANIME_HOST;


    public static final String LOG_HTML_PARSE = "HtmlParseE";
    public static final String LOG_TAG = "MainLog";
    public static final String REQUEST_LOG_TAG = "RequestsLog";
    public static final String ALLOHA_VIDEO_PARSER_LOG = "AllohaLog";
    public static final String DOWNLOAD_SERVICE_TAG = "DownloadService";
    public static final String M3U8_DOWNLOAD_FNAME = "index.m3u8";


    //region INT_PARAMS
    public static final int VIDEO_CONTROLS_WAIT_TIME=3300;//ms
    public static final int VIDEO_CONTROLS_DOUBLE_CLICK_MAX_TIME=250;//ms
    public static final double VIDEO_CONTROLS_DOUBLE_CLICK_NEXT_EP_AREA=0.25d;//proportion
    public static final int VIDEO_CONTROLS_DOUBLE_CLICK_WAIT_TIME=1100;//ms
    public static final int VIDEO_CONTROLS_SEEK_TIME=10000;//ms
    public static final long VIDEO_CONTROLS_ROTATION_ANIMATION_DURATION=500;//ms
    public static final int START_ONE_ANIME_ACTIVITY_REQUEST_CODE=1;
    public static final int FAVORITES_ITEMS_LOAD_COUNT=50;
    public static final int DOWNLOADS_LOAD_COUNT=0xffff;
    public static final int DOWNLOADS_SLEEP_TIME_MS = 200;
    public static final int DOWNLOADS_NOTIF_UPDATE_PROGRESS_TIME_MS = 1400;
    public static final int DOWNLOADS_VIEW_UPDATE_PROGRESS_TIME_MS = 500;

    public static final long SEARCH_WAIT_TIME_MS = 1300;
    public static final byte HOST_YUMMY_ANIME=0;
    public static final byte HOST_ANIMEGO_ORG=1;
    public static final byte HOST_ANIME_JOY=2;
    public static final byte HOST_GOGO_ANIME=3;
    public static final Map.Entry<Byte,String>[] HOSTS = new Map.Entry[]{
            new AbstractMap.SimpleEntry<>(Config.HOST_YUMMY_ANIME, "Yummy-Anime.ru"),
            new AbstractMap.SimpleEntry<>(Config.HOST_ANIMEGO_ORG, "AnimeGo.org"),
            new AbstractMap.SimpleEntry<>(Config.HOST_ANIME_JOY, "AnimeJoy.ru"),
            new AbstractMap.SimpleEntry<>(Config.HOST_GOGO_ANIME, "Gogoanime.day"),
    };

    public final static int MAX_SPEED_PROGRESS_BAR_VALUE = 1000;
    public final static int DEFAULT_SPEED_PROGRESS_BAR_VALUE = MAX_SPEED_PROGRESS_BAR_VALUE/2;

    public final static int DOWNLOAD_NOTIFICATION_ID = Integer.MIN_VALUE;
    public final static int VIDEO_PLAYER_NOTIFICATION_ID = 4;
    public static final int SEARCH_ITEMS_LOAD_COUNT = 10;
    public static final String DATABASE_ER_LOG = "DBError:";
    public static final String SEARCH_Q="q";
    public static final String SEARCH_C="c";
    public static final String LAST_SEARCH="last_search";
    public static final String VIDEO_PLAYER_LOG = "VideoPlayerE";
    public static final String MEDIA_SESSION_TAG = "VideoMediaSession";
    public static final boolean NEED_LOG = BuildConfig.DEBUG;

    //endregion
    //region useful methods
    @DrawableRes public static int getIconByHost(byte searchHost) {
        switch (searchHost) {
            case Config.HOST_ANIMEGO_ORG :
                return R.mipmap.animego_icon;
            case Config.HOST_YUMMY_ANIME:
                return R.drawable.ic_yummy_fav;
            case Config.HOST_ANIME_JOY:
                return R.mipmap.animejoy_icon;
            case Config.HOST_GOGO_ANIME:
                return R.mipmap.gogo_anime_icon;
        }
        return 0;
    }
    public static int getHostFromId(@IdRes int id){
        if(id==R.id.animego_menu_item)return HOST_ANIMEGO_ORG;
        if(id==R.id.yummyanime_menu_item)return HOST_YUMMY_ANIME;
        if(id==R.id.animejoy_menu_item)return HOST_ANIME_JOY;
        return 0;
    }

    public static TextView getLinkTextView(final Context context, final CharSequence text, final View.OnClickListener onClick){
        final TextView textView = new TextView(context);
        setTextViewLink(textView,text,onClick);
        return textView;
    }
    public static void setTextViewLink(final TextView textView,final CharSequence text, final View.OnClickListener onClick){
        final TypedValue typedValue = new TypedValue();
        final int offsetClickableItem = Config.dpToPix(textView.getContext(), 2);
        textView.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        textView.setTextColor(textView.getContext().getResources().getColor(R.color.link_color));
        textView.setBackgroundResource(typedValue.resourceId);
        textView.setOnClickListener(onClick);
        textView.setText(text);
        textView.setPadding(offsetClickableItem, 0, offsetClickableItem, 0);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
    }
    public static void copyOnLongPress(MyApp activity, TextView textView){
        copyOnLongPress(activity,textView,null);
    }
    public static void copyOnLongPress(@NotNull MyApp activity,@NotNull TextView textView, String whatToCopy){
        textView.setOnLongClickListener((v)->{
            MyPopupMenu menu = new MyPopupMenu(activity,textView);
            activity.getMenuInflater().inflate(R.menu.copy_context_menu,menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                activity.copyText(whatToCopy==null?textView.getText().toString():whatToCopy);
                return true;
            });
            menu.show();
            return true;
        });
    }
    public static String getPosString(int pos){
        pos/=1000;
        final int hours = pos/3600,
                secs = pos%60,
                minutes = (pos % 3600) / 60;
        String ans = String.format(Locale.getDefault(),"%02d:%02d",minutes,secs);
        return hours==0?ans:hours+":"+ans;
    }
    public static String md5(final String url){
        try {
            final byte[] data= MessageDigest.getInstance("MD5").digest(url.getBytes(Config.coding));
            final StringBuilder builder = new StringBuilder();
            for (byte datum : data) {
                builder.append(Integer.toHexString((datum & 0xFF) | 0x100).substring(1, 3));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ignore) {}
        return "error";

    }
    public static CharSequence getText(Context context, int id, Object... args) {
        for(int i = 0; i < args.length; ++i)
            args[i] = args[i] instanceof String? TextUtils.htmlEncode((String)args[i]) : args[i];
        return Html.fromHtml(String.format(Html.toHtml(new SpannedString(context.getText(id))), args));
    }
    public static void writeBoolean(Parcel parcel,boolean bool){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) parcel.writeBoolean(bool);
        else parcel.writeByte((byte)(bool?1:0));
    }
    public static boolean readBoolean(Parcel parcel){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return parcel.readBoolean();
        return parcel.readByte()==1;
    }
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
    public static int loadIntegerFromText(String text,int min_length){
        Matcher m = FIND_NUM.matcher(text);
        while (m.find()){
            if(m.group().length()<min_length)continue;
            return Integer.parseInt(m.group());
        }
        return 0;
    }
    public static String getSizeFromByte(final double bytes){
        if(bytes < 1<<20 ){ //если еще не скачан 1 мбайт
            return String.format(Locale.getDefault(),"%d KB",Math.round(bytes/1024f));
        } else {
            return String.format(Locale.getDefault(),"%.2f MB",bytes/1048576);
        }
    }
    public static int dpToPix(final Context context, final int dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }
    public final static Pattern FIND_NUM = Pattern.compile("\\d+");
    //endregion
    //region SAVE_INSTANCE_STATE

    public static final String FRAGMENT_ANIME_VIDEO="f_anime_video";
    public static final String FRAGMENT_ANIME_DATA="f_anime_data";
    public static final String FRAGMENT_ANIME_OBJ="anime";
    public static final String FRAGMENT_AUTHOR_SELECTED_SPINNER="author_sp";
    public static final String FRAGMENT_VIDEO_PLAYER_SELECTED_SPINNER="video_pl_sp";
    public static final String FRAGMENT_EPISODE_SELECTED_SPINNER="video_ep_sp";
    public static final String FRAGMENT_CONTROLS_POS="pos";
    public static final String FRAGMENT_HISTORY_DATA ="anime_array";
    public static final String FRAGMENT_HISTORY ="main_history_fragment";
    public static final String FRAGMENT_DOWNLOADS = "downloads_fragment";
    public static final String FRAGMENT_FAVORITES = "favorites_fragment";
    public static final String FRAGMENT_HOME = "home_fragment";
    public static final String FRAGMENT_FAVORITES_ITEMS = "items";
    public static final String FAV_TYPE_KEY="fav_type";
    public static final String PATH = "path";
    public static final String URL_TYPE="url_type";

    public static final String DATA_PATH = "data";
    public static final String DATA_MAIN_PAGE="main_page";
    public static final String HOST_KEY="HOST_KEY";
    public static final String DATA_ERROR="error";
    public static final String IS_REFRESH="refresh";
    public static final String GO_TO_FRAGMENT="go_to";
    public static final String GO_TO_FRAGMENT_ID="go_id";

    public static final String COVER_DATA_PATH = "cover.jpg";
    public static final String EPISODES_MAP = "ep_map";
    public static final String DOWNLOADS_CHANNEL_ID = "down_channel_id";
    public static final String VIDEO_PLAYER_CHANNEL_ID = "video_player_id";
    public static final String DOWNLOADS_SERVICE_NAME = "downloadService";
    public final static String PAUSE_BUTTON_CLICKED = "action.pause";
    public final static String IS_PAUSE_EVENT = "pause";
    public static final String BACK_BUTTON_CLICKED = "action.back";
    public static final String NEXT_BUTTON_CLICKED = "action.next";
    public final static String CANCEL_BUTTON_CLICKED = "action.cancel";
    public final static String DESTROY_SERVICE = "action.destroy";
    public final static String DOWNLOADS_FRAGMENT_ID = "ids";
    public static final String PROFILE_KEY ="pofile_key";

    //endregion
    //TODO: error on link https://yummy-anime.ru/catalog/item/kulak-severnoj-zvezdy-legenda-o-yurii
    //TODO: error on link https://yummy-anime.ru/catalog/item/gospozha-kaguya-v-lyubvi-kak-na-vojne

    public final static String CaptchaPublicKey = "b1847961-208e-4a90-9671-1e6bba9e0b36";

}

// TODO: check for updates and show update dialog (if new version)