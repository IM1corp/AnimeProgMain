package com.imcorp.animeprog.Requests.JsonObj.YummyAnime

import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class YMainPageLoader(private val html: Document) {
    companion object {
        val yearFindRegex = "\\d{4,5}".toRegex()
        val episodesFindRegex = "Серий: \\d+".toRegex()
    }
    private val ans=MainPage()
    fun load():MainPage{
        this.parseSlider()
        this.parseContainers()
        this.parseTabs()
        return ans
    }
    private fun parseHrefAndImg(item: Element,to: OneAnime):Boolean{
        item.selectFirst("a")?.attr("href")?.let{
            to.setPath(it)
        }?:return false
        item.selectFirst("img")?.attr("src")?.let{
            to.setBigCover(it)
            to.cover=it
        }?:return false
        return true
    }
    private fun parseTabs() {
        fun parseItem(el:Element):OneAnime?{
            val ans = OneAnime(Config.HOST_YUMMY_ANIME).apply {
                fun parseDataFromString(data:String){
                    //data like ' 1997 | Сериал | Серий: 26'
                    year = yearFindRegex.find(data)?.value?.toInt()?:0
                    //attrs.epCount = episodesFindRegex.find(data)?.groups?.get(1)?.value?.toInt()?:0
                }
                el.selectFirst(".preview-title").text()?.let{title=it}
                el.selectFirst(".main-rating").text()?.let{attrs.rating = it}
                el.selectFirst(".content-main-info, ul")?.let {
                    attrs.genres=YAnimeLoader.loadGenres(it)
                    it.selectFirst("li")?.text()?.let{parseDataFromString(it)}
                }
            }
            if(!parseHrefAndImg(el,ans)) return null
            return ans
        }
        html.select("ul.tabs li").forEachIndexed { index, tab ->
            val title = tab.text()
            val container = html.selectFirst(tab.attr("data-id")) ?:
                html.selectFirst(".tab-content:eq(${index})") ?: return@forEachIndexed
            val items = ArrayList<OneAnime>()
            for(el in container.select(".preview-block"))
                parseItem(el)?.let{items.add(it)}
            RowList(OneAnime.Link.noHref(title),items).also{
                if(index==0) ans.bigRow=it
                else ans.rows.add(it)
            }
        }
    }
    private fun parseContainers() {
        fun parseOneULItem(el:Element): OneAnime? {
            val ans = OneAnime(Config.HOST_YUMMY_ANIME)
            el.selectFirst("a")?.attr("href")?.let{
                ans.setPath(it)
            }?:return null
            el.selectFirst("img")?.attr("src")?.let{
                ans.setBigCover(it)
                ans.cover=it
            }
            el.selectFirst(".update-title")?.text()?.let{ ans.title=it }
            el.selectFirst(".update-date")?.text()?.let{ ans.dataPosterText.mainTitle = it }
            el.selectFirst(".update-info")?.text()?.let{ ans.dataPosterText.subTitle = it}
            return ans
        }
        for(header in html.select(".col-container .block-header")){
            val container = header.nextElementSibling()
            if( container.tagName() != "ul" || ! container.hasClass("update-list") )continue

            val items = ArrayList<OneAnime>()
            ans.rows.add(RowList(OneAnime.Link(header.ownText(),header.select("a")?.attr("href")?:""),items))
            for(el in container.children())
                parseOneULItem(el)?.let{items.add(it)}

        }
    }
    private fun parseSlider(){
        fun parseItem(item: Element) : OneAnime?{
            val ans = OneAnime(Config.HOST_YUMMY_ANIME)
            if(!parseHrefAndImg(item,ans)) return null
            ans.title = item.text()
            return ans
        }
        val dataTitle = html.selectFirst("#slider")?.parent()?.selectFirst(".block-header")?.text()
        dataTitle?.let{title->
            val items = ArrayList<OneAnime>()
            ans.rows.add(RowList(OneAnime.Link.noHref(title),items))
            for(item in html.select("#slider .carousel-cell"))
                parseItem(item)?.let{items.add(it);}
        }
    }
}