package com.imcorp.animeprog.Requests.JsonObj.GogoAnime

import android.content.Context
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Config.HOST_GOGO_ANIME
import com.imcorp.animeprog.Default.backgroundFromCss
import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.Http.ReqBuild
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.mainPage.Container
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import kotlinx.coroutines.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object GAMainPageLoader {
    class DataThreadAns{
        lateinit var month:Document
        lateinit var day: Document
        lateinit var week: Document
    }
    suspend fun loadDataThread(context: Context, ans: DataThreadAns) = withContext(Dispatchers.IO) {
        val task = (1..3).map {
            async {
                val ansHtml = ReqBuild(context, "https://ajax.gogo-load.com/anclytic-ajax.html?id=$it&link_web=https://www1.gogoanime.day/", false)
                        .addHeader("referer", Config.GOGOANIME_URL)
                        .addHeader("origin", Config.GOGOANIME_URL)
                        .SendRequest().toHtml()
                if (ansHtml != null)
                    when (it) {
                        1 -> ans.day = ansHtml
                        2 -> ans.week = ansHtml
                        3 -> ans.month = ansHtml
                    }


            }
        }
        task.awaitAll()
    }
    @JvmStatic
    fun load(html: Document, mContext: Context): MainPage {
        val popOngoingUpdates = ReqBuild(mContext, "https://ajax.gogo-load.com/ajax/page-recent-release-ongoing.html?page=1", false)
                .addHeader("referer", Config.GOGOANIME_URL)
                .addHeader("origin", Config.GOGOANIME_URL)
                .SendRequest().toHtml()?: thrw("no popular ongoing updates element")
        val ans = DataThreadAns().also{
            runBlocking {
                val dataOngoinUpdates = loadDataThread(mContext, it)
            }
        }
        val contentUpdates : Element = html.selectFirst("#load_recent_release")?:thrw("no content element")
        return (MainPage()).apply {
            val rowTop = RowList(OneAnime.Link.noHref("New ongoing episodes"), parseRowListMain(contentUpdates))
            bigRow = RowList(
                    OneAnime.Link.noHref(popOngoingUpdates.selectFirst("h2")?.text()?.capitalize(Locale.ENGLISH)
                            ?: "Popular ongoing update"),
                    parseAnimeListFromBlocks(popOngoingUpdates)
            )
            rows.add(rowTop)
            val images = HashMap<String, OneAnime>().apply {
                for(rowList in arrayOf<RowList>(bigRow, rowTop))
                for(anime in rowList.list){
                    this[anime.title] = anime
                }
            }
            this.containers.add(Container(OneAnime.Link.noHref("Last updates"), ArrayList<RowList>().apply{
                add(RowList(OneAnime.Link.noHref("Today"), buildLastRealeseBlocks(ans.day, images)))
                add(RowList(OneAnime.Link.noHref("Week"), buildLastRealeseBlocks(ans.week, images)))
                add(RowList(OneAnime.Link.noHref("Month"), buildLastRealeseBlocks(ans.month, images)))
            }))
//            val items = AJSearchLoader.loadDataFromHtml(content)
//            return MainPage(ArrayList(),ArrayList(),
//                    RowList(OneAnime.Link.noHref("Anime"), ArrayList(items.size)).apply{
//                        this.list.addAll(items.map{ it.anime })
//                    })


        }
    }
    private fun buildLastRealeseBlocks(element: Element, images: Map<String, OneAnime>) = ArrayList<OneAnime>().apply{
        for(el in element.select("ul li")){
            val link = el.selectFirst("a") ?: thrw("No link element found")
            val href = link.attr("href")
            val title = link.attr("title")
            val release = el.selectFirst(".reaslead") ?: thrw("No release element found")
            val image = if(title in images) images[title]?.cover else generateImageFromHref(href)
            add(OneAnime(HOST_GOGO_ANIME).apply{
                this.cover=image
                this.title=title
                this.path=href
                this.dataPosterText.mainTitle = release.text()
            })

        }
    }
    fun parseRowListMain(element: Element) = ArrayList<OneAnime>().apply{
        for(el in element.select("ul.items li")){
            val img = el.selectFirst("img") ?: thrw("No anime img element found")
            val title = el.selectFirst(".name a") ?: thrw("No anime title element found")
            val episode = el.selectFirst(".episode,.released")
            add(OneAnime(HOST_GOGO_ANIME).apply {
                this.cover = img.attr("src")
                this.title = title.attr("title")
                this.path = title.attr("href")
                episode?.let { this.dataPosterText.mainTitle = it.text() }
            })
        }
    }
    private fun parseAnimeListFromBlocks(element: Element) = ArrayList<OneAnime>().apply {
        for (el in element.select(".added_series_body li")) {
            val imgHref = el.selectFirst("a") ?: thrw("No anime href element found")
            val imgSrc = imgHref.selectFirst(".thumbnail-popular") ?: thrw("No anime thumbnail element")
            val genres = el.selectFirst(".genres")?: thrw("No anime genres element")

            add(OneAnime(HOST_GOGO_ANIME).apply {
                this.cover = imgSrc.backgroundFromCss
                this.title = imgHref.attr("title")
                this.path = imgHref.attr("href")
                this.attrs.genres = ArrayList(genres.select("a").map {
                    val title = it.text().trimStart(',').trim()
                    OneAnime.Link(title, it.attr("href"))
                })
                this.description = genres.text() + "\n" + (genres.nextElementSibling()?.text()?:"")
            })
        }
    }
    fun generateImageFromHref(href: String): String{
        val id = if("/category/" in href) href.split("/category/").last()
                 else href.split("-episode-")[0].split('/').last()
        return "https://cdn.statically.io/img/gogocdn.net/cover/$id.png"
    }
    fun getRealUrl(url: String): String{
        if("/category" !in url) {
            val path = url.split("-episode-")[0].split('/').last()
            return Config.GOGOANIME_URL + "/category/" + path
        }
        return url;
    }
    fun thrw(error: String): Element = throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError(error))


}