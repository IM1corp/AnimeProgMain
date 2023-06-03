package com.imcorp.animeprog.DownloadManager;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import org.json.JSONException;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {
    private final DownloadManager mManager;
    private String defaultDubbingSelection=null;
    private OneVideo.OneVideoPlayer defaultVideoPlayer = OneVideo.OneVideoPlayer.UNDEFINED;
    private OneVideo.VideoType defaultVideoType = OneVideo.VideoType.AUTO;
    RecycleViewAdapter(DownloadManager manager){
        this.mManager = manager;
    }
    @NonNull public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_download_item, parent, false);
        return new MyViewHolder(view);
    }
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(position);
    }
    public int getItemCount() {
        return mManager.anime.videos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        private TextView episodeNum;
        private Spinner spinnerDubbing,spinnerPlayer,spinnerQuality;
        private ArrayAdapter<CharSequence> adapterDubbing,adapterPlayer, adapterQuality;
        private CheckBox checkBox;
        private Thread thread;
        private int index;
        MyViewHolder(View view) {
            super(view);
            this.view=view;
            this.episodeNum= view.findViewById(R.id.episodeNum);
            this.spinnerDubbing = view.findViewById(R.id.spinnerDubbing);
            this.spinnerQuality = view.findViewById(R.id.spinnerQuality);
            this.spinnerPlayer = view.findViewById(R.id.spinnerPlayer);
            this.checkBox = view.findViewById(R.id.checkBox);
        }
        void bind(final int index){
            this.index=index;
            if(getVideoEP().isDownloaded()) this.setEpisodeDownloaded();
            this.episodeNum.setText(getVideoEP().num);
            this.checkBox.post(() -> checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                final int VISIBLE = isChecked?View.VISIBLE:View.GONE;
                spinnerQuality.setVisibility(VISIBLE);spinnerPlayer.setVisibility(VISIBLE);spinnerDubbing.setVisibility(VISIBLE);
                EpisodeSelected ep = map.get(index);
                if(isChecked) {
                    if (ep == null) map.put(index, new EpisodeSelected());
                    loadVideos();
                } else if (ep!=null) {
                    map.remove(index);
                    for (ArrayAdapter<CharSequence> sp : new ArrayAdapter[]{adapterDubbing, adapterPlayer, adapterQuality}) {
                        if(sp==null)continue;
                        sp.clear();
                        sp.addAll(mManager.activity.getResources().getStringArray(R.array.not_defined));
                        sp.notifyDataSetChanged();
                    }
                }
                mManager.okButton.setEnabled(map.size()!=0);
            }));

        }
        private void setEpisodeDownloaded() {
            this.view.setBackgroundColor(mManager.activity.getResources().getColor(R.color.downloaded_item_color));
        }
        private void loadVideos(){
            if(thread!=null)return;
            thread = new Thread(() -> {
                try {
                    final ArrayList<OneVideo> videos = getVideoEP().getVideos(mManager.anime,mManager.activity.request);
                    mManager.activity.threadCallback.post(() -> updateSpinners(videos));
                }
                catch (IOException e) {
                    mManager.activity.showInvalidJsonError();
                }
                catch (JSONException e) {
                    mManager.activity.showInvalidJsonError();
                }
                thread=null;
            });
            thread.start();
        }
        private void updateSpinners(final ArrayList<OneVideo> videos){
            if(adapterDubbing==null){
                spinnerPlayer.setAdapter(adapterPlayer=new ArrayAdapter<>(mManager.activity,android.R.layout.simple_spinner_item,new ArrayList<CharSequence>()));
                spinnerQuality.setAdapter(adapterQuality =new ArrayAdapter<>(mManager.activity,android.R.layout.simple_spinner_item,new ArrayList<CharSequence>()));
                spinnerDubbing.setAdapter(adapterDubbing =new ArrayAdapter<>(mManager.activity,android.R.layout.simple_spinner_item,new ArrayList<CharSequence>()));
                this.spinnerDubbing.post(() -> spinnerDubbing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        defaultDubbingSelection = spinnerDubbing.getSelectedItem().toString();
                        spinnerPlayer.setSelection(0);
                        updatePlayers();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }));
                this.spinnerPlayer.post(() -> spinnerPlayer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    videoPayerSelect(position);
                                }catch (InvalidHtmlFormatException e){
                                    mManager.activity.showInvalidHtmlException(e);
                                }catch (IOException e){
                                    mManager.activity.showNoInternetException();
                                }catch (JSONException e){
                                    mManager.activity.showInvalidJsonError();
                                }
                            }
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        }));
                this.spinnerQuality.post(() -> spinnerQuality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                             @Override
                             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                 try {
                                     videoQualitySelect();
                                 } catch (IOException|JSONException e) {
                                     mManager.activity.showUndefinedError();
                                 }
                             }
                             public void onNothingSelected(AdapterView<?> parent) {

                             }
                         }));
            }
            this.adapterDubbing.clear();this.adapterPlayer.clear();this.adapterQuality.clear();
            this.updateDubbings(videos);
        }
        private void videoQualitySelect() throws IOException, JSONException {
            final ArrayList<OneVideo> videos = getVideoEP().getVideos(mManager.anime, mManager.activity.request);
            final int index_video = this.getVideoSelectedIndex(videos);
            final int dubbing_index = spinnerQuality.getSelectedItemPosition();
            if(dubbing_index>=0&&videos.get(index_video).videoQualities.size()>dubbing_index)
            map.get(this.index).quality=videos.get(index_video).videoQualities.get(dubbing_index).quality;

        }
        private void updateDubbings(final ArrayList<OneVideo> videos){
            final ArrayList<String> dubbings_container=new ArrayList<>();
            for(OneVideo video:videos){
                if(dubbings_container.contains(video.voiceStudio.toLowerCase()))continue;
                dubbings_container.add(video.voiceStudio.toLowerCase());
            }
            adapterDubbing.addAll(dubbings_container);
            adapterDubbing.notifyDataSetChanged();

            final int index=dubbings_container.indexOf(defaultDubbingSelection);
            if(index!=-1) spinnerDubbing.setSelection(index);
            else {
                defaultDubbingSelection=dubbings_container.get(0);
                spinnerDubbing.setSelection(0);
            }
            updatePlayers();

        }
        private void updatePlayers(){
            int selected_index = 0;
            adapterPlayer.clear();
            try {
                final ArrayList<OneVideo> videos = getVideoEP().getVideos(null,null);
                String dubbing=getSelectedDubbing(videos);
                if(dubbing!=null){
                    OneVideo.OneVideoPlayer player = null;
                    for(int i=0,c=0;i<videos.size();i++) {
                        OneVideo video = videos.get(i);
                        if (video.voiceStudio.toLowerCase().equals(dubbing)) {
                            if (player == null) player = video.player;
                            adapterPlayer.add(video.getPlayerString(mManager.activity.getResources()));
                            if (video.player == defaultVideoPlayer) {
                                selected_index = c;
                                player = video.player;
                            }
                            c++;
                        }
                    }
                    if(selected_index==0&&player!=null)defaultVideoPlayer=player;
                }else adapterPlayer.add(mManager.activity.getString(R.string.not_chosen));

                adapterPlayer.notifyDataSetChanged();
                spinnerPlayer.setSelection(selected_index);
                videoPayerSelect(0);
            } catch (IOException|JSONException ignored) {}


        }
        private void videoPayerSelect(int index_) throws IOException, JSONException {
            final ArrayList<OneVideo> videos = getVideoEP().getVideos(null,null);
            final int videoSelectedIndex = getVideoSelectedIndex(videos);
            if(map.get(index)==null)return;
            map.get(index).video_index =videoSelectedIndex;
            loadQualityFromVideo(videos.get(videoSelectedIndex), new CallBack() {
                @Override
                public void callBack(final ArrayList<OneVideo.VideoType> qualities) {
                    try {
                        updateVideoQualities(defaultVideoPlayer=videos.get(videoSelectedIndex).player,getSelectedDubbing(videos),qualities);
                    } catch (IOException|JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(Exception e) {
                    if(e instanceof IOException){
                        mManager.activity.showNoInternetException();
                    }
                }
            });
        }
        private void updateVideoQualities(@Nullable OneVideo.OneVideoPlayer player,@Nullable String dubbing,final ArrayList<OneVideo.VideoType> qualities) throws IOException, JSONException {
            if(player==null||dubbing==null){
                final ArrayList<OneVideo> list = getVideoEP().getVideos(null,null);
                if(player==null) player = list.get(getVideoSelectedIndex(list)).player;
                if(dubbing==null)dubbing = getSelectedDubbing(list);
            }
            if(qualities==null){ mManager.activity.showInvalidHtmlException(new InvalidHtmlFormatException("Can not parse urls from videos src"));return;}

            int select_index = 0;
            adapterQuality.clear();
            for(int i=0;i<qualities.size();i++) {
                OneVideo.VideoType type = qualities.get(i);
                adapterQuality.add(OneVideoEpisodeQuality.getQualityString(type, mManager.activity));
                if(type==defaultVideoType)select_index =i;
            }
            adapterQuality.notifyDataSetChanged();
            spinnerQuality.setSelection(select_index);
            if(select_index==0)defaultVideoType = qualities.get(0);
        }
        private int getVideoSelectedIndex(ArrayList<OneVideo> videos){
            String selectedDubbing = getSelectedDubbing(videos);
            if(selectedDubbing!=null){
                for(int i=0,index_player_selected = spinnerPlayer.getSelectedItemPosition();i<videos.size();i++){
                    if(videos.get(i).voiceStudio.toLowerCase().equals(selectedDubbing)){
                        if(index_player_selected==0){
                            return i;
                        }else index_player_selected--;
                    }
                }
            }
            return 0;
        }
        private String getSelectedDubbing(ArrayList<OneVideo> videos){
            final ArrayList<String> dubbings=new ArrayList<>();
            String dubbing=null;
            for(OneVideo video:videos){
                if(!dubbings.contains(video.voiceStudio.toLowerCase()))dubbings.add(video.voiceStudio.toLowerCase());
                if(dubbings.size()-1==this.spinnerDubbing.getSelectedItemPosition()){
                    dubbing = dubbings.get(dubbings.size()-1);
                    break;
                }
            }
            return dubbing;
        }
        private OneVideoEP getVideoEP(){
            return mManager.anime.videos.get(index);
        }
    }
    public final SparseArray<EpisodeSelected> map=new SparseArray<>();
    private final HashMap<OneVideo, ArrayList<OneVideo.VideoType>> qualities = new HashMap<>();
    public final static class EpisodeSelected implements Parcelable {
        public int video_index=0;
        public OneVideo.VideoType quality = OneVideo.VideoType.UNDEFINED;


        public static final Creator<EpisodeSelected> CREATOR = new Creator<EpisodeSelected>() {
            @Override
            public EpisodeSelected createFromParcel(Parcel in) {
                EpisodeSelected ep = new EpisodeSelected();
                ep.video_index = in.readInt();
                ep.quality = OneVideo.VideoType.values()[in.readInt()];
                return ep;
            }

            @Override
            public EpisodeSelected[] newArray(int size) {
                return new EpisodeSelected[size];
            }
        };
        @Override public int describeContents() {
            return 0;
        }
        @Override public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(video_index);
            dest.writeInt(quality.ordinal());
        }
    }
    private void loadQualityFromVideo(final OneVideo video, final CallBack callable){
        if(qualities.containsKey(video)){
            callable.callBack(qualities.get(video));
            return;
        }
        final AbstractMap.SimpleEntry<String, OneVideo.OneVideoPlayer> a = new AbstractMap.SimpleEntry<>(video.voiceStudio.toLowerCase(),video.player);
        new Thread(() -> {
            try {
                final ArrayList<OneVideoEpisodeQuality> q = video.getVideoQualities(mManager.anime,mManager.activity,mManager.anime.getHostString(),video,true);
                if (q!=null) {
                    final ArrayList<OneVideo.VideoType> t = new ArrayList<>();
                    for (OneVideoEpisodeQuality i : q) t.add(i.quality);
                    qualities.put(video, t);
                    mManager.activity.threadCallback.post(() -> callable.callBack(t));
                }else qualities.put(video,null);

            } catch (InvalidHtmlFormatException e){mManager.activity.showInvalidHtmlException(e);}
            catch (final IOException e) {
                mManager.activity.threadCallback.post(() -> callable.onError(e));
                mManager.activity.showNoInternetException();
            }
            catch (JSONException e) {mManager.activity.showInvalidJsonError();}
            catch (Throwable e) {mManager.activity.showUndefinedError();}
        }).start();

    }
    private interface CallBack{
        public void callBack(final ArrayList<OneVideo.VideoType> qualities);
        public void onError(Exception e);
    }
}
