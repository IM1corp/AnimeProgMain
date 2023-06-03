package com.imcorp.animeprog.Requests.JsonObj.AnimeJoy

import com.imcorp.animeprog.Requests.Http.InvalidHtmlFormatException
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object AJMainPageLoader {
    @JvmStatic
    fun load(html: Document,): MainPage {
        fun thrw(error: String):Element = throw InvalidHtmlFormatException(InvalidHtmlFormatException.getHtmlError(error))
        val content: Element = html.selectFirst("#content")?:thrw("no content element")
        val items = AJSearchLoader.loadDataFromHtml(content)
        return MainPage(ArrayList(),ArrayList(),
                RowList(OneAnime.Link.noHref("Anime"), ArrayList(items.size)).apply{
                    this.list.addAll(items.map{ it.anime })
                })
    }

}