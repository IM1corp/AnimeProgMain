package com.imcorp.animeprog.DownloadManager.DownloadWorker;

import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Requests.JsonObj.Video.Subtitle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class SimpleDataDownloader {
    final Worker worker;
    final int downloadCount;

    public SimpleDataDownloader(Worker worker,final int downloadCount){
        this.worker = worker;
        this.downloadCount = downloadCount;
    }
    public abstract void destruct();
    public abstract void downloadData(Worker.OneDownloadThing data) throws InterruptedException;

    public abstract String getEpName();
    protected void lockThreadIfPaused() throws InterruptedException {
        if(worker.pausedLock.availablePermits()==0){
            worker.pausedLock.acquire();
            worker.pausedLock.release();
        }
    }
    protected void downloadSubtitles(ArrayList<Subtitle> subtitles, String ref) {
        if(subtitles!=null)
            for(Subtitle i:subtitles){
                try {
                    File whereToSave = worker.instance.getService().dataBase.loader.getAnimeLocalPath(worker.instance.anime.getPath());
                    whereToSave = new File(whereToSave,i.getFileName());
                    final String text = worker.instance.getService().request.loadFrameFromUrl(i.url, ref);
                    try (PrintWriter out = new PrintWriter(whereToSave)) {
                        out.println(text);
                    }

                } catch (IOException e) {
                    Log.e(Config.LOG_TAG, e.getMessage(),e);
                }

            }
    }
}
