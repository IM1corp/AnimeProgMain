package com.imcorp.animeprog.MainActivity.search

import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import java.io.IOException

class MainSearch(fragment: SearchFragment) : SimpleSearchAdapter(fragment,fragment) {

    @kotlin.jvm.Throws(IOException::class)
    override fun getLittleData(q: String): ArrayList<OneAnime.OneAnimeWithId> = activity.request.searchJson(lastSearch,fragment.activity.dataBase.settings.searchHost)
    @kotlin.jvm.Throws(IOException::class)
    override fun getBigSearchData(q: String): ArrayList<OneAnime.OneAnimeWithId> = activity.request.searchBig(q,1,fragment.activity.dataBase.settings.searchHost)

}