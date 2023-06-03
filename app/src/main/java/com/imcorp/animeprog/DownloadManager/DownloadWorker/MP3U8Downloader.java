package com.imcorp.animeprog.DownloadManager.DownloadWorker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.ArrayUtilsKt;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo;
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MP3U8Downloader extends SimpleDataDownloader {
    private String name;
    private OneVideoEpisodeQuality ep;

    private OneEpDownloader downloader;

    MP3U8Downloader(Worker worker, int downloadCount) {
        super(worker, downloadCount);
    }

    @Override
    public void destruct() {}
    private void doIT() throws InterruptedException {
        final File anime_path = worker.instance.getService().dataBase.loader.getAnimeLocalPath(worker.instance.anime.getPath());
        final File anime_dir = new File(anime_path, name);

        if (!anime_dir.isDirectory() && !anime_dir.mkdirs() && !anime_dir.isDirectory()) {
            worker.success = Worker.WorkerState.UNDEFINED_ERROR;
            throw new InterruptedException("Can not create directory");
        }
        this.downloader = new OneEpDownloader(anime_dir, ep.m3u8Url, Config.M3U8_DOWNLOAD_FNAME);
        while(downloader.downloadCount<3) {
            try{
                this.downloader.start();
                //test
//                try(FileOutputStream outStream = new FileOutputStream(new File(anime_path,name+".mp4"))) {
//                    for (File file : anime_dir.listFiles()) {
//                        if(file.getName().endsWith(".ts")) {
//                            RandomAccessFile readStream = new RandomAccessFile(file, "r");
//                            byte[] b = new byte[(int)readStream.length()];
//                            readStream.readFully(b);
//                            readStream.close();
//                            try {
//                                outStream.write(b);
//                            }catch (Exception e){
//                                int g = 12;
//                            }
//                        }
//                        file.delete();
//                    }
//                    anime_dir.delete();
//                }
                break;
            }catch (IOException e){
                this.onIOException(e);
            }catch (Exception e){
                if(e instanceof InterruptedException)throw e;
                else{
                    if(Config.NEED_LOG) Log.e(Config.DOWNLOAD_SERVICE_TAG,e.getMessage(),e);
                }
            }
        }
        //File stream will not close file if paused
    }
    private void onIOException(IOException e) throws InterruptedException {
        ConnectivityManager m = (ConnectivityManager) worker.instance.getService().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo internetOn = m == null ? null : m.getActiveNetworkInfo();
        if (internetOn != null && internetOn.isConnected())
            downloader.downloadCount++;
        Thread.sleep(2000);
    }
    @Override public void downloadData(final Worker.OneDownloadThing data) throws InterruptedException {
        this.name = data.num;
        this.ep = data.episodeDownload;
        worker.instance.updateDownloadCount(worker.downloadedCount+1,downloadCount);
        downloadSubtitles(data.subtitles, data.episodeDownload.ref);
        this.doIT();
    }
    @Override public String getEpName() {
        return name;
    }
    private class OneEpDownloader{
        private final String url,downloadFName;
        private final File path;
        private String[] lines;
        private LinkedList<OneTsSegment> dataTs;
        private Queue<OneEpDownloader> downloader;
        byte downloadCount = 0;
        public OneEpDownloader(final File path, final String url,final String downloadFName){
            this.path = path;
            this.downloadFName = downloadFName;
            this.url = url.replace('\\','/');
        }
        public void start() throws InterruptedException, IOException {
            int current_line = -1;
            if(current_line ==-1||lines==null)this.getLines();
            if(downloader!=null){
                OneEpDownloader ep;
                while((ep = downloader.poll())!=null) {
                    ep.start();
                }
            }

            long downloadedData = 0;
            int progress=0;
            for(OneTsSegment seg :this.dataTs){
                progress++;
                byte retryCount=0;
                while(retryCount<3) {
                    HttpURLConnection conn=null;
                    try {
                        conn = worker.instance.getService().request.downloadData(seg.url, new HashMap<String, String>() {{
                            put("Referer", ep.ref);
                        }});
                        conn.connect();
                        if (!seg.whereToSave.exists()) seg.whereToSave.createNewFile();
                        try(DataInputStream dis = new DataInputStream(conn.getInputStream())){
                            byte[] buffer = new byte[1024];
                            int length,totalLength=0;
                            try(FileOutputStream fos = new FileOutputStream(seg.whereToSave)){
                                while ((length = dis.read(buffer))>0) {
                                    fos.write(buffer, 0, length);
                                    totalLength+=length;
                                }
                            }
                            catch (Exception e){
                                if(Config.NEED_LOG) Log.e(Config.DOWNLOAD_SERVICE_TAG,e.getMessage(),e);
                                break;
                            }
                            downloadedData+=totalLength;
                            worker.instance.updateProgress(downloadedData,
                                    (long) (((double)downloadedData) / progress * this.dataTs.size()));
                            lockThreadIfPaused();
                            break;
                        }
                    }
                    catch (InterruptedIOException ex) {
                        throw new InterruptedException();
                    }
                    catch (IOException exception) {
                        ConnectivityManager m = (ConnectivityManager) worker.instance.getService().getSystemService(Context.CONNECTIVITY_SERVICE);
                        final NetworkInfo internetOn = m == null ? null : m.getActiveNetworkInfo();
                        if (internetOn != null && internetOn.isConnected())
                            retryCount++;
                        Thread.sleep(2000);
                    }
                    finally {
                        if(conn!=null)conn.disconnect();
                    }
                }
            }
        }
        private void parseLines(FileWriter f) throws IOException {
            this.dataTs = new LinkedList<>();
            final LinkedList<OneVideoDownload> videos = new LinkedList<>();
            final HashMap<String, OneEpDownloader> audios = new HashMap<String, OneEpDownloader>();
            String audioID=null,lastLine=null;
            boolean thisLineTs = false,thisLineVideo=false;
            OneVideo.VideoType type=null;
            for (String line : lines) {
                if (line.startsWith("#")) {
                    if (line.startsWith("#EXTINF:")) {
                        thisLineTs = true;
                    }
                    else if (line.startsWith("#EXT-X-STREAM-INF:") || line.startsWith("#EXT-X-I-FRAME-STREAM-INF")) {
                        final HashMap<String,String> params = parseLineParameters(line.substring(line.startsWith("#EXT-X-STREAM-INF:")?18:25));//"#EXT-X-STREAM-INF:".length ==18
//                        final Matcher resolution = Pattern.compile("RESOLUTION=\\d+x(\\d)+").matcher(line);
//                        if (!resolution.find()) type = OneVideo.VideoType.UNDEFINED;
                        if(params.containsKey("RESOLUTION")) switch (params.get("RESOLUTION").split("x")[1]){
                            case "1080":
                                type = OneVideo.VideoType.V1080;
                                break;
                            case "720":
                                type = OneVideo.VideoType.V720;
                                break;
                            case "480":
                                type = OneVideo.VideoType.V480;
                                break;
                            case "360":
                                type = OneVideo.VideoType.V360;
                                break;
                            default:
                                type = OneVideo.VideoType.UNDEFINED;
                        }
                        else type = OneVideo.VideoType.UNDEFINED;
                        if (params.containsKey("AUDIO")) audioID = params.get("AUDIO");

                        thisLineVideo = true;
                        lastLine=line;
                        continue;
                    }
                    else if (line.startsWith("#EXT-X-MAP:")){
                        final HashMap<String,String> params = parseLineParameters(line.substring(11));
                        if(params.containsKey("URI")){
                            String fn = params.get("URI");
                            dataTs.add(new OneTsSegment(getAbsoluteUrl(fn),
                                    getAbsoluteFile(getAbsoluteFName(fn))
                            ));
                        }
                    }
                    else if (line.startsWith("#EXT-X-MEDIA:")){
                        final HashMap<String,String> params = parseLineParameters(line.substring(13));
                        if (params.get("TYPE").equalsIgnoreCase("AUDIO")) {
                            String url = params.get("URI");
                            url = getAbsoluteFName(url);
                            if (url != null) {
                                final String name = params.get("GROUP-ID");
                                audios.put(name,new OneEpDownloader(this.path,getAbsoluteUrl(url),url));
                            }
                        }
                    }
                    f.write(line);
                }
                else if (thisLineTs) {
                    final String file_name = getAbsoluteFName(line);
                    f.write(file_name.replace(':','-'));
                    this.dataTs.addLast(new OneTsSegment(getAbsoluteUrl(file_name), getAbsoluteFile(file_name.replace(':','-'))));
                    thisLineTs = false;
                }
                else if (thisLineVideo) {
                    String fName = getAbsoluteFName(line);
                    //f.write(fName);
                    final File fNames = getAbsoluteFile(fName);
                    final OneEpDownloader downloader = new OneEpDownloader(fNames.getParentFile(), getAbsoluteUrl(fName), fNames.getName());
                    videos.add(new OneVideoDownload(type, downloader,lastLine+'\n'+fName+'\n'));
                    thisLineVideo = false;
                }
                f.write("\n");
            }
            downloader = new LinkedList<OneEpDownloader>(){{
                if(!videos.isEmpty()) {
                    boolean success=false;
                    final OneVideo.VideoType q = worker.instance.getService().dataBase.settings.getQuality();
                    for (OneVideoDownload e : ArrayUtilsKt.sortBy(videos,(a,b)->a.type.ordinal()-b.type.ordinal())) {
                        if (e.type.ordinal() >= q.ordinal()) {
                            add(e.downloader);
                            f.write(e.writeData);
                            success = true;
                            break;
                        }
                    }
                    if (!success) {
                        f.write(videos.getFirst().writeData);
                        add(videos.getFirst().downloader);
                    }
                }
            }};
            if(audioID!=null) {
                OneEpDownloader el = audios.get(audioID);
                if(el==null) {
                    el = audios.values().iterator().next();
                    if(el!=null) downloader.add(el);
                }
                else downloader.add(el);
            }

        }
        private void getLines() throws IOException, InterruptedException {
            final String data = worker.instance.getService().request.loadTextFromUrl(this.url, false, ep.ref, true).toString();
            lines = data.split("\\r?\\n");
            try (FileWriter f = new FileWriter(new File(path, downloadFName))) {
                this.parseLines(f);
            } catch (IOException ignored) {
                worker.success = Worker.WorkerState.UNDEFINED_ERROR;
                throw new InterruptedException("Can not write data to file");
            }
        }

        private String getAbsoluteFName(String fName){
            if(fName==null) return null;
            if(fName.startsWith("./")||fName.startsWith(".\\"))
                fName = fName.substring(2);
            else if(fName.startsWith("/")) {
                fName = fName.substring(1);
                final File f = new File(path,fName);
                f.getParentFile().mkdirs();
            }
            return fName;
        }
        private File getAbsoluteFile(String fName){
            return new File(path, fName);
        }
        private String getAbsoluteUrl(String fName){
            final int lastIndex = url.lastIndexOf("/");
            return url.substring(0,lastIndex+1)+fName;
        }

        private final class OneTsSegment{
            String url;
            File whereToSave;
            public OneTsSegment(String url,File whereToSave){
                this.url=url;
                this.whereToSave=whereToSave;
            }

        }
        private final class OneVideoDownload{
            public OneVideo.VideoType type;
            public OneEpDownloader downloader;
            public String writeData;

            public OneVideoDownload(OneVideo.VideoType type, OneEpDownloader downloader, String writeData) {
                this.type =type;
                this.downloader=downloader;
                this.writeData=writeData;
            }
        }
    }

    private HashMap<String, String> parseLineParameters(String line) {
        final HashMap<String,String> answer = new HashMap<>();
        Matcher m = findLinePattern.matcher(line);
        while (m.find()) {
            final String[] values = m.group(0).split("=",2);
            answer.put(values[0].toUpperCase(),values[1].substring(1,values[1].length()-1));
        }
        m=findLinePattern2.matcher(line);
        while (m.find())
            answer.put(m.group(1).toUpperCase(),m.group(2));
        return answer;
    }
    private final Pattern findLinePattern = Pattern.compile("[a-zA-Z0-9\\-]+=(\")(?:(?=(\\\\?))\\2.)*?\\1");
    private final Pattern findLinePattern2 = Pattern.compile("([a-zA-Z0-9\\-]+)=([a-z0-9A-Z]+),");
}
