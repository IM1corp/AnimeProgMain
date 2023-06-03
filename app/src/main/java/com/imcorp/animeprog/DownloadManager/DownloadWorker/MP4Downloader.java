package com.imcorp.animeprog.DownloadManager.DownloadWorker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

public class MP4Downloader extends SimpleDataDownloader {
    private String name;
    private long progress = 0,size=-1;
    private byte retryCount = 0;
    private FileOutputStream fileStream;
    private HttpURLConnection conn;
    private OneVideoEpisodeQuality ep;
    private BufferedInputStream readStream;

    MP4Downloader(Worker worker, int downloadCount) {
        super(worker, downloadCount);
    }

    private void doIT() throws InterruptedException {
        try {
            final File f = new File(worker.instance.getService().dataBase.loader.getAnimeLocalPath(worker.instance.anime.getPath()), name + ".mp4");
            if (f.exists()) f.delete();
            fileStream = new FileOutputStream(f);

            while(retryCount<3) {
                try {
                    conn = worker.instance.getService().request.downloadData(ep.mp4Url, new HashMap<String, String>() {{
                        put("Referer", ep.ref);
                    }});
                    if (progress != 0) conn.setRequestProperty("Range", "bytes=" + progress + "-");
                    conn.connect();

                    this.readStream = new BufferedInputStream(conn.getInputStream());

                    if(size==-1)size=conn.getContentLength();
                    else if(conn.getContentLength()==size) this.readStream.skip(progress); //if Range header do not works

                    this.startDownloadingLoop();

                    progress = 0;

                    break;
                }
                catch (InterruptedIOException ex) {
                    throw new InterruptedException();
                }
                catch (IOException exception) {
                    this.onIOException(exception);
                }
                finally {
                    if(conn!=null)conn.disconnect();
                }
            }
        }
        catch (IOException ignored) {}
        finally {
            this.destruct();
        }
    }

    private void startDownloadingLoop() throws IOException, InterruptedException {
        byte[] buffer = new byte[1024];
        int dataBuffer;
        while ((dataBuffer = readStream.read(buffer, 0, 1024)) != -1) {
            fileStream.write(buffer, 0, dataBuffer);
            progress += dataBuffer;
            worker.instance.updateProgress(progress, size);
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedIOException();
            }
            this.lockThreadIfPaused();
        }
    }

    private void onIOException(IOException e) throws InterruptedException {
        ConnectivityManager m = (ConnectivityManager) worker.instance.getService().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo internetOn = m == null ? null : m.getActiveNetworkInfo();
        if (internetOn != null && internetOn.isConnected())
            retryCount++;
        Thread.sleep(2000);
    }
    @Override public void downloadData(final Worker.OneDownloadThing data) throws InterruptedException {
        this.name = data.num;
        this.ep = data.episodeDownload;
        worker.instance.updateDownloadCount(worker.downloadedCount+1,downloadCount);
        this.downloadSubtitles(data.subtitles, data.episodeDownload.ref);
        this.doIT();
    }
    @Override public String getEpName() {
        return name;
    }
    public void destruct(){
        if(fileStream!=null) {
            try { fileStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            fileStream = null;
        }
    }
}
