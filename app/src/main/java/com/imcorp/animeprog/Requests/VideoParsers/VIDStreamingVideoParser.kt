package com.imcorp.animeprog.Requests.VideoParsers

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEpisodeQuality
import java.util.regex.Pattern

class VIDStreamingVideoParser(frame_url: String, request: Request, url_from: String) : SimpleVideoParser(frame_url, request, url_from) {
    override fun loadToOneVideoEpisode(episode: OneVideo) {
        val frameUrlNew = "https://gogoanime.bio/streaming/01.php?"+frameUrl.split('?',limit=2).last()
        val frameHtml = request.loadFrameFromUrl(frameUrlNew, urlFrom)
        val file = find_url_pattern.matcher(frameHtml).run {
            if(this.find()) this.group(1) else ""
        }
        if(file.isNotBlank()) {
            episode.videoQualities.add(OneVideoEpisodeQuality(file, "", OneVideo.VideoType.AUTO, frameUrlNew))
        }
        else throw InvalidHtmlFormatException.NoVideosFoundException("Videos not found :(")
    }
    val find_url_pattern: Pattern by lazy {
        Pattern.compile("file: ['\"]?(.+?)['\"]")
    }
}