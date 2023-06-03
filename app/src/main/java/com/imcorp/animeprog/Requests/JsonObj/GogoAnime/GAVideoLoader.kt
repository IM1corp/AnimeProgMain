package com.imcorp.animeprog.Requests.JsonObj.GogoAnime

import android.content.Context
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP
import org.jsoup.nodes.Document

object GAVideoLoader {
    @JvmStatic
    fun loadVideosInto(episode: OneVideoEP, doc: Document, dub: String, context: Context) = ArrayList<OneVideo>().apply{
        for(el in doc.select(".anime_muti_link li a")){
            val src = el.attr("data-video")
            val player = el.ownText().trim()
            add(OneVideo(episode.num).apply{
                this.urlFrame = src
                this.voiceStudio = dub
                this.loadOneVideoPlayerFromUrl()
            })
        }

    }
}