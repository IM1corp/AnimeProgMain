package com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.feed

import android.content.res.Resources
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
import com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.YummyIAnimeJson
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList

class YummyMainPage() {
    @SerializableField("top_carousel") public var topCarousel: YummyCarousel = YummyCarousel()
    @SerializableField("announcements") public var announcements: ArrayList<YummyIAnimeJson> = arrayListOf()
    @SerializableField("recommends") public var recommends: ArrayList<YummyIAnimeJson> = arrayListOf()
    @SerializableField("new_videos") public var newVideos: ArrayList<IFeedVideoJson> = arrayListOf()
    @SerializableField("new") public var newItems: ArrayList<YummyIAnimeJson> = arrayListOf()

    fun toMainPage(resources: Resources): MainPage = MainPage().also{ page->
        page.rows.add(RowList(
                OneAnime.Link.noHref(getTopTitle(resources)),
                ArrayList(topCarousel.items.map{it.toOneAnime(resources)})
        ))
        page.rows.add(RowList(
                OneAnime.Link(resources.getString(R.string.anime_updates), "/updates"),
                ArrayList(newVideos.map{it.toOneAnime(resources)})
        ))
        page.rows.add(RowList(
                OneAnime.Link(resources.getString(R.string.announcement), "/announcement"),
                ArrayList(announcements.map{it.toOneAnime(resources)})
        ))
        if(recommends.size > 0 )
            page.rows.add(RowList(
                    OneAnime.Link(resources.getString(R.string.recommends), "/recommends"),
                    ArrayList(recommends.map{it.toOneAnime(resources)})
            ))
        page.bigRow = RowList(
                OneAnime.Link(resources.getString(R.string.site_news), "/catalog"),
                ArrayList(newItems.map{it.toOneAnime(resources)})
        )
    }

    fun getTopTitle(resources: Resources) = resources.getString(R.string.main_page_title, resources.getString(when(topCarousel.season){
        1->R.string.season_1_f
        2->R.string.season_2_f
        3->R.string.season_3_f
        else->R.string.season_4_f
    })+" "+topCarousel.year)

}