package com.imcorp.animeprog.Requests.Http;

import static com.imcorp.animeprog.Config.coding;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.imcorp.animeprog.BuildConfig;
import com.imcorp.animeprog.Config;
import com.imcorp.animeprog.Default.MyApp;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.spec.InvalidParameterSpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ReqBuild {
    private static final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    } };
    private static final Map<String, Map<String, String> > cookiesForSite = new HashMap<>();
    private static final String userAgent = JummyAnimeAdapter.INSTANCE.getRandomUserAgent().getName();
    static{
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final Context mContext;
    private final StringBuilder url;
    private String url_;
    private StringBuilder post_data = null;
    private final String method;
    private final HashMap<String, String> headers = new HashMap<>();
    private boolean addingFirstTime = true;

    ReqBuild(Context context , String url) {
        this.url = new StringBuilder(url);
        this.method = "POST";
        this.mContext = context;
        this.addingFirstTime = !url.contains("?");
    }

    public ReqBuild(Context context, String url, boolean post) {
        this(context, url, post?"POST": "GET");
    }
    public ReqBuild(Context context, String url, String method){
        this.url = new StringBuilder(url);
        this.method = method;
        this.mContext = context;
        this.addingFirstTime = !url.contains("?");
    }
    public ReqBuild add(HashMap<String, String> keyValuePair) throws UnsupportedEncodingException {
        for (Map.Entry<String, String> s : keyValuePair.entrySet()) {
            this.add(s.getKey(), s.getValue());
        }
        return this;
    }
    public ReqBuild add(String key, String value) throws UnsupportedEncodingException {
        this.url.append(addingFirstTime ? '?' : '&')
                .append(key)
                .append('=')
                .append(URLEncoder.encode(value, coding));
        addingFirstTime = false;
        return this;
    }
    public ReqBuild add(String key, int value) {
        return add(key, (long)value);
    }
    public ReqBuild add(String key, long value) {
        this.url.append(addingFirstTime ? '?' : '&')
                .append(key)
                .append('=')
                .append(value);
        addingFirstTime = false;
        return this;
    }
    public ReqBuild addPost(String key, String value) throws UnsupportedEncodingException {
        if (post_data == null) {
            post_data = new StringBuilder();
        }
        else {
            post_data.append("&");
        }
        post_data.append(key).append("=").append(URLEncoder.encode(value, coding));
        return this;
    }
    public ReqBuild addPostJson(JSONObject json) throws InvalidParameterSpecException {
        if(post_data == null)
            post_data = new StringBuilder();
        else throw new InvalidParameterSpecException("Body had already been set!");
        post_data.append(json.toString());
        this.addHeader("content-type", "application/json");
        return this;
    }
    public ReqBuild addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    public ReqBuild addXRequestsWithHeader() {
        return this.addHeader("X-Requested-With", "XMLHttpRequest");
    }
    public ReqResponse SendRequestWithImage(final @Nullable byte[] image) throws IOException {
        HttpURLConnection httpConn = initRequest();
        //region image
        if (image != null) {
            String boundary = "===" + System.currentTimeMillis() + "===";

            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            OutputStream outputStream = httpConn.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, coding), true);
            writer.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"")
                    .append("ava")
                    .append("\"; filename=\"").append("user.jpeg").append("\"").append("\r\n")
                    .append("Content-Type: image/jpeg").append("\r\n").append("Content-Transfer-Encoding: binary").append("\r\n").append("\r\n");
            writer.flush();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();

            writer.append("\r\n");
            writer.flush();

            writer.append("\r\n").flush();
            writer.append("--")
                    .append(boundary)
                    .append("--")
                    .append("\r\n")
                    .close();
        }
        //endregion
        return new ReqResponse(this.sendRequestFromHttpConn(httpConn));
    }
    public ReqResponse SendRequest() throws IOException {
        HttpURLConnection httpConn = initRequest();
        return new ReqResponse(this.sendRequestFromHttpConn(httpConn));
    }
    HttpURLConnection initRequest() throws IOException {
        url_ = this.url.toString();
        if (!url_.startsWith("http")) {
            if (url_.startsWith("//")) {
                url_ = "https:" + url_;
            }
            else url_ = "https://" + url_;
        }
        URL urlRe = new URL(url_);

        HttpURLConnection httpConn = BuildConfig.DEBUG?
                (HttpURLConnection) urlRe.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.246", 8888)))
                : (HttpURLConnection) urlRe.openConnection();
        httpConn.setRequestMethod(method);
        httpConn.setConnectTimeout(20000);
        httpConn.setReadTimeout(20000);
        httpConn.setDoInput(true);
        if (url_.startsWith("https")) {
            try {
                httpConn.addRequestProperty("User-Agent",userAgent);
                //s.getSslContext(httpConn.getSSLSocketFactory(),"yummy-anime.ru");
            } catch (Exception e) {
                if(Config.NEED_LOG) Log.e(Config.LOG_TAG,e.getMessage(),e);
            }
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpConn.addRequestProperty(entry.getKey(), entry.getValue());
        }
        return httpConn;
    }
    private String sendRequestFromHttpConn(HttpURLConnection httpConn) throws IOException {
        InputStreamReader isReader = null;
        BufferedReader bufReader = null;
        if (!headers.containsKey("User-agent")) headers.put("User-agent", Config.user_agent);
        if (post_data != null) {
            httpConn.setDoOutput(true);
            httpConn.setUseCaches(false);

            OutputStream os = httpConn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, coding));
            writer.write(post_data.toString());
            writer.flush();
            writer.close();
            os.close();
        }
        final StringBuilder readTextBuf = new StringBuilder();

        try {
//            if(url_.startsWith(Config.YUMMY_ANIME_URL)){
//                final JummyAnimeAdapter.TextWebView v = new JummyAnimeAdapter.TextWebView(mContext, url_, Config.YUMMY_ANIME_URL, headers);
//                ((MyApp) mContext).threadCallback.post(v::initilize);
//                try {
//                    v.lock();
//                    if(v.isError())throw new IOException(v.answer);
//                    return v.answer;
//                }
//                catch (InterruptedException e){throw new InterruptedIOException(e.getMessage()); }
//                finally {
//                    v.stop();
//                }
//            } else {
                httpConn.connect();
                int status = httpConn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK ) {
//                    if(status == HttpURLConnection.HTTP_FORBIDDEN){
//                        return antiCloudFlare();
//                    }
                    InputStream inputStream = httpConn.getErrorStream();
                    isReader = new InputStreamReader(inputStream);
                    bufReader = new BufferedReader(isReader);
                    String line = bufReader.readLine();
                    while (line != null) {
                        readTextBuf.append(line).append("\n");
                        line = bufReader.readLine();
                    }
                    String error = readTextBuf.toString();

                    throw new InvalidStatusException("Status is not ok", status, error);
                }

                InputStream inputStream = httpConn.getInputStream();
                isReader = new InputStreamReader(inputStream);
                bufReader = new BufferedReader(isReader);
                String line = bufReader.readLine();
                while (line != null) {
                    readTextBuf.append(line).append("\n");
                    line = bufReader.readLine();
                }
            return readTextBuf.toString();
//            }
        } finally {
            if (bufReader != null) {
                bufReader.close();
            }
            if (isReader != null) {
                isReader.close();
            }
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
    private String antiCloudFlare() throws IOException {
        TestCLF cf = new TestCLF(this.url_);
        cf.setUser_agent("YOUR USER AGENT HERE");
        cf.getCookies(new TestCLF.cfCallback() {
            @Override
            public void onSuccess(List<HttpCookie> cookieList) {
                //convert the cookielist to a map
                Map<String, String> cookies = TestCLF.List2Map(cookieList);
//                ReqBuild.cookiesForSite.remove()
            }

            @Override
            public void onFail() {
                Log.d("ERROR_UNKNOWN", "OMG IT FAILED!!!");
            }
        });

        final JummyAnimeAdapter.TextWebView v = new JummyAnimeAdapter.TextWebView(mContext, url_, Config.ANIMEJOY_URL, headers);
        ((MyApp) mContext).threadCallback.post(v::initilize);
        try {
            v.lock();
            if(v.isError())throw new IOException(v.answer);
            return v.answer;
        }
        catch (InterruptedException e){throw new InterruptedIOException(e.getMessage()); }
        finally {
            v.stop();
        }
    }
}
