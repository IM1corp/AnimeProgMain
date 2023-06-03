package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.Default.GetProp;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.OneAnime;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SpinnerController {
    private OneAnime anime;
    ArrayList<OneVideoEP> vid;

    private int episodesSpinnerSelectIndex=0;

    private ArrayList<CharSequence> videoPlayers;
    private ArrayList<OneVideo> dubbingsList = new ArrayList<>(1);
    private ArrayList<OneVideo> videoPlayerList = new ArrayList<>(1);
    private ArrayList<HashMap.Entry<String,ArrayList<OneVideo>>> dubbingsHashMap;
    private SimpleAdapter adapterDubbing;
    private SimpleAdapter adapterVideoPlayers;

    private final Spinner dubbingsSpinner,videoPlayerSpinner,episodesSpinner;
    private final FragmentOneAnimeVideo context;
    private OneVideoEP episodeSelected;
    private final AdapterView.OnItemSelectedListener onEpisodesSelect = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            episodeSelected=anime.videos.get(i);
            loadDubbingAndPlayers(null);
        }
        public void onNothingSelected(AdapterView<?> adapterView) {}
    },
        onAuthorSelect = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dubbingSelect();
            }
            public void onNothingSelected(AdapterView<?> adapterView) { }
        },
        onVideoPlayerSelect = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                videoPlayerSelect();
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        };

    private final ReentrantLock loadingLockObject = new ReentrantLock();
    SpinnerController(final Spinner episodes_spinner, final Spinner author_spinner, final Spinner video_player_spinner,final FragmentOneAnimeVideo context){
        this.context = context;
        this.episodesSpinner = episodes_spinner;
        this.dubbingsSpinner = author_spinner;
        this.videoPlayerSpinner = video_player_spinner;
    }

    private void initData(){
        vid = anime.videos;
        ArrayList<CharSequence> episodes = new ArrayList<>(vid.size());
        for(int i=0;i<vid.size();i++){
            episodes.add(vid.get(i).getTitle(context.getResources()));
        }
        videoPlayers = new ArrayList<CharSequence>(){{add(context.getString(R.string.not_chosen));}};
        ArrayList<CharSequence> dubbings = new ArrayList<CharSequence>() {{ add(context.getString(R.string.not_chosen)); }};

        SimpleAdapter adapterEp = new SimpleAdapter(SpinnerType.EPISODE, episodes);
        adapterDubbing = new SimpleAdapter(SpinnerType.DUBBING, dubbings);
        adapterVideoPlayers = new SimpleAdapter(SpinnerType.VIDEO_PLAYER,videoPlayers);

        episodesSpinner.setAdapter(adapterEp);
        dubbingsSpinner.setAdapter(adapterDubbing);
        videoPlayerSpinner.setAdapter(adapterVideoPlayers);
    }
    public SpinnerController setSpinners(final OneAnime oneAnime){
        this.anime = oneAnime;
        initData();
        episodesSpinner.post(() -> episodesSpinner.setOnItemSelectedListener(onEpisodesSelect));
        dubbingsSpinner.post(() -> dubbingsSpinner.setOnItemSelectedListener(onAuthorSelect));
        videoPlayerSpinner.post(() -> videoPlayerSpinner.setOnItemSelectedListener( onVideoPlayerSelect));
        episodesSpinner.setSelection(episodesSpinnerSelectIndex);
        episodeSelected=anime.videos.get(episodesSpinnerSelectIndex);
        loadDubbingAndPlayers(null);
        return this;
    }
    private void dubbingSelect(){
        //fired when dubbing selected
        adapterVideoPlayers.clearEverythingExceptFirst();
        if(this.dubbingsList==null||this.dubbingsList.size()==0||dubbingsHashMap==null||dubbingsHashMap.size()==0)return;
        videoPlayerList = dubbingsHashMap.get(getSelectedDubbingIndex()).getValue();
        videoPlayers = ArrayUtilsKt.selectByFunc(videoPlayerList, new GetProp<OneVideo, CharSequence>() {
                    private final Resources resources = context.getResources();
                    @Override
                    public CharSequence getProp(OneVideo element) {
                        final String video_player = element.getPlayerString(context.getResources());
                        SpannableString s = new SpannableString(resources.getString(R.string.video_player)+" "+video_player);
                        s.setSpan(new StyleSpan(Typeface.BOLD),s.length()-video_player.length(),s.length(),0);
                        return s;
                    }
                });
        adapterVideoPlayers.addAll(videoPlayers);
        adapterVideoPlayers.notifyDataSetChanged();
        videoPlayerSpinner.setSelection(0);
    }
    private void videoPlayerSelect(){
        final int index = getSelectedVideoPlayerIndex();
        if(index==0)return;
        context.videoSelected = videoPlayerList.get(index-1);//1 element - "undefined"
        context.loadOneVideo();
    }
    private void clearVideoPlayerAndDubbingSpinner(){
        this.adapterDubbing.clear();
        this.adapterDubbing.add(context.getString(R.string.undefined));
        this.adapterVideoPlayers.clearEverythingExceptFirst();
    }
    private void onLoadData(){
        dubbingsHashMap = new ArrayList<>();
        for(final OneVideo video:dubbingsList){
            HashMap.Entry<String, ArrayList<OneVideo>> exists_video = ArrayUtilsKt.any(dubbingsHashMap,
                    objects -> objects.getKey().equalsIgnoreCase(video.voiceStudio));
            if(exists_video==null){
                dubbingsHashMap.add(exists_video = new HashMap.SimpleEntry<>(video.voiceStudio,new ArrayList<>()));
            }
            exists_video.getValue().add(video);
        }
        adapterDubbing.clear();
        adapterDubbing.addAll(ArrayUtilsKt.selectByFunc(dubbingsHashMap, new GetProp<HashMap.Entry<String, ArrayList<OneVideo>>, CharSequence>() {
                    final Resources resources = context.getResources();
                    @Override
                    public CharSequence getProp(Map.Entry<String, ArrayList<OneVideo>> element) {
                        final String dubbing = element.getKey();
                        SpannableString s = new SpannableString(resources.getString(R.string.voice_studio)+" "+dubbing);
                        s.setSpan(new StyleSpan(Typeface.BOLD),s.length()-dubbing.length(),s.length(),0);
                        return s;
                    }
                }));
        adapterDubbing.notifyDataSetChanged();
        videoPlayerList = new ArrayList<>(dubbingsHashMap.get(0).getValue().size());
        videoPlayerList.addAll(dubbingsHashMap.get(0).getValue());
        dubbingSelect();
    }
    private void loadDubbingAndPlayers(@Nullable final Runnable onSuccess){
        clearVideoPlayerAndDubbingSpinner();
        new Thread(() -> {
            if(!loadingLockObject.tryLock())return;
            try {
                dubbingsList = episodeSelected.getVideos(anime, context.context.request);
                context.context.threadCallback.post(() -> {
                    onLoadData();
                    if(onSuccess!=null)onSuccess.run();
                });
            } catch (final IOException | JSONException e) {
                context.context.threadCallback.post(() -> showErrorLoadingDubbings(e));
            }finally {
                loadingLockObject.unlock();
            }
        }).start();
    }

    private void showErrorLoadingDubbings(Exception ex){
        this.episodesSpinner.setSelection(0);
        if(ex instanceof JSONException) context.context.showInvalidJsonError();
        else if(ex instanceof InvalidHtmlFormatException) context.context.showInvalidHtmlException((InvalidHtmlFormatException)ex);
        else if(ex instanceof IOException) context.context.showNoInternetException();
    }
    int getSelectedDubbingIndex(){
        final int selected = dubbingsSpinner.getSelectedItemPosition();
        return selected!=-1 ? selected:0;
    }
    int getSelectedVideoPlayerIndex(){
        final int selected = videoPlayerSpinner.getSelectedItemPosition();
        return selected!=-1?selected:0;
    }
    int getSelectedEpisodeSpinnerIndex(){
        return this.episodesSpinner.getSelectedItemPosition();
    }

    void loadEp(final int ep,final String selectDubbing){
        final OneVideo.OneVideoPlayer player = videoPlayerList.get(Math.max(getSelectedVideoPlayerIndex() - 1,0)/*first el- "not found"*/).player;
        this.episodesSpinner.setSelection(ep);
        episodeSelected=anime.videos.get(ep);
        loadDubbingAndPlayers(new Runnable(){
            @Override
            public void run() {
                final int dubbingIndex = getDubbingIndex();
                final ArrayList<OneVideo> videos = dubbingsHashMap.get(dubbingIndex).getValue();
                dubbingsSpinner.setSelection(dubbingIndex);
                dubbingSelect();

                final OneVideo elSelected = ArrayUtilsKt.any(videos,e->e.player==player);
                videoPlayerSpinner.setSelection(elSelected!=null?videos.indexOf(elSelected)+1:1);
                videoPlayerSelect();
                //this.showAskPlayerDialog();
            }
//            private void showAskPlayerDialog() {
//                new MaterialAlertDialogBuilder(context.context)
//                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
//                        .setItems(videoPlayers.toArray(new CharSequence[0]), (dialog, which) -> {
//                            context.videoSelected = videoPlayerList.get(which);
//                            context.loadOneVideo();
//                            videoPlayerSpinner.setSelection(which);
//                        })
//                        .setOnCancelListener((dialog)->{
//                            episodesSpinner.setSelection(ep-1);
//                            loadDubbingAndPlayers(null);
//                        })
//                        .setTitle(R.string.select_video_player)
//                        .show();
//            }
            private int getDubbingIndex(){
                final ArrayList<Double> sameDubbings= ArrayUtilsKt.selectByFunc(dubbingsHashMap, element -> Config.similarity(element.getKey(),selectDubbing));
                double max=Collections.max(sameDubbings);
                if(max>0.7d)
                    return sameDubbings.indexOf(max);
                else {
                    SpinnerController.this.context.context.notificator.showDubbingsChanged(v -> context.videoPlayer.show(false));
                    return 0;
                }
            }
        });

    }

    void saveInstanceState(Bundle intent){
        intent.putInt(Config.FRAGMENT_EPISODE_SELECTED_SPINNER,getSelectedEpisodeSpinnerIndex());
        intent.putInt(Config.FRAGMENT_AUTHOR_SELECTED_SPINNER,getSelectedDubbingIndex());
        intent.putInt(Config.FRAGMENT_VIDEO_PLAYER_SELECTED_SPINNER,getSelectedVideoPlayerIndex());
    }
    void restoreInstanceState(Bundle savedInstanceState) {
        episodesSpinnerSelectIndex = savedInstanceState.getInt(Config.FRAGMENT_EPISODE_SELECTED_SPINNER);
    }
    void setVideoPlayerUndefinedSelection(){
        this.videoPlayerSpinner.setSelection(0);
    }


    private class SimpleAdapter extends ArrayAdapter<CharSequence>{
        private final SpinnerType type;
        private final int resource;
        public SimpleAdapter(@NotNull final SpinnerType type, @NonNull ArrayList<CharSequence> objects) {
            super(context.context, R.layout.video_spinner_item,android.R.id.text1,objects);
            resource=R.layout.video_spinner_item;
            this.type=type;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(Config.NEED_LOG) Log.i(Config.LOG_TAG,"getting view...");
            if(convertView==null)
                convertView = LayoutInflater.from(getContext()).inflate(this.resource,null);
            final boolean downloaded,visible;
            switch (type){
                case EPISODE:
                    final OneVideoEP item = vid.get(position);
                    downloaded = item.isDownloaded();
                    visible = anime.getProgress(context.context.dataBase).existsNum(item.num);
                    break;
                case DUBBING:
                    downloaded = dubbingsHashMap!=null&&
                               !dubbingsHashMap.isEmpty()&&
                        ArrayUtilsKt.any(dubbingsHashMap.get(position).getValue(), objects -> objects.downloaded)!=null;
                    visible=false;
                    break;
                case VIDEO_PLAYER:
                    downloaded=position!=0&&
                               videoPlayerList!=null&&
                               !videoPlayerList.isEmpty() &&
                                videoPlayerList.get(position-1).downloaded;
                    visible=false;
                    break;
                default:downloaded=visible=false;
            }
            convertView.findViewById(R.id.downloadedImageView).setVisibility(downloaded?View.VISIBLE:View.INVISIBLE);
            convertView.findViewById(R.id.eyeImageView).setVisibility(visible?View.VISIBLE:View.INVISIBLE);
            ((TextView)convertView.findViewById(R.id.textView)).setText(this.getItem(position));
            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position,convertView,parent);
        }

        //keep first element - it is "undefined"
        void clearEverythingExceptFirst() {
            while (this.getCount()>1)
                this.remove(this.getItem(this.getCount()-1));

        }
    }
    private enum SpinnerType{EPISODE,DUBBING,VIDEO_PLAYER}
}
