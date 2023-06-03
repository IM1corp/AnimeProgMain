package com.imcorp.animeprog.Requests.Http

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.Semaphore
import javax.net.ssl.*
import kotlin.random.Random


object JummyAnimeAdapter {

    private val chromeVersions = arrayOf(
            arrayOf("1331.54", "2661.102", "2661.18", "2661.75", "2661.87", "2661.94", "2661.95"),
            arrayOf("2683.0", "2704.103", "2704.106", "2704.47", "2704.63", "2704.79", "2704.84", "2741.70"),
            arrayOf("2704.79", "2743.116", "2743.75", "2743.82", "82"),
            arrayOf("0.11780", "2785.101", "2785.104", "2785.113", "2785.116", "2785.143", "2785.21", "2785.70", "2785.89", "2410.8782"),
            arrayOf("2704.103", "2824.0", "2840.27", "2840.59", "2840.71", "2840.87", "2840.99", "2878.86", "2840.71"),
            arrayOf("0.12195", "2490.80", "2883", "2883.103", "2883.105", "2883.11", "2883.75", "2883.87", "2883.95", "2883.96", "3130.117", "5138.0866", "2883.75", "2883.75", "2883.75"),
            arrayOf("1750.154", "2911.0", "2924.28", "2924.51", "2924.67", "2924.76", "2924.87", "2924.90", "2944.0", "2924.87", "2924.87", "2924.87", "2924.87"),
            arrayOf("0.12335", "2345.113", "2945.0", "2950.4", "2951.0", "2954.0", "2960.0", "2970.0", "2979.0", "2984.0", "2985.0", "2986.0", "2987.0", "2987.108", "2987.110", "2987.133", "2987.137", "2987.138", "2987.21", "2987.74", "2987.88", "2987.98", "2987.98", "2987.98", "2987.98", "2987.98"),
            arrayOf("2988.0", "2993.0", "3014.0", "3018.3", "3022.0", "3029.110", "3029.114", "3029.19", "3029.33", "3029.41", "3029.54", "3029.81", "3029.82", "3029.96", "3029.97", "3249.33", "3029.81"),
            arrayOf("3032.0", "3036.0", "3041.0", "3042.4", "3044.0", "3047.4", "3048.0", "3051.3", "3071.104", "3071.109", "3071.112", "3071.115", "3071.15", "3071.29", "3071.30", "3071.36", "3071.47", "3071.61", "3071.71", "3071.86", "3071.90"),
            arrayOf("0.1508", "3075.0", "3095.5", "3100.0", "3112.101", "3112.105", "3112.113", "3112.50", "3112.66", "3112.72", "3112.78", "3112.90"),
            arrayOf("3113.0", "3116.0", "3124.11", "3141.7", "3143.0", "3147.0", "3154.0", "3155.1", "3159.5", "3163.100", "3163.102", "3163.31", "3163.39", "3163.49", "3163.59", "3163.79", "3163.91"),
            arrayOf("3167.0", "3188.4", "3192.0", "3202.29", "3202.38", "3202.45", "3202.62", "3202.75", "3202.84", "3202.89", "3202.9", "3202.94", "3202.97"),
            arrayOf("3219.0", "3239.108", "3239.132", "3239.18", "3239.26", "3239.52", "3239.59", "3239.84"),
            arrayOf("3251.0", "3255.0", "3282.100", "3282.119", "3282.140", "3282.167", "3282.186", "3282.24", "3396.62"),
            arrayOf("3288.0", "3298.4", "3325.146", "3325.162", "3325.181", "3325.183", "3325.19", "3325.52"),
            arrayOf("3343.4", "3355.4", "3359.117", "3359.139", "3359.158", "3359.170", "3359.181", "3359.66", "3359.81"),
            arrayOf("3377.1", "3381.1", "3393.4", "3396.10", "3396.103", "3396.18", "3396.48", "3396.62", "3396.79", "3396.87", "3396.99"),
            arrayOf("3409.2", "3416.0", "3418.2", "3423.2", "3427.0", "3432.3", "3440.106", "3440.17", "3440.25", "3440.59", "3440.7", "3440.75", "3440.84"),
            arrayOf("2343.52", "3449.0", "3463.1", "3497.100", "3497.12", "3497.32", "3497.42", "3497.81", "3497.82", "3497.92"),
            arrayOf("3535.0", "3538.102", "3538.110", "3538.113", "3538.25", "3538.67", "3538.77"),
            arrayOf("3578.80", "3578.98"),
            arrayOf("3626.104", "3626.109", "3626.110", "3626.119", "3626.121", "3626.122", "3626.81", "3626.96"),
            arrayOf("3683.103", "3683.75", "3683.86"),
            arrayOf("3729.108", "3729.131", "3729.136", "3729.157", "3729.169", "3729.91"),
            arrayOf("3739.0", "3770.66"),
            arrayOf("3788.1"))
    const val from = 50
    const val to = 76
    fun generateRandomChromeV() : String{
        val v1 = Random.nextInt(from, to)
        val arr:Array<String> = chromeVersions[v1 - from]
        return "${v1}.0.${arr[Random.nextInt(arr.size - 1)]}"
    }
    public fun getRandomUserAgent():User_Agent{
        val system = when(Random.nextBits(2)){
            0 -> System.DARWIN;1 -> System.LINUX;else->System.WINDOWS}
        val systemString = when (system) {
            System.WINDOWS -> {
                val data = arrayOf("; WOW64", "; Win64; x64", "");
                val versions = arrayOf("6.1", "5.1", "10.0", "6.3", "6.2")
                "Windows NT ${versions[Random.nextBits(3) % versions.size]}${data[Random.nextInt(data.size)]}"
            }
            System.DARWIN -> {
                val versions = arrayOf("9_5", "10_5", "11_4", "11_5", "11_6", "12_2", "11_2",
                        "11_4", "10_2", "12_3", "12_6", "13_0", "13_1", "13_2", "13_3", "13_4",
                        "13_5", "13_6")
                "Macintosh; Intel Mac OS X 10_${versions[Random.nextInt(versions.size - 1)]}"
            }
            System.LINUX -> "X11; Linux x86_64"
        }

        fun firefoxBrowser() : User_Agent {
            val firefoxVList = arrayOf("50.0", "51.0", "52.0", "52.9", "53.0", "54.0", "55.0", "56.0",
                    "57.0", "58.0", "59.0", "60.0", "60.9", "61.0", "62.0", "63.0", "64.0", "65.0", "66.0")
            val firefoxV = firefoxVList[Random.nextInt(firefoxVList.size - 1)]
            return User_Agent("Mozilla/5.0 (${systemString}; rv:${firefoxV}) " +
                    "Gecko/20100101 Firefox/${firefoxV}",
                    Browser.FIREFOX, system)
        }
        fun chromeBrowser() : User_Agent = User_Agent(
                "Mozilla/5.0 (${systemString}) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${generateRandomChromeV()} Safari/537.36",
                Browser.CHROME, system
        )

        return if(Random.nextBoolean()) chromeBrowser() else firefoxBrowser()
    }

    class User_Agent(var name: String, private var browser: Browser, private var system: System){
        val headers = when(browser){
            Browser.CHROME -> arrayOf(
                    Pair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"),
                    Pair("Accept-Language", "en-US,en;q=0.9")
                    // Pair("Accept-Encoding", "gzip, deflate, br")
            )
            Browser.FIREFOX -> arrayOf(
                    Pair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"),
                    Pair("Accept-Language", "en-US,en;q=0.5")
                    // Pair("Accept-Encoding", "gzip, deflate, br")
            )
        }
        private val cipherSuite = when(browser){
            Browser.CHROME -> arrayOf("TLS_AES_128_GCM_SHA256",
                    "TLS_AES_256_GCM_SHA384",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "ECDHE-ECDSA-AES128-GCM-SHA256",
                    "ECDHE-RSA-AES128-GCM-SHA256",
                    "ECDHE-ECDSA-AES256-GCM-SHA384",
                    "ECDHE-RSA-AES256-GCM-SHA384",
                    "ECDHE-ECDSA-CHACHA20-POLY1305",
                    "ECDHE-RSA-CHACHA20-POLY1305",
                    "ECDHE-RSA-AES128-SHA",
                    "ECDHE-RSA-AES256-SHA",
                    "AES128-GCM-SHA256",
                    "AES256-GCM-SHA384",
                    "AES128-SHA",
                    "AES256-SHA",
                    "DES-CBC3-SHA")
            Browser.FIREFOX -> arrayOf(
                    "TLS_AES_128_GCM_SHA256",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_AES_256_GCM_SHA384",
                    "ECDHE-ECDSA-AES128-GCM-SHA256",
                    "ECDHE-RSA-AES128-GCM-SHA256",
                    "ECDHE-ECDSA-CHACHA20-POLY1305",
                    "ECDHE-RSA-CHACHA20-POLY1305",
                    "ECDHE-ECDSA-AES256-GCM-SHA384",
                    "ECDHE-RSA-AES256-GCM-SHA384",
                    "ECDHE-ECDSA-AES256-SHA",
                    "ECDHE-ECDSA-AES128-SHA",
                    "ECDHE-RSA-AES128-SHA",
                    "ECDHE-RSA-AES256-SHA",
                    "AES128-SHA",
                    "AES256-SHA",
                    "DES-CBC3-SHA")
        }
        public fun connectToHttpsUrlConnection(conn: HttpsURLConnection){
            conn.sslSocketFactory = TLSSocketFactory(LinkedList(cipherSuite.asList()))
        }
        public fun setHeaders(conn: HttpsURLConnection){
            conn.setRequestProperty("User-Agent", this.name)
            //for(i in headers) conn.setRequestProperty(i.first,i.second)
        }

    }
    enum class Browser{ FIREFOX, CHROME }
    enum class System{ WINDOWS,LINUX,DARWIN}

    class TLSSocketFactory(enabledChipperSuites: LinkedList<String>) : SSLSocketFactory() {
        private val delegate: SSLSocketFactory
        private val enabledChipperSuites: Array<String>
        override fun getDefaultCipherSuites(): Array<String> = enabledChipperSuites
        override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites
        @Throws(IOException::class)
        override fun createSocket(): Socket ?{
            return enableTLSOnSocket(delegate.createSocket())
        }

        @Throws(IOException::class)
        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket ?{
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose))
        }

        @Throws(IOException::class, UnknownHostException::class)
        override fun createSocket(host: String, port: Int): Socket ?{
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class, UnknownHostException::class)
        override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket ?{
            return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort))
        }

        @Throws(IOException::class)
        override fun createSocket(host: InetAddress, port: Int): Socket? {
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class)
        override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort))
        }

        private fun enableTLSOnSocket(socket: Socket?): Socket? {
            if (socket != null && socket is SSLSocket) {
                socket.enabledCipherSuites = this.enabledChipperSuites
                socket.enabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")
            }
            return socket
        }

        init {
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf(TrustAll()), null)
            delegate = context.socketFactory
            val supported = this.supportedCipherSuites
            val it = enabledChipperSuites.iterator()
            loop@ while (it.hasNext()) {
                val e = it.next()
                for (i in supported)
                    if (e == i) continue@loop
                it.remove()
            }
            this.enabledChipperSuites = enabledChipperSuites.toTypedArray()
        }

        private class TrustAll : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate?> =arrayOfNulls<X509Certificate>(0)
        }
    }
    abstract class MyWebView(protected val context: Context, protected var myUrl: String, private var host:String) : WebViewClient() {
        abstract val interf:InterfaceA
        protected val locker= Semaphore(1)
        internal lateinit var webView:WebView
        companion object {
            const val callbackTitle = "KOTLIN"
            const val FETCH_STRING = "if(!window.fetch)window.fetch = function(u,d){" +
                    "var x = new XMLHttpRequest();\n" +
                    "x.open('GET',u);\n" +
                    "if(d.type)" +
                    "x.responseType = d.type;\n" +
                    "Object.keys(d.headers||{}).forEach(function(key){x.setRequestHeader(key,d.headers[key]);});" +
                    "x.send();" +
                    "return {" +
                    "then:function(callbackF){" +
                    "var success;" +
                    "x.onload = function(){" +
                    "var f = function(){return x.response};" +
                    "var ans = callbackF({text:f,arrayBuffer:f});" +
                    "if(success)success(ans);" +
                    "};" +
                    "return {" +
                    "then:function(e){success=e}" +
                    "}" +
                    "}" +
                    "}" +
                    "};\n"+
                    "if(!Uint8Array.from)Uint8Array.from=function(a){\n" +
                    "if(Uint8Array.slice)return a.slice();\n" +
                    "var n=new Uint8Array(a.length);\n" +
                    "if(n.set)n.set(a,0);\n" +
                    "else for(var i=0;i<a.length;i++)n[i] = a[i];\n" +
                    "return n;" +
                    "};"
        }
        abstract inner class InterfaceA { }

        var error:Exception? = null
        val isError:Boolean get() = error !=null
        init{
            try {
                locker.acquire()
            }catch (e: InterruptedException){
                error=e
            }
        }
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String){
            error = IOException(description)
            locker.release()
        }
        @TargetApi(android.os.Build.VERSION_CODES.M)
        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse) {

            error = IOException(errorResponse.reasonPhrase + errorResponse.data)
            locker.release()
        }
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
            handler.proceed()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }
        @SuppressLint("AddJavascriptInterface", "JavascriptInterface", "SetJavaScriptEnabled")
        fun initilize(){
            webView = WebView(context).apply{
                //settings.blockNetworkLoads = false
                settings.javaScriptEnabled = true
                settings.databaseEnabled = true

//                settings.setAppCacheEnabled(false)
                settings.domStorageEnabled = true
                webViewClient = this@MyWebView
                webChromeClient = object : WebChromeClient(){
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        if(consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                            if (Config.NEED_LOG) Log.e(Config.LOG_TAG, consoleMessage.message())
                            locker.release()
                        }
                        else if (Config.NEED_LOG) Log.i(Config.LOG_TAG, consoleMessage.message())
                        return super.onConsoleMessage(consoleMessage)
                    }
                }
                addJavascriptInterface(interf, callbackTitle)
                settings.userAgentString = getRandomUserAgent().name
            }
            loadData()

        }
        open fun loadData(){
            webView.loadDataWithBaseURL(host, "<script>'use strict';${FETCH_STRING};${loadScript()}</script>", "text/html", "utf-8", Config.YUMMY_ANIME_URL)
        }
        abstract fun loadScript(): String
        /*
        @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
        override fun onPageFinished(view: WebView, url: String) = with(view){
            settings.javaScriptEnabled = true
            (context as MyApp).threadCallback.post {
                when (type) {
                    ItemType.TEXT, ItemType.HTML ->
                        loadUrl("javascript:${callbackTitle}.call(document.getElementsByTagName('html')[0].${if (type == ItemType.TEXT) "innerText" else "innerHTML"})")
                    ItemType.IMAGE -> loadUrl("javascript:var a=document.getElementsByTagName(\"img\")[0]," +
                                "b=document.createElement(\"canvas\");" +
                            "function onEnd(){" +
                                "b.width=a.width;" +
                                "b.height=a.height;" +
                                "b.getContext(\"2d\").drawImage(a,0,0);" +
                                "${callbackTitle}.call(a.width+' '+a.height)/*b.toDataURL('image/jpeg')+b.width+' '+b.height);*/" +
                            "}"+
                            "document.readyState=='complete'?onEnd():(a.onload = onEnd)");
                }
            }
            return@with
        }*/

        /*override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            request?.url
            if(request?.url.toString() == this.myUrl) {
                isError = true
                answer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) error?.description
                        ?: "" else ""
                locker.release()
            }
        }*/
        @Throws(InterruptedException::class)
        fun lock(){
            if(this.isError)locker.release()
            else locker.acquire()
        }
        fun stop() = (context as MyApp).runRunnableInUI {
            webView.destroy()
        }
    }
    class TextWebView(context: Context, myUrl: String, host:String,
                      val headers: Map<String, String>? = null) : MyWebView(context, myUrl, host) {
        public lateinit var answer:String
        override val interf = Interface()
        private var retryCount:Int = 0

        inner class Interface:InterfaceA(){
            @JavascriptInterface
            fun call(ans: String){
                if(++retryCount <= 3 && ans.contains("Please turn JavaScript on and reload the page.")){
                    webView.loadDataWithBaseURL(myUrl, ans, "text/html", "utf-8", "/")
                }
                else {
                    answer = ans
                    locker.release()
                }
            }
        }
        override fun loadScript() : String{
            val headersStr = "{${headers?.run{
                "headers:" + JSONObject(headers.filter { it.key.toLowerCase(Locale.ROOT) != "user-agent" }).toString()}?:""
            }," +
                "credentials:'include'" +
            "}"
            return "\nfetch('${myUrl}',${headersStr})\n" +
                        ".then(function(e){return e.text()})\n" +
                        ".then(function(t){${callbackTitle}.call(t)})"
        }
        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
             return url?.let {
                 ++retryCount;
                 this.myUrl = url
                 this.loadData()
                 false
            }?:true
        }

    }
    class BlobWebView(context: Context, blobUrl: String, host: String) : MyWebView(context, blobUrl, host){
        public lateinit var answer:ByteArray
        override val interf = Interface()
        override fun loadScript()="\nfetch('${myUrl}',{type:'arraybuffer'})" +
                ".then(function(e){return e.arrayBuffer()})\n" +
                ".then(" +
                    "function(t){${callbackTitle}.call(Uint8Array.from(new Uint8Array(t)))}"+
                ")"


        inner class Interface:InterfaceA(){
            @JavascriptInterface
            public fun call(ans: ByteArray){
                answer = ans
                locker.release()
            }
        }
    }
//    class CloudFlareWebView(context: Context, url: String, private val data:String) : MyWebView(context, url){
//        override val interf: InterfaceA = object:InterfaceA(){
//
//        }
//
//        override fun loadScript() = "";
//        override fun loadData() {
//            webView.loadDataWithBaseURL(this.myUrl, data,"text/html", "utf-8","/")
//
//        }
//
//        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//            request!!.url
//            return super.shouldOverrideUrlLoading(view, request)
//        }
//
//
//    }
}
