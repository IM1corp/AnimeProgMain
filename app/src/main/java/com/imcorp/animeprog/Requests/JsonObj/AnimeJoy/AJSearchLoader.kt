package com.imcorp.animeprog.Requests.JsonObj.AnimeJoy

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import org.jsoup.nodes.Element

object AJSearchLoader{
    public fun loadDataFromHtml(html: Element) : ArrayList<OneAnime.OneAnimeWithId> =
            ArrayList<OneAnime.OneAnimeWithId>().apply{addAll(
        html.select("article.shortstory").map{
            val titleElHref = it.selectFirst("a")
            val image = it.selectFirst("picture img").attr("src")
            val description = it.selectFirst("p[itemprop='description']")?.text()?:""
            val year = Config.loadIntegerFromText(html.selectFirst(".timpact:containsOwn(Дата)")?.parent()?.ownText()?:"0000",4)
            OneAnime.fromSearch(Config.HOST_ANIME_JOY,year,titleElHref.attr("href"),
                titleElHref.text(),image,description)
        }
    )}
}