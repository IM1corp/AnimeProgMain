package com.imcorp.animeprog.Requests.VideoParsers

import com.imcorp.animeprog.Default.toHtml
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException.getHtmlError
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import org.jsoup.nodes.Document

class AllVideoVideoParser(frame_url: String, request: Request, url_from: String) :
        SimpleVideoParser(frame_url, request, url_from) {
    companion object {
        private val FIND_FILE_REGEX by lazy {"file: ?[\"|'](.+)[\"|'],".toRegex()}
        val FIND_QUALITY by lazy {"\\[\\d+p]".toRegex()}
    }
    private lateinit var html: Document
    override fun loadToOneVideoEpisode(episode: OneVideo) {
        html = request.loadFrameFromUrl(frameUrl, urlFrom).toHtml(frameUrl)
        for( i in html.select("script")){
            val data = i.data()
            if(!data.contains("new Playerjs") || !data.contains("file"))continue;
            val items = FIND_FILE_REGEX.find(data)?.groupValues?.last()?.split(',')
                    ?: throw InvalidHtmlFormatException(getHtmlError("No file found"))
            episode.videoQualities.addAll(items.map{
                val url = it.trim()
                val quality = FIND_QUALITY.find(url)?.value?:""
                OneVideoEpisodeQuality(null, url.replaceFirst(quality, ""), quality)
            })
            break
        }
    }
}

