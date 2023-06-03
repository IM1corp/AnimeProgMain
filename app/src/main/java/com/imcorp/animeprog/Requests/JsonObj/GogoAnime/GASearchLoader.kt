package com.imcorp.animeprog.Requests.JsonObj.GogoAnime

import com.imcorp.animeprog.Config.HOST_GOGO_ANIME
import com.imcorp.animeprog.Default.backgroundFromCss
import com.imcorp.animeprog.Default.toHtml
import com.imcorp.animeprog.Requests.JsonObj.GogoAnime.GAMainPageLoader.thrw
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import org.json.JSONObject
import org.jsoup.nodes.Document
import java.util.ArrayList

object GASearchLoader {
    @JvmStatic
    fun fromHtml(html: Document): ArrayList<OneAnime.OneAnimeWithId> {
        val dataAns = GAMainPageLoader.parseRowListMain(html)
        return ArrayList(dataAns.map { OneAnime.OneAnimeWithId(it.path.hashCode(), it) })
    }

    @JvmStatic
    fun fromJson(json: JSONObject?): ArrayList<OneAnime.OneAnimeWithId> {
        val document = json?.getString("content")?.toHtml()?: thrw("No search content element found")
        return ArrayList(document.select(".list_search_ajax").map {
            val thumb = it.selectFirst(".thumbnail-recent_search") ?: thrw("No thumb element found")
            val title = it.text().trim()
            val href = it.selectFirst("a")?.attr("href")
            OneAnime.OneAnimeWithId(title.hashCode(), OneAnime(HOST_GOGO_ANIME).apply{
                this.title = title
                this.cover = thumb.backgroundFromCss
                this.path = href
            })
        })
    }

}