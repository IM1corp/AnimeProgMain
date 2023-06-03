package com.imcorp.animeprog.Requests.VideoParsers

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import java.net.URL
import java.net.URLDecoder

class AJVideoParser(frame_url: String, request: Request, url_from: String) : SimpleVideoParser(frame_url, request, url_from) {
    override fun loadToOneVideoEpisode(episode: OneVideo) {
        val querys = URL(frameUrl).query.split('&')
                .associateBy(
                        {it.substringBefore('=')},
                        { URLDecoder.decode(it.substringAfter('='),"UTF-8")}
                )
        val files = querys["file"]?.split(',')
                ?: throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError("no video url found"))
        episode.videoQualities.addAll(files.map{
            val v = AllVideoVideoParser.FIND_QUALITY.find(it)?.value?:""
            OneVideoEpisodeQuality(null, it.replaceFirst(v,""), v, urlFrom)
        })

    }
}