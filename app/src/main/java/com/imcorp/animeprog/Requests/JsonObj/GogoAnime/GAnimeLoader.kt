package com.imcorp.animeprog.Requests.JsonObj.GogoAnime

import android.content.Context
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Config.HOST_ANIME_JOY
import com.imcorp.animeprog.Config.HOST_GOGO_ANIME
import com.imcorp.animeprog.Requests.Http.ReqBuild
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.SimpleAnimeLoader
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object GAnimeLoader: SimpleAnimeLoader {
    val FIND_STATUS_REGEX by lazy{"\\[(\\d+) из (\\d+)]".toRegex()}
    val FIND_NUM_REGEX by lazy{"\\d+".toRegex()}


    private fun selectParam(document:Element, key:String):Element? = document.selectFirst(".type span:containsOwn(${key})")?.parent()
    override fun fromHtml(html: Document, path: String, context: Context) = OneAnime(HOST_GOGO_ANIME).apply {
        val data: Element = html.selectFirst(".anime_info_body_bg")
        fun loadData() {
            title = data.selectFirst("h1")?.text() ?: ""
            description = selectParam(data, "Plot")?.ownText()?:""
            year = selectParam(data, "Released")?.ownText()?.trim()?.let {
                val digits = it.filter { ch->ch.isDigit() }
                if(digits.isEmpty()) 0 else digits.toInt()
            } ?: 0
            status = selectParam(data, "Status")?.text()
            cover = data.selectFirst("img")?.attr("src")
        }

        fun loadAttrs() = with(attrs) {
            val typeElement = selectParam(data, "Type")?.selectFirst("a")
            issueDate = typeElement?.text()
            val syns = selectParam(data, "Other name")?.ownText()?.trim()?.run {
                this.split(" ;")
            }?: emptyList()
            synonyms = ArrayList(syns)
            genres = loadGenres(data)
        }
        loadData()
        loadAttrs()

        videos = loadVideos(html, context)
        setPath(path)
    }
    private fun loadGenres(doc:Element) : ArrayList<OneAnime.Link> = ArrayList<OneAnime.Link>().apply {
        val el = selectParam(doc,"Genre") ?: return@apply
        addAll(el.select("a").map{ OneAnime.Link(it.attr("title"),it.attr("href")) })
    }
    private fun loadVideos(doc:Element, context: Context) = ArrayList<OneVideoEP>().apply {
        val ans = ReqBuild(context, "https://ww1.gogoanime.re/ajax/load-list-episode", false)
                .add("ep_start", 0)
                .add("ep_end", 999)
                .add("id", doc.selectFirst("#movie_id")?.attr("value"))
                .add("default_ep", 0)
                .add("alias", doc.selectFirst("#alias_anime")?.attr("value"))
                .SendRequest()
                .toHtml()
        for(el in ans?.select("#episode_related li")?: emptyList()){
            add(OneVideoEP(true).apply{
                this.num = el.selectFirst(".name").ownText().trim()
                val href = el.selectFirst("a").attr("href").trim().run{
                    if(this.startsWith("/")) Config.GOGOANIME_URL+this else this
                }
                this.keys["href"]=href
                this.keys["dub"] = el.selectFirst(".cate").text().trim()
            })
        }
    }
}