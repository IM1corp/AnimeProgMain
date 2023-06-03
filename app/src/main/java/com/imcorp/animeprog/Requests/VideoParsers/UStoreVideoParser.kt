package com.imcorp.animeprog.Requests.VideoParsers

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.Request
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo

class UStoreVideoParser(frame_url:String,request: Request,urlFrom:String): SimpleVideoParser(frame_url,request,urlFrom) {
    override fun loadToOneVideoEpisode(episode: OneVideo?) {
        throw InvalidHtmlFormatException("Not yet implemented")
    }
}