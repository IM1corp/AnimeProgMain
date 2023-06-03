package com.imcorp.animeprog.DownloadManager.DownloadWorker;

import android.os.NetworkOnMainThreadException;
import android.util.SparseArray;
import android.widget.Toast;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService;
import com.imcorp.animeprog.DownloadManager.RecycleViewAdapter;
import com.imcorp.animeprog.R;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;
import com.imcorp.animeprog.Requests.JsonObj.Video.Subtitle;

import org.json.JSONException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class Worker{
    final DownloadService.DownloadingInstance instance;
    private final SparseArray<RecycleViewAdapter.EpisodeSelected> map;
    private final ArrayList<OneDownloadThing> q;
    private long animeId;

    private boolean paused = false;
    public WorkerState success=WorkerState.UNDEFINED_ERROR;
    private int videosLoaded = -2;
    int downloadedCount = 0;
    private SimpleDataDownloader simpleDataDownloader;

    final Semaphore pausedLock = new Semaphore(1);
    public Worker(SparseArray<RecycleViewAdapter.EpisodeSelected> map, DownloadService service,final DownloadService.DownloadingInstance instance){
        this.instance = instance;
        this.map = map;
        this.q = new ArrayList<>(this.map.size());
    }
    public void start() throws InterruptedException {
        this.animeId = instance.getService().dataBase.downloads.addToDownloads(instance.anime);
        instance.downloadNotification.show();
        instance.updateDownloadCount(0,q.size());
        parseSaveArgs();
        this.startDownloadingData();
        success = WorkerState.SUCCESS;
    }

    private void startDownloadingData() throws InterruptedException {
        for(Iterator<OneDownloadThing> iterator = q.iterator(); downloadedCount<q.size(); downloadedCount++){
            final OneDownloadThing quality = iterator.next();
            if(simpleDataDownloader==null||!simpleDataDownloader.getEpName().equals(quality.num))
            {
                if(quality.episodeDownload.m3u8Url!=null)
                    simpleDataDownloader = new MP3U8Downloader(this,q.size());
                else if(quality.episodeDownload.mp4Url!=null)
                    simpleDataDownloader = new MP4Downloader(this,q.size());
                else continue;
            }
            simpleDataDownloader.downloadData(quality);
        }
    }

    private void parseSaveArgs() throws InterruptedException{
        if(videosLoaded==-2) {
            for (int i = 0; i < map.size(); i++) {
                int video_ep = map.keyAt(i);
                RecycleViewAdapter.EpisodeSelected video = map.get(video_ep);
                if (video == null) continue;
                final ArrayList<OneVideo> videos;
                try {
                    if (instance.anime.videos.get(video_ep).videosLoaded())
                        Thread.sleep(Config.DOWNLOADS_SLEEP_TIME_MS);
                    videos = instance.anime.videos.get(video_ep).getVideos(instance.anime, instance.getService().request);
                } catch (IOException | JSONException | NetworkOnMainThreadException e) {
                    //TODO: activity.showUndefinedError();
                    Toast.makeText(instance.getService(), R.string.undefined_error, Toast.LENGTH_SHORT).show();
                    continue;
                }
                OneVideo this_video = videos.get(video.video_index);
                if (this_video.videoQualities == null || this_video.videoQualities.size() == 0)
                    continue;
                OneVideoEpisodeQuality quality_main = this_video.videoQualities.get(0);
                for (OneVideoEpisodeQuality quality : this_video.videoQualities) {
                    if (quality.quality == video.quality) {
                        quality_main = quality;
                        break;
                    }
                }
                this.q.add(new OneDownloadThing(this_video.num,quality_main,this_video.videoSubtitles));
            }
            videosLoaded=instance.anime.videos.size()-1;
        }
        //addToDataBase
        for(;videosLoaded>=0;videosLoaded--){
            OneVideoEP video = instance.anime.videos.get(videosLoaded);
            final RecycleViewAdapter.EpisodeSelected is_downloading_index =map.get(videosLoaded,null);
            if(!video.videosLoaded())Thread.sleep(Config.DOWNLOADS_SLEEP_TIME_MS);
            final ArrayList<OneVideo> videos;
            try {
                videos = video.getVideos(instance.anime,instance.getService().request);
            }
            catch (InterruptedIOException ignored){
                Thread.currentThread().interrupt();
                return;
            }
            catch (IOException|JSONException e) {
                continue;
            }
            if(Thread.currentThread().isInterrupted())throw new InterruptedException();
            for(int i=0;i<videos.size();i++){
                final OneVideo one_video = videos.get(i);
                instance.getService().dataBase.downloads.addVideoToDownloads(animeId,one_video,is_downloading_index!=null && is_downloading_index.video_index==i);
            }

        }
    }
    public void notifyPauseButtonClicked() {
        if(paused = !paused){
            try {
                pausedLock.acquire();
            } catch (InterruptedException ignored) {}
        } else {
            pausedLock.release();
        }
        instance.downloadNotification.onPause(paused);

    }
    public void notifyCancelButtonClicked() {
        success = WorkerState.CANCELED;
        instance.serviceLooper.getThread().interrupt();
    }



    public static enum WorkerState{SUCCESS,CANCELED,UNDEFINED_ERROR}
    public static class OneDownloadThing{
        String num;
        OneVideoEpisodeQuality episodeDownload;
        ArrayList<Subtitle> subtitles;
        public OneDownloadThing(){}
        public OneDownloadThing(String num, OneVideoEpisodeQuality episodeDownload, ArrayList<Subtitle> subtitles){
            this.num=num;
            this.episodeDownload=episodeDownload;
            this.subtitles=subtitles;
        }
    }
}