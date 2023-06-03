package com.imcorp.animeprog.Requests.VideoParsers

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import com.imcorp.animeprog.Requests.VideoParsers.AllohaVideoParser.BASE64Decrypt
import org.json.JSONException
import org.jsoup.Jsoup
import java.io.IOException
import java.net.MalformedURLException
import java.nio.charset.Charset
import java.nio.charset.Charset.*
import java.util.*
import java.util.regex.Pattern

class KodikVideoParser(frame_url: String?, request: Request?, url_from: String?) : SimpleVideoParser(frame_url, request, url_from) {
    private val videoGetVars = HashMap<String, String>()
    private val videoGetJsonParams = HashMap<String, String>()
    private var jsonUrl: String? = null
    @Throws(IOException::class, JSONException::class)
    override fun loadToOneVideoEpisode(episode: OneVideo) {
        //this.loadFrameUrlFromFirstFrame();
        loadParamsFromHtml()
        loadParamsFromJs()
        getInfoFromJson(episode)
    }

    @Throws(IOException::class, JSONException::class)
    private fun getInfoFromJson(episode: OneVideo) {
        val json = request.post(jsonUrl, videoGetJsonParams).toJson()
        val links = json!!.getJSONObject("links")
        val it = links.keys()
        while (it.hasNext()) {
            val key = it.next()
            val q = OneVideoEpisodeQuality()
            q.loadVideoTypeFromString(key)
            val array = links.getJSONArray(key)
            q.m3u8Url = getGoodSrc(decodeURL(array.getJSONObject(0).getString("src")), frameUrl)
            q.ref = frameUrl
            val mp4_index = q.m3u8Url.lastIndexOf(".mp4:")
            if (mp4_index != -1) {
                q.mp4Url = q.m3u8Url.substring(0, mp4_index + 4)
            }
            episode.videoQualities.add(q)
        }
    }

    @Throws(MalformedURLException::class)
    private fun setDefJsonParams() {
        videoGetJsonParams["bad_user"] = "false"
        videoGetJsonParams.putAll(videoGetVars)
        /*for (Map.Entry<String,String> i:videoGetVars.entrySet()) {
            videoGetJsonParams.put(i.getKey(),i.getValue());
        }*/jsonUrl = getGoodSrc(POST_SRC, frameUrl)
    }

    @Throws(IOException::class)
    private fun loadParamsFromJs() {
        setDefJsonParams()
        /*
        String javascript = request.loadTextFromUrl(scriptSrcToFindHash2Param,false).toString();
        Matcher m= find_hash2_param.matcher(javascript);
        String data;
        if(!m.find()||(data = m.group(1))==null){
            setDefJsonParams();
        }else{

            for (String i:data.split(",")) {
                String[] s= i.split(":",2);
                if(s.length!=2){
                    setDefJsonParams();
                    break;
                }
                String key = s[0].trim(),value = s[1].trim();
                if(videoGetVars.containsKey(value)){
                    videoGetJsonParams.put(key,videoGetVars.get(value));
                    videoGetVars.remove(value);
                }
                else if(key.equals("bad_user")){
                    videoGetJsonParams.put("bad_user","false");
                }
                else if(value.startsWith("\"")||value.startsWith("'")){
                    videoGetJsonParams.put(key,value.substring(1,value.length()-1));
                }
            }
            for (Map.Entry< String,String> key:videoGetVars.entrySet()) {
                videoGetJsonParams.put(key.getKey(),key.getValue());
            }
            StringBuilder json_url = new StringBuilder();
            int index =javascript.indexOf("url",m.end());
            if(index!=-1) {
                boolean a_g=false;
                for (index = index + 3; index < javascript.length(); index++) {
                    char ch = javascript.charAt(index);
                    if (ch == '"' || ch == '\'') {
                        if (!a_g){
                            a_g=true;
                            json_url.delete(0,json_url.length());
                            continue;
                        }
                        break;
                    }
                    json_url.append(ch);
                }
            }
            jsonUrl = getGoodSrc(json_url.length()==0?"/get-vid":json_url.toString(),this.frameUrl);

        }

         */
    }

    @Throws(IOException::class)
    private fun loadParamsFromHtml() {
        val html = request.loadFrameFromUrl(frameUrl, urlFrom)
        urlFrom = frameUrl
        val doc = Jsoup.parse(html)
        for (el in doc.select("script")) {
            val text = el.data().trim()
            if (text.isNotEmpty()) {
                var m = find_params_pattern.matcher(text)
                while (m.find()) {
                    val key = m.group(1)!!
                    val value = m.group(2)!!
                    videoGetVars[key] = value
                }
                m = Pattern.compile("videoInfo\\.hash ?\\= ? ['\"](.+?)['\"]").matcher(text)
                if (m.find()) {
                    val value = m.group(1)!!
                    videoGetVars["hash"] = value
                }
            }
        }
        if (videoGetVars.size == 0) throw InvalidHtmlFormatException("Html is invalid - no params in html")
    }

    @Throws(IOException::class)
    private fun loadFrameUrlFromFirstFrame() {
        val url = frameUrl
        val data = request.loadFrameFromUrl(url, urlFrom)
        val match = find_url_pattern.matcher(data)
        if (!match.find()) {
            throw InvalidHtmlFormatException("Html is invalid - no src in html")
        }
        urlFrom = frameUrl
        frameUrl = match.group(1)
        if (frameUrl == null) throw InvalidHtmlFormatException("HTML is invalid - frame url is null")
        if (frameUrl.startsWith("//")) {
            frameUrl = "https:" + frameUrl
        } else if (frameUrl.startsWith("/")) {
            for (i in url.split("/".toRegex()).toTypedArray()) {
                if (!i.isEmpty() && i != "http:" && i != "https:") {
                    frameUrl = "https://" + i + frameUrl
                    break
                }
            }
        }
    }

    companion object {
        /* Класс для получения ссылок на video из плеера kodik.
        Принцип такой - сначало посылаем запрос по адресу iframe'a.
        Далее, находим там iframe с video.                                - loadFrameUrlFromFirstFrame
        Далее, посылаем запрос по этому iframe
        Далее получаем из этого iframe post параметры,                    - loadParamsFromHtml
         и ссылку на скрипт,                                              - scriptSrcToFindHash2Param
         из которого где надо достать еще один post параметр              - loadParamsFromJs
         и ссылку
         по которой надо послать запрос с post параметрами,
         чтобы получить ссылку json с ссылками на видео.

     */
        private const val POST_SRC = "/gvi"
        private val find_url_pattern = Pattern.compile("\\.src ?= ?\\\\?[\"|'](.+?)\\\\?[\"|']")
        private val find_params_pattern = Pattern.compile("([a-zA-Z0-9_]+?)[ |]=[ |][\"|']([^'\"]+?)[\"|']")
        private const val KEY_THAT_SCRIPT_HAS = "app.promo"
        private const val HASH2_DEF_PARAM = "OErmnYyYA4wHwOP"
        private val AZPattern = "[a-zA-Z]".toRegex()
         private fun decodeURL(src: String): String {
            return try {
                val ans = (
                        src.replace(AZPattern) { e ->
                            val code = e.value[0].toInt() + 13
                            if ((e.value[0] <= 'Z' && code > 'Z'.toInt()) || code > 'z'.toInt()) {
                                code - 26
                            } else {
                                code
                            }.toChar().toString()
                        }
                )
                val data = BASE64Decrypt.fromBase64(ans.toString())
                String(data, forName("ISO-8859-1"))
            } catch (e: Exception) {
                src
            }
        }

    }
}