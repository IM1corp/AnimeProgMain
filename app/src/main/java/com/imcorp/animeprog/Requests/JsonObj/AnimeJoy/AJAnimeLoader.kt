package com.imcorp.animeprog.Requests.JsonObj.AnimeJoy

import android.content.Context
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Config.HOST_ANIME_JOY
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.SimpleAnimeLoader
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideoEP
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.YMainPageLoader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object AJAnimeLoader: SimpleAnimeLoader {
    val FIND_STATUS_REGEX by lazy{"\\[(\\d+) из (\\d+)]".toRegex()}
    val FIND_NUM_REGEX by lazy{"\\d+".toRegex()}


    private fun selectParam(document:Element, key:String):Element? = document.selectFirst(".timpact:containsOwn(${key})")?.parent()
    override fun fromHtml(html: Document, path: String, context: Context) = OneAnime(HOST_ANIME_JOY).apply {
        val data: Element = html.selectFirst("#content")
        fun loadData() {
            fun loadStatus() = (
                    FIND_STATUS_REGEX.find(title)?.run {
                        this.groups[1] == this.groups[2]
                    } == true).run { if (this) context.getString(R.string.ready) else context.getString(R.string.ongoing) }
            title = data.selectFirst(".ntitle")?.text() ?: ""
            description = data.select(".pcdescrf p").joinToString("\n") { it.ownText() }
            year = selectParam(data, "Дата")?.ownText()?.run {
                YMainPageLoader.yearFindRegex.find(this)?.value?.toInt()
            } ?: 0
            status = loadStatus()
            cover = data.selectFirst("picture img")?.attr("src")
        }

        fun loadAttrs() = with(attrs) {
            fun getEpCount(): Int = selectParam(data, "серий")?.ownText()?.run {
                FIND_NUM_REGEX.find(this)?.value?.toInt()
            } ?: 0
            synonyms = ArrayList<String>()
            data.selectFirst(".romanji")?.text()?.let {
                synonyms.add(it)
            }
            selectParam(data, "название")?.let { titleEl ->
                titleEl.children().last()?.let {
                    val subtext = it.text().trim()
                    if (!synonyms.contains(subtext)) synonyms.add(subtext)
                }
                var el: Element = titleEl
                while (el.nextElementSibling().also { el = it }.text() == el.ownText())
                    synonyms.add(el.text())
            }
            epCount = getEpCount()
            genres = loadGenres(data)
            studios = arrayListOf(parseLink(data, "Студия"))
            producers = arrayListOf(parseLink(data, "Режиссер"))
        }
        loadData()
        loadAttrs()
        viewingOrder = loadViewingOrder(data, path)
        videos = loadVideos(data, path, context)
        setPath(path)
    }
    private fun loadGenres(doc:Element) : ArrayList<OneAnime.Link> = ArrayList<OneAnime.Link>().apply {
        val el = selectParam(doc,"Жанр") ?: return@apply
        addAll(el.select("a").map{OneAnime.Link(it)})
    }
    private fun parseLink(doc:Element, key:String):OneAnime.Link? = selectParam(doc,key)?.run{
        selectFirst("a")?.run{OneAnime.Link(this)}?:OneAnime.Link.noHref(ownText())
    }
    private fun loadViewingOrder(doc:Element,currentHref:String):
            ArrayList<OneAnime>? = doc.select(".text_spoiler li")?.run{
        val ans = ArrayList<OneAnime>(size)
        ans.addAll(map{ element->
            OneAnime(HOST_ANIME_JOY).apply {
                path = element.selectFirst("a")?.attr("href")?:currentHref
                val text = element.text()
                try{
                    year = YMainPageLoader.yearFindRegex.find(text)?.value?.toInt()?:0
                    title = text.replace(year.toString(),"").trim()
                    if(title.last() == ',')title = title.substring(0,title.length-2)
                }catch(e:Exception){
                    title = text
                }
                description=""
            }
        })

        return ans
    }
    private fun loadVideos(doc:Element, path:String, context: Context): ArrayList<OneVideoEP> {
        fun loadHtml():Element{
            val dataId = doc.selectFirst(".playlists-ajax")?.attr("data-news_id")?:
                FIND_NUM_REGEX.find(path)?.value?: throw InvalidHtmlFormatException.NoVideosFoundException("No videos found - no video id found")
            val json = (context as MyApp).request.loadTextFromUrl(
                    Config.ANIMEJOY_URL + "/engine/ajax/playlists.php?news_id=${dataId}&xfield=playlist",
                    false).toJson()
            if (json?.getBoolean("success")!=true)
                throw InvalidHtmlFormatException.NoVideosFoundException("No videos found - ${json?.getString("message")?:"???"}")
            return Jsoup.parse(json.getString("response"), path)
        }
        val parentEls = loadHtml()
        val ans = HashMap<String, ArrayList<OneVideo>>()
        for(element in parentEls.select(".playlists-videos ul li")){
            val number = element.ownText().replace("серия","").trim()
            val list:ArrayList<OneVideo> = ans[number] ?: ArrayList<OneVideo>().also{ans[number] = it}
            list.add(OneVideo(number).apply{
                urlFrame = element.attr("data-file")
                voiceStudio = context.getString(R.string.subtitle)
                loadOneVideoPlayerFromUrl()
            })
        }
        return ArrayList(ans.map {
            OneVideoEP(true).apply {
                num = it.key
                initVideos(it.value.size).addAll(it.value)
            }
        }.sortedBy {
            try {
                it.num.toInt()
            } catch (e: NumberFormatException) {
                it.num.sumOf {eb-> eb.toInt() }
            }
        })
    }
}